# ---- base ----
FROM python:3.11-slim AS base
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends \
    libglib2.0-0 libsm6 libxext6 libxrender1 gcc curl && \
    rm -rf /var/lib/apt/lists/*

# ---- builder ----
FROM base AS builder
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip wheel --no-cache-dir --wheel-dir /wheels -r requirements.txt

# ---- model-downloader ----
FROM base AS model-downloader
COPY --from=builder /wheels /wheels
COPY requirements.txt .

# 의존성 설치
RUN pip install --no-cache-dir --no-index --find-links=/wheels -r requirements.txt && \
    rm -rf /wheels

# 모델 다운로드 (캐시에 저장됨)
RUN python -c "from sentence_transformers import SentenceTransformer; \
    print('모델 다운로드 시작...'); \
    model = SentenceTransformer('jhgan/ko-sroberta-multitask'); \
    print('모델 다운로드 완료!')"

# ---- final ----
FROM base AS final
COPY --from=builder /wheels /wheels
COPY requirements.txt .

# 의존성 설치
RUN pip install --no-cache-dir --no-index --find-links=/wheels -r requirements.txt && \
    rm -rf /wheels

# 다운로드한 모델 캐시 복사
COPY --from=model-downloader /root/.cache/huggingface /root/.cache/huggingface

# 애플리케이션 코드 복사
COPY . .
EXPOSE 8000