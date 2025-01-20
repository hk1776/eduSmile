from fastapi import FastAPI, File, UploadFile, HTTPException
from pydantic import BaseModel
import os
from pathlib import Path
from typing import Dict
from ai_models.model_pipline import EduContentProcessor, process_audio_file,process_text_file, load_api_keys

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
ALLOWED_EXTENSIONS1 = {"mp3", "wav", "flac"}

# 파일 확장자 검증 함수
def validate_audio_file(file: UploadFile):
    filename = file.filename
    file_extension = filename.split(".")[-1].lower()
    return file_extension
    
    # 파일 확장자가 허용된 오디오 형식인지 확인
    if file_extension not in ALLOWED_EXTENSIONS1:
        raise HTTPException(status_code=400, detail="Uploaded file is not a valid audio file")

ALLOWED_EXTENSIONS2 = {"pdf"}

# 파일 확장자 검증 함수
def validate_pdf_file(file: UploadFile):
    filename = file.filename
    file_extension = filename.split(".")[-1].lower()
    
    # 파일 확장자가 허용된 pdf 형식인지 확인
    if file_extension not in ALLOWED_EXTENSIONS2:
        raise HTTPException(status_code=400, detail="Uploaded file is not a valid pdf file")
class TextRequest(BaseModel):
    text: str
    
@app.post("/process_audio/")
async def process_audio(file: UploadFile = File(...)):
    """업로드된 오디오 파일을 처리하여 공지사항, 수업 내용, 해설을 반환합니다."""
    
    try:
        validate_audio_file(file)

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
    
@app.post("/process_text/")
async def process_text(request: TextRequest):
    """
    업로드된 오디오 파일을 처리하여 공지사항, 수업 내용, 해설을 반환합니다.
    """
    try:
        # 오디오 파일 처리
        results = process_text_file(processor, request.text)
        return results
    except Exception as e:
        return {"error": str(e)}
        
@app.post("/process_pdf/")
async def process_audio(file: UploadFile = File(...)):
    """업로드된 PDF파일을 처리합니다다."""

    try:
        validate_pdf_file(file)

        results = 1
        
        return results
        
    except Exception as e:
        return {"error": str(e)}

# 실행 방법: 
# uvicorn ai_models.fastapi:app --reload
