# Python 3.9 이미지 사용
FROM python:3.9-slim

# 시스템 패키지 설치
RUN apt-get update && apt-get install -y \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 필요한 파일들 복사
COPY requirements.txt .
COPY ./ai_models ./ai_models

# Python 패키지 설치
RUN pip install --no-cache-dir -r requirements.txt

# FastAPI 애플리케이션을 위한 포트 설정
EXPOSE 8000

# 애플리케이션 실행
CMD ["uvicorn", "ai_models.app:app", "--host", "0.0.0.0", "--port", "8000"]