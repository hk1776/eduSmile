from fastapi import FastAPI, File, UploadFile
from pydantic import BaseModel
import os
from pathlib import Path
from typing import Dict
from ai_models.model_pipline import EduContentProcessor, process_audio_file, load_api_keys

app = FastAPI()

# API 키 파일 경로
base_folder = os.path.dirname(os.path.abspath(__file__))

# API 키 로드
config = load_api_keys(base_folder)

# EduContentProcessor 객체 초기화
processor = EduContentProcessor(
    config['openai_api_key'],
    config['claude_api_key'],
    config['base_folder']
)

@app.post("/process_audio/")
async def process_audio(file: UploadFile = File(...)):
    """업로드된 오디오 파일을 처리하여 공지사항, 수업 내용, 해설을 반환합니다."""
    
    try:
        # 임시 파일로 오디오 저장
        audio_path = Path(base_folder) / "temp_audio.wav"
        with open(audio_path, "wb") as buffer:
            buffer.write(await file.read())
        
        # 오디오 파일 처리
        results = process_audio_file(processor, audio_path)
        
        # 임시 파일 삭제
        os.remove(audio_path)
        
        return results
        
    except Exception as e:
        return {"error": str(e)}


# 실행 방법: 
# uvicorn ai_models.fastapi:app --reload
