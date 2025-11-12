# app/main.py (시나리오 생성 test용)
from pathlib import Path
from dotenv import load_dotenv

# 레포 루트의 .env를 확실히 로드 (import 전에!)
ENV_PATH = Path(__file__).resolve().parents[2] / ".env"
load_dotenv(dotenv_path=ENV_PATH)

import os
import logging
from fastapi import FastAPI
from starlette.middleware.cors import CORSMiddleware

from app.domains.auth.middleware import AuthMiddleware
from app.domains.scenario.scenario_router import router as scenario_router
from app.common.api_response import ApiResponse

logging.basicConfig(
    level=logging.INFO,
    format="%(levelname)s %(asctime)s %(name)s: %(message)s"
)

app = FastAPI(
    title="AI Service API",
    description="음성/STT + 시나리오 생성 API",
    version="1.0.0"
)

ALLOWED_ORIGINS = [
    "https://www.majuclass.com",
    "https://majuclass.com",
    "http://localhost:5173",
    "http://localhost:4173",
]

# 미들웨어가 app.state.AUTH_WHITELIST를 읽도록 구현
app.state.AUTH_WHITELIST = {
    "/", "/health",
    "/ai/stt-analyze",
    "/ai/scenario/generate",
}

app.add_middleware(AuthMiddleware)
app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 시나리오 라우터는 항상 노출
app.include_router(scenario_router)

# STT/WS 라우터는 환경변수로 토글
ENABLE_STT = os.getenv("ENABLE_STT", "true").lower() in ("1", "true", "yes")
if ENABLE_STT:
    # (주의) 이 import가 유사도 모델 로딩을 유발하므로 조건부로 둡니다.
    from app.domains.speech_to_text.stt_router import router as stt_router
    from app.domains.speech_to_text.websocket_router import router as websocket_router
    app.include_router(stt_router)
    app.include_router(websocket_router)

# 디버그
logging.info("LAMBDA_PRESIGNED_URL_API loaded? %s", bool(os.getenv("LAMBDA_PRESIGNED_URL_API")))

@app.get("/")
def root():
    return {"message": "AI Service API is running"}

@app.get("/health")
async def health():
    return ApiResponse.success("서비스가 정상 작동 중입니다")


# from fastapi import FastAPI
# from dotenv import load_dotenv
# # 환경 변수 로드
# load_dotenv()
# from starlette.middleware.cors import CORSMiddleware

# from app.domains.auth.middleware import AuthMiddleware
# from app.domains.speech_to_text.stt_router import router as stt_router
# from app.domains.speech_to_text.websocket_router import router as websocket_router
# from app.domains.scenario.scenario_router import router as scenario_router
# from app.common.api_response import ApiResponse
# import logging



# logging.basicConfig(
#     level=logging.INFO,
#     format="%(levelname)s %(asctime)s %(name)s: %(message)s"
# )

# # FastAPI 앱 인스턴스 생성
# app = FastAPI(
#     title="AI Service API",
#     description="음성 인식 및 텍스트 분석 API 서비스",
#     version="1.0.0"
# )

# # 프런트/게이트웨이가 실제로 띄워지는 Origin들을 정확히 나열
# ALLOWED_ORIGINS = [
#     "https://www.majuclass.com",
#     "https://majuclass.com",
#     "http://localhost:5173",
#     "http://localhost:4173",
# ]

# app.add_middleware(AuthMiddleware)

# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=ALLOWED_ORIGINS,
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )

# app.include_router(stt_router)
# app.include_router(websocket_router)
# app.include_router(scenario_router)   

# @app.get("/")
# def read_root():
#     return {"message": "AI Service API is running"}

# @app.get(
#     "/health",
#     summary="서비스 상태 확인",
#     description="STT 서비스가 정상 작동하는지 확인합니다."
# )
# async def health_check():
#     return ApiResponse.success(
#         message="서비스가 정상 작동 중입니다",
#     )