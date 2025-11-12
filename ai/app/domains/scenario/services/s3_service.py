# app/domains/scenario/services/s3_service.py
import os, httpx
from uuid import uuid4
from enum import Enum
from dotenv import load_dotenv

load_dotenv()  # 임포트/테스트 시에도 .env 읽도록

class ScenarioImageType(str, Enum):
    THUMBNAIL = "scenarios/thumbnails"
    BACKGROUND = "scenarios/backgrounds"
    OPTION = "scenarios/options"

def _ext(ct: str) -> str:
    ct = (ct or "").lower()
    if ct in ("image/jpeg","image/jpg"): return ".jpg"
    if ct == "image/png": return ".png"
    if ct == "image/gif": return ".gif"
    if ct == "image/webp": return ".webp"
    return ".jpg"

def generate_scenario_image_key(img_type: ScenarioImageType, content_type: str) -> str:
    return f"{img_type.value}/{uuid4()}{_ext(content_type)}"

def _presign_endpoint() -> str:
    ep = os.getenv("LAMBDA_PRESIGNED_URL_API")
    if not ep:
        raise RuntimeError("LAMBDA_PRESIGNED_URL_API not set")
    return ep

async def get_presigned_upload(file_name: str, content_type: str = "image/png") -> dict:
    payload = {"fileName": file_name, "operation": "putObject", "contentType": content_type}
    async with httpx.AsyncClient(timeout=15) as c:
        r = await c.post(_presign_endpoint(), json=payload)
        r.raise_for_status()
        data = r.json()
        if "url" not in data or "fileName" not in data:
            raise RuntimeError(f"Invalid presign response: {data}")
        return {"uploadUrl": data["url"], "fileName": data["fileName"], "contentType": content_type}

async def put_presigned(url: str, data: bytes, content_type: str = "image/png"):
    async with httpx.AsyncClient(timeout=120) as c:
        r = await c.put(url, content=data, headers={"Content-Type": content_type})
        if r.status_code < 200 or r.status_code >= 300:
            raise RuntimeError(f"PUT failed {r.status_code}: {r.text}")