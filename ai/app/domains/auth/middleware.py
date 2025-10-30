import os

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from jose import jwt, JWTError
from starlette.middleware.base import BaseHTTPMiddleware

app = FastAPI()

SECRET_KEY = os.getenv("JWT_SECRET")
ALGORITHM = "HS256"

class AuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):

        public_path = [
            "/docs",
            "/openapi.json",  # OpenAPI 스키마
            "/redoc",  # ReDoc
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
            payload = jwt.decode(token, SECRET_KEY, algorithms=[Algorithm])

            token_type = payload.get("token_type")
            if token_type == "refresh":
                return JSONResponse(
                    status_code=403,
                    content={
                        "status" : "ERROR",
                        "message" : "Refresh 토큰으로 접근할 수 없습니다."
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
