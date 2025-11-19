import asyncio
from typing import Tuple, Dict, Any, List

from .llm_service import LLMService
from .image_service import ImageService
from .rag_service import ScenarioRAGService
from .s3_service import (
    ScenarioImageType,
    generate_scenario_image_key,
    get_presigned_upload,
    put_presigned,
)

class ScenarioService:
    """LLM → RAG → presign → DALL·E → PUT 업로드 → 최종 JSON 조립"""

    def __init__(self):
        self.llm = LLMService()
        self.image = ImageService()
        self.rag = ScenarioRAGService()

    async def _build_augmented_prompt(
        self,
        user_prompt: str,
        rag_results: List[Dict[str, Any]],
        max_length: int = 2000,
    ) -> str:
        """
        RAG 검색 결과를 프롬프트에 주입

        Args:
            user_prompt: 사용자 원본 프롬프트
            rag_results: RAG 검색 결과 리스트
            max_length: 참고 자료 최대 길이 (토큰 제한)

        Returns:
            증강된 프롬프트
        """
        if not rag_results:
            return user_prompt

        # 참고 자료 섹션 구성
        context_parts = ["[참고 자료 - 기존 시나리오 예시]"]
        current_len = 0

        for i, result in enumerate(rag_results, 1):
            content = result['content']
            score = result['score']

            # 토큰 수 추정 (한글 1자 ≈ 1.5 토큰)
            estimated_tokens = len(content) * 1.5
            if current_len + estimated_tokens > max_length:
                break

            context_parts.append(f"\n{i}. (유사도: {score:.2f})")
            context_parts.append(content)
            current_len += estimated_tokens

        # 최종 프롬프트 조립
        augmented = "\n".join(context_parts)
        augmented += f"\n\n[생성 요청]\n{user_prompt}\n"
        augmented += """
위 참고 자료의 스타일, 톤, 난이도를 참고하여 일관성 있는 새로운 시나리오를 생성하세요.
참고 자료를 그대로 복사하지 말고, 유사한 형식과 품질로 창의적인 내용을 만들어주세요.
"""
        return augmented

    async def generate_scenario_with_presigned(
        self,
        *,
        prompt: str,
        question_count: int,
        options_per_question: int,
        category_id: int,
        image_size: str = "1024x1024",
        option_image_size: str = "1024x1024",
        use_rag: bool = True,
        rag_top_k: int = 5,
    ) -> Tuple[Dict[str, Any], Dict[str, Any]]:

        # ===== 1) RAG 검색 (옵션) =====
        rag_sources = []
        if use_rag:
            try:
                print(f"[RAG] 검색 시작: '{prompt[:50]}...', category={category_id}")
                rag_results = await self.rag.retrieve(
                    query=prompt,
                    category_id=category_id,
                    top_k=rag_top_k,
                )

                if rag_results:
                    # 프롬프트 증강
                    augmented_prompt = await self._build_augmented_prompt(
                        prompt, rag_results
                    )
                    prompt = augmented_prompt  # 증강된 프롬프트로 교체

                    rag_sources = [
                        {
                            "scenario_id": r["metadata"].get("scenario_id"),
                            "score": r["score"],
                            "chunk_type": r["metadata"].get("chunk_type"),
                        }
                        for r in rag_results
                    ]
                    print(f"[RAG] ✓ {len(rag_results)}개 참고 자료 발견, 프롬프트 증강 완료")
                else:
                    print("[RAG] 검색 결과 없음, 순수 LLM 사용")

            except Exception as e:
                print(f"[RAG] 검색 실패: {e}, 순수 LLM 사용")

        # ===== 2) 텍스트 시나리오 생성/검증 =====
        text = await self.llm.generate_scenario_text(
            prompt=prompt,
            question_count=question_count,
            options_per_question=options_per_question,
            category_id=category_id,
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

        # 6) 최종 JSON (RAG 소스 추가)
        scenario = {
            "title": text["title"],
            "summary": text["summary"],
            "categoryId": category_id,
            "thumbnailS3Key": thumb_ps["fileName"],
            "backgroundS3Key": bg_ps["fileName"],
            "total_sequences": len(text["sequences"]),
            "sequences": text["sequences"],
            "ragSources": rag_sources,  # RAG 참고 자료 정보
        }
        return scenario, {"uploaded": uploaded, "failedRefs": failed}
