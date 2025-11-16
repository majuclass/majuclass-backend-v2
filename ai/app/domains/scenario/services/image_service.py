# app/domains/scenario/services/image_service.py
import os
import base64
import httpx
import logging
from typing import List, Literal

log = logging.getLogger(__name__)

# === 환경 변수 ===
GMS_KEY = os.getenv("GMS_KEY")
GMS_IMAGE_BASE = os.getenv("GMS_IMAGE_BASE")
GMS_BASE = os.getenv("GMS_BASE")
IMAGE_STUB = os.getenv("IMAGE_STUB", "0") == "1"

# 실제 호출할 엔드포인트 결정
GMS_IMAGE_ENDPOINT = GMS_IMAGE_BASE or f"{GMS_BASE}/images/generations"

# DALL·E 3가 공식적으로 지원하는 대표 사이즈
SupportedSize = Literal["1024x1024", "1024x1792", "1792x1024"]


def _coerce_size_for_dalle(size: str) -> str:
    """
    DALL·E 3가 지원하지 않는 사이즈(예: 512x512)가 오면 안전하게 1024x1024로 보정.
    """
    allowed = {"1024x1024", "1024x1792", "1792x1024"}
    if size not in allowed:
        log.warning("DALL·E 3은 '%s' 미지원 → '1024x1024'로 보정합니다.", size)
        return "1024x1024"
    return size


class ImageService:
    """
    DALL·E 3 (GMS 프록시)로 이미지 bytes를 생성한다.
    - 업로드는 하지 않음 (업로드는 s3_service가 처리)
    - 기본 포맷: PNG (S3 presign 시 contentType='image/png' 권장)
    """

    # 1x1 투명 PNG (스텁 모드용)
    _PLACEHOLDER_PNG_B64 = (
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/xcAAnsB9Xw1ZmgAAAAASUVORK5CYII="
    )

    def __init__(self, timeout: float = 120.0):
        if not GMS_KEY:
            raise RuntimeError("GMS_KEY is missing in environment")
        self._timeout = timeout
        self._headers = {
            "Authorization": f"Bearer {GMS_KEY}",
            "Content-Type": "application/json",
        }

    # ---------- 프롬프트 빌더 (선택) ----------
    @staticmethod
    def build_option_prompt(question: str, option_text: str) -> str:
        """
        하 난이도(이미지 선택)용 옵션 이미지 지침:
        - 텍스트 없는 플랫 일러스트/아이콘
        - 상황/선택지를 연상시키되 과도한 디테일 지양
        """
        style = (
            "flat illustration, icon-like, no text on image, high contrast, "
            "simple shapes, kid-friendly, clean background"
        )
        return f"{question} 상황에서 '{option_text}'를 상징하는 소품/아이콘, {style}"

    @staticmethod
    def build_cover_prompt(scenario_prompt: str, kind: Literal["thumbnail", "background"]) -> str:
        """
        썸네일/배경 공통 지침: 텍스트 없이 깔끔하고 대비 높은 플랫 일러스트
        """
        tag = "표지 일러스트" if kind == "thumbnail" else "배경 일러스트"
        style = "flat illustration, no text, clean background, high contrast"
        return f"{scenario_prompt} {tag}, {style}"

    # ---------- 생성기 ----------
    async def generate_bytes(
        self,
        prompt: str,
        *,
        size: SupportedSize | str = "1024x1024",
        n: int = 1,
    ) -> bytes | List[bytes]:
        """
        DALL·E 3로 이미지를 생성해 bytes를 반환한다.
        - n=1이면 bytes, n>1이면 List[bytes]
        - 기본 size는 1024x1024 (옵션: 1024x1792, 1792x1024)
        - 512x512 등 미지원 사이즈가 들어오면 1024x1024로 자동 보정
        """
        # 스텁 모드: 즉시 통과용 (E2E 검증이나 업스트림 불안정시)
        if IMAGE_STUB:
            png = base64.b64decode(self._PLACEHOLDER_PNG_B64)
            return png if n == 1 else [png] * n

        size = _coerce_size_for_dalle(str(size))

        payload = {
            "model": "dall-e-3",
            "prompt": prompt,
            "size": size,
            "n": n,
            "response_format": "b64_json",
        }

        async with httpx.AsyncClient(timeout=self._timeout) as client:
            try:
                resp = await client.post(GMS_IMAGE_ENDPOINT, headers=self._headers, json=payload)
                resp.raise_for_status()
            except httpx.HTTPStatusError as e:
                body = e.response.text if e.response is not None else "<no body>"
                log.error(
                    "DALL·E API 오류 %s\nURL: %s\nREQ: %s\nRESP: %s",
                    getattr(e.response, "status_code", "?"),
                    GMS_IMAGE_ENDPOINT,
                    payload,
                    body,
                )
                raise

        data = resp.json()
        if "data" not in data or not data["data"]:
            raise RuntimeError(f"DALL·E 3 empty response: {data}")

        images = [base64.b64decode(item["b64_json"]) for item in data["data"]]
        return images[0] if n == 1 else images
