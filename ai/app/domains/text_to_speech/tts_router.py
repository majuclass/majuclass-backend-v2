import io
import os
import requests
from dotenv import load_dotenv
from fastapi import APIRouter, HTTPException, Query
from fastapi.responses import StreamingResponse

load_dotenv()

CLOVA_CLIENT_ID = os.getenv("CLOVA_CLIENT_ID")
CLOVA_CLIENT_SECRET = os.getenv("CLOVA_CLIENT_SECRET")
CLOVA_API_URL = "https://naveropenapi.apigw.ntruss.com/tts-premium/v1/tts"

router = APIRouter()

clova_headers = {
    "X-NCP-APIGW-API-KEY-ID": CLOVA_CLIENT_ID or "",
    "X-NCP-APIGW-API-KEY": CLOVA_CLIENT_SECRET or "",
    "Content-Type": "application/x-www-form-urlencoded",
}

@router.get("/tts")
def get_tts(
    text: str = Query(..., description="합성할 텍스트"),
    speaker: str = Query("nara"),
    speed: int = Query(0),
    format: str = Query("mp3"),
):
    if not CLOVA_CLIENT_ID or not CLOVA_CLIENT_SECRET:
        raise HTTPException(status_code=500, detail="CLOVA API credentials are not set.")

    data = {"speaker": speaker, "text": text, "speed": speed, "format": format}

    try:
        resp = requests.post(CLOVA_API_URL, data=data, headers=clova_headers, timeout=30)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"CLOVA request failed: {e}")

    if resp.status_code != 200:
        try:
            details = resp.json()
        except Exception:
            details = {"message": resp.text}
        raise HTTPException(status_code=resp.status_code, detail={"error": "CLOVA TTS failed", "details": details})

    audio_stream = io.BytesIO(resp.content)
    return StreamingResponse(audio_stream, media_type="audio/mpeg",
                             headers={"Content-Disposition": 'inline; filename="speech.mp3"'})
