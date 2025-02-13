from fastapi import FastAPI, File, UploadFile, HTTPException
from pydantic import BaseModel
import os
from pathlib import Path
from typing import Dict
from ai_models.model_pipline import EduContentProcessor,process_class_text_file, load_api_keys,process_counsel_text_file
from ai_models.pdf_ocr import PDFProcessor
from ai_models.job_model_claude import CareerAdvisor

app = FastAPI()

# API 키 파일 경로
base_folder = os.path.dirname(os.path.abspath(__file__))
pdf_json_folder=Path(base_folder) / "pdf2json"
os.environ["GOOGLE_APPLICATION_CREDENTIALS"]=str(Path(base_folder)/"ocr_google_api_key.json")
# API 키 로드
config = load_api_keys(base_folder)

# EduContentProcessor 객체 초기화
processor = EduContentProcessor(
    config['openai_api_key'],
    config['claude_api_key'],
    config['base_folder']
)
# PDFProcessor 객체 초기화
processor2 = PDFProcessor(config['claude_api_key'],base_folder,pdf_json_folder)

# CareerAdvisor 객체 초기화
advisor = CareerAdvisor(config['claude_api_key'], base_folder)

# 받아올 text형식 지정
class TextRequest(BaseModel):
    text: str
    
@app.post("/process_calss_text/")
async def process_calss_text(request: TextRequest):
    """
    업로드된 수업 텍스트 파일을 처리하여 공지사항, 수업 내용, 해설을 반환합니다.
    """
    try:
        # 수업 텍스트 파일 처리
        results = process_class_text_file(processor, request.text)
        return results
    except Exception as e:
        return {"error": str(e)}
    
@app.post("/process_counsel_text/")
async def process_counsel_text(request: TextRequest):   
    """
    업로드된 상담 텍스트 파일을 처리하여 공지사항, 수업 내용, 해설을 반환합니다.
    """
    try:
        # 상담 텍스트 파일 처리
        results = process_counsel_text_file(processor, request.text)
        return results
    except Exception as e:
        return {"error": str(e)}


@app.post("/process_pdf/")
async def process_pdf(file: UploadFile = File(...)):
    """업로드된 PDF파일을 처리합니다다."""
    try:
        #받아온 pdf파일 임시저장
        base_folder_path = Path(base_folder)
        pdf_path = base_folder_path / "temp_pdf.pdf"
        print(f"Saving file to: {pdf_path}")
        with open(pdf_path, "wb") as buffer:
            buffer.write(await file.read())

        #받아온 pdf파일 처리후 직업추천
        results = processor2.process_pdf(pdf_path)
        if os.path.exists(pdf_path):
            os.remove(pdf_path)
        
        #추천된 직업을 이용해 가지고있는 직업정보에 있는지 확인 후 없으면 직업정보 생성
        job_recommendations = results.get("summary", {}).get("RecommendedJobs", {}).keys()
        results["job_information"]={}
        for desired_job in job_recommendations:
            results["job_information"][desired_job]=advisor.process(desired_job)
        return results
        
    except Exception as e:
        return {"error": str(e)}
# 실행 방법: 
# uvicorn ai_models.main:app --reload
