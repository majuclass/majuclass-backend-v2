import logging

from dotenv import load_dotenv
from fastapi import FastAPI
from starlette.middleware.cors import CORSMiddleware

from app.common.api_response import ApiResponse
from app.domains.auth.middleware import AuthMiddleware
from app.domains.speech_to_text.stt_router_v2 import router as stt_router_v2
from app.domains.text_to_speech.tts_router import router as tts_router
from app.domains.scenario.scenario_router import router as scenario_router

# 환경 변수 로드
load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(levelname)s %(asctime)s %(name)s: %(message)s"
)

# FastAPI 앱 인스턴스 생성
app = FastAPI(
    title="AI Service API",
    description="음성 인식 및 텍스트 분석 API 서비스",
    version="1.0.0"
)

# 프런트/게이트웨이가 실제로 띄워지는 Origin들을 정확히 나열
ALLOWED_ORIGINS = [
    "https://www.majuclass.com",
    "https://majuclass.com",
    "http://localhost:5173",
    "http://localhost:4173",
]

app.add_middleware(AuthMiddleware)

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 구 버전 STT 라우터
# app.include_router(stt_router)
app.include_router(stt_router_v2)

# TTS 라우터
app.include_router(tts_router, prefix="/ai", tags=["TTS"])


# 시나리오 라우터는 항상 노출
app.include_router(scenario_router)

@app.get("/")
def root():
    return {"message": "AI Service API is running"}

@app.get(
    "/health",
    summary="서비스 상태 확인",
    description="STT 서비스가 정상 작동하는지 확인합니다."
)
async def health_check():
    return ApiResponse.success(
        message="서비스가 정상 작동 중입니다",
    )