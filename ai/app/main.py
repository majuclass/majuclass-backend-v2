from fastapi import FastAPI

# FastAPI 앱 인스턴스 생성
app = FastAPI()

# 루트 경로 엔드포인트
@app.get("/")
def read_root():
    return {"message": "Hello World"}