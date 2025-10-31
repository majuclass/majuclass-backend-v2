import os

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from jose import jwt, JWTError
from starlette.middleware.base import BaseHTTPMiddleware
from sqlalchemy import text

from app.database import AsyncSessionLocal

app = FastAPI()

SECRET_KEY = os.getenv("JWT_SECRET")
ALGORITHM = "HS256"

async def is_valid_user(user_id: int) -> bool:
    try:
        async with AsyncSessionLocal() as session:
            result = await session.execute(
                text("SELECT id FROM users WHERE id = :user_id AND is_deleted IS false"),
                {"user_id": user_id}
            )
            return result.scalar_one_or_none() is not None
    except Exception as e :
        print(f"DB 조회 오류: {e}")
        return False

class AuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):

        public_path = [
            "/docs",
            "/openapi.json",
            "/redoc",
            "/health",
        ]

        if request.url.path in public_path:
            return await call_next(request)

        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return JSONResponse(
                status_code=401,
                content={
                    "status" : "ERROR",
                    "message" : "토큰이 존재하지 않습니다."
                }
            )

        token = auth_header.split(" ")[1]

        try :
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])

            token_type = payload.get("token_type")
            if token_type == "refresh":
                return JSONResponse(
                    status_code=403,
                    content={
                        "status" : "ERROR",
                        "message" : "Refresh 토큰으로 접근할 수 없습니다."
                    }
                )

            user_id = payload.get("sub")
            if not user_id:
                return JSONResponse(
                    status_code=401,
                    content={
                        "status" : "ERROR",
                        "message" : "유효하지 않은 토큰입니다."
                    }
                )

            if not await is_valid_user(int(user_id)):
                return JSONResponse(
                    status_code=401,
                    content={
                        "status" : "ERROR",
                        "message" : "유효하지 않은 사용자입니다."
                    }
                )
        except JWTError :
            return JSONResponse(
                status_code=401,
                content={
                    "status" : "ERROR",
                    "message" : "잘못된 토큰입니다."
                }
            )

        response = await call_next(request)
        return response

app.add_middleware(AuthMiddleware)
