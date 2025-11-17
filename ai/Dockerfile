# ========================================
# Multi-stage build for FastAPI AI Service
# Optimized for production with security best practices
# ========================================

# ========================================
# Stage 1: Base Image with System Dependencies
# ========================================
FROM python:3.11-slim AS base

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

WORKDIR /app

# Install system dependencies (minimal set for production)
# - build-essential: Required for building Python packages with C extensions
# - curl: For healthchecks
# - libglib2.0-0, libsm6, libxext6, libxrender1: Required for certain ML libraries
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    ca-certificates \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# ========================================
# Stage 2: Dependencies Builder
# ========================================
FROM base AS builder

# Copy requirements file
COPY requirements.txt .

# Build wheels for all dependencies with pip cache mount
# This allows Docker BuildKit to cache downloaded packages
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install --upgrade pip setuptools wheel && \
    pip wheel --wheel-dir /wheels -r requirements.txt

# ========================================
# Stage 3: Model Downloader
# ========================================
FROM base AS model-downloader

# Copy wheels from builder
COPY --from=builder /wheels /wheels
COPY requirements.txt .

# Install all dependencies from pre-built wheels
RUN pip install --no-index --find-links=/wheels -r requirements.txt && \
    rm -rf /wheels

# Pre-download the Korean sentence transformer model
# This ensures the model is baked into the image for faster startup
RUN python -c "\
from sentence_transformers import SentenceTransformer; \
import logging; \
logging.basicConfig(level=logging.INFO); \
logger = logging.getLogger(__name__); \
logger.info('Downloading Korean SentenceTransformer model...'); \
#model = SentenceTransformer('jhgan/ko-sroberta-multitask'); \
model = SentenceTransformer('snunlp/KR-SBERT-V40K-klueNLI-augSTS'); \
logger.info('Model download complete and cached'); \
"

# ========================================
# Stage 4: Production Runtime
# ========================================
FROM base AS production

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy wheels from builder
COPY --from=builder /wheels /wheels
COPY requirements.txt .

# Install all dependencies from pre-built wheels
RUN pip install --no-index --find-links=/wheels -r requirements.txt && \
    rm -rf /wheels

# Copy pre-downloaded model cache from model-downloader stage
COPY --from=model-downloader /root/.cache/huggingface /home/appuser/.cache/huggingface

# Copy application code
COPY --chown=appuser:appuser . .

# Create necessary directories with proper permissions
RUN mkdir -p /app/data/chroma_db && \
    chown -R appuser:appuser /app/data && \
    chown -R appuser:appuser /home/appuser/.cache

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

# Run the application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "1"]
