# Python 3.11 기반 이미지 (Debian Bookworm)
FROM python:3.11-slim-bookworm

# 1. 시스템 패키지 설치 전 필수 작업
RUN apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get update -y --fix-missing

# 2. 필수 시스템 패키지 설치
RUN apt-get install -y \
    software-properties-common \
    apt-transport-https \
    ca-certificates \
    && apt-get update

# 3. 그래픽 및 PDF 처리 핵심 패키지
RUN apt-get install -y \
    poppler-utils \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    && apt-get clean

# 작업 디렉토리 설정
WORKDIR /app

# pip, setuptools 최신화
RUN python -m pip install --upgrade pip setuptools wheel

# requirements.txt 복사
COPY requirements.txt /tmp/

# 나머지 의존성 설치
RUN pip install --no-cache-dir -r /tmp/requirements.txt

# 애플리케이션 코드 복사
COPY . /app

# 포트 노출
EXPOSE 8000

# FastAPI 애플리케이션 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload", "--log-level", "info"]