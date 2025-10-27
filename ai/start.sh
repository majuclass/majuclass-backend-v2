#!/usr/bin/env bash
set -e
if [ -f ".env" ]; then
  export $(grep -v '^#' .env | xargs)
fi
# 아래 uvicorn 대상은 너 레포의 실제 엔트리로 맞춰줘
# 예시 A) 패키지형: app.main:app
# 예시 B) 루트에 main.py: main:app
exec uvicorn main:app --host "${APP_HOST:-0.0.0.0}" --port "${APP_PORT:-8001}" --proxy-headers