# ai/app/domains/scenario/services/scenario_service.py
import asyncio
from typing import Tuple, Dict, Any, List

from .llm_service import LLMService
from .image_service import ImageService
from .s3_service import (
    ScenarioImageType,
    generate_scenario_image_key,
    get_presigned_upload,
    put_presigned,
)

class ScenarioService:
    """LLM → presign → DALL·E → PUT 업로드 → 최종 JSON 조립"""

    def __init__(self):
        self.llm = LLMService()
        self.image = ImageService()

    async def generate_scenario_with_presigned(
        self,
        *,
        prompt: str,
        question_count: int,
        options_per_question: int,
        category_id: int,
        image_size: str = "1024x1024",
        option_image_size: str = "1024x1024",
    ) -> Tuple[Dict[str, Any], Dict[str, Any]]:
        
        # 1) 텍스트 시나리오 생성/검증
        text = await self.llm.generate_scenario_text(
            prompt=prompt,
            question_count=question_count,
            options_per_question=options_per_question,
        )

        # 2) 썸네일/배경 presign
        thumb_key = generate_scenario_image_key(ScenarioImageType.THUMBNAIL, "image/png")
        bg_key = generate_scenario_image_key(ScenarioImageType.BACKGROUND, "image/png")
        thumb_ps = await get_presigned_upload(thumb_key, "image/png")
        bg_ps = await get_presigned_upload(bg_key, "image/png")

        # 3) 옵션 presign + key 주입
        presigns: Dict[str, Dict[str, str]] = {}
        for seq in text["sequences"]:
            for opt in seq["options"]:
                k = generate_scenario_image_key(ScenarioImageType.OPTION, "image/png")
                ps = await get_presigned_upload(k, "image/png")
                ref = f"seq{seq['seqNo']}.opt{opt['optionNo']}"
                presigns[ref] = ps
                opt["optionS3Key"] = ps["fileName"]  # LOW 난이도에서 이미지 사용

        # 4) 이미지 생성
        thumb_bytes = await self.image.generate_bytes(
            self.image.build_cover_prompt(prompt, "thumbnail"), size=image_size
        )
        bg_bytes = await self.image.generate_bytes(
            self.image.build_cover_prompt(prompt, "background"), size=image_size
        )

        async def gen_opt(q: str, label: str) -> bytes:
            return await self.image.generate_bytes(
                self.image.build_option_prompt(q, label), size=option_image_size
            )

        # 5) 업로드 (동시 실행)
        uploaded: List[Dict[str, str]] = []
        failed: List[Dict[str, str]] = []

        async def _upload(ref: str, url: str, data: bytes, key: str):
            try:
                await put_presigned(url, data, "image/png")
                uploaded.append({"ref": ref, "s3Key": key})
            except Exception as e:
                failed.append({"ref": ref, "reason": str(e)})

        tasks: List[asyncio.Task] = [
            asyncio.create_task(_upload("thumbnail", thumb_ps["uploadUrl"], thumb_bytes, thumb_ps["fileName"])),
            asyncio.create_task(_upload("background", bg_ps["uploadUrl"], bg_bytes, bg_ps["fileName"])),
        ]

        for seq in text["sequences"]:
            q = seq["question"]
            for opt in seq["options"]:
                ref = f"seq{seq['seqNo']}.opt{opt['optionNo']}"
                ps = presigns[ref]
                try:
                    img = await gen_opt(q, opt["optionText"])
                    tasks.append(asyncio.create_task(_upload(ref, ps["uploadUrl"], img, ps["fileName"])))
                except Exception as e:
                    failed.append({"ref": ref, "reason": f"image-gen: {e}"})

        await asyncio.gather(*tasks)

        # 6) 최종 JSON
        scenario = {
            "title": text["title"],
            "summary": text["summary"],
            "categoryId": category_id,
            "thumbnailS3Key": thumb_ps["fileName"],
            "backgroundS3Key": bg_ps["fileName"],
            "total_sequences": len(text["sequences"]),
            "sequences": text["sequences"],
        }
        return scenario, {"uploaded": uploaded, "failedRefs": failed}
