import os
from dotenv import load_dotenv
from typing import AsyncGenerator
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker

# 환경 변수 로드
load_dotenv()

USER = os.getenv("DB_USERNAME")
PASS = os.getenv("DB_PASSWORD")
HOST = os.getenv("DB_HOST")
PORT = os.getenv("DB_PORT")
NAME = os.getenv("DB_NAME")

DATABASE_URL = f"mysql+asyncmy://{USER}:{PASS}@{HOST}:{PORT}/{NAME}"

engine = create_async_engine(
    DATABASE_URL,
    pool_pre_ping=True,
    pool_recycle=1800,
    pool_size=5,
    max_overflow=10,
    echo=os.getenv("DB_ECHO", "false").lower() == "true",
    future=True,
)

AsyncSessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    autoflush=False,
    expire_on_commit=False,
)

async def get_session() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        yield session

async def close_db():
    await engine.dispose()