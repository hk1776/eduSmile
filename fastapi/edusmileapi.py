from fastapi import FastAPI, File, UploadFile
import tempfile


app = FastAPI()

@app.get("/edusmile")
async def get_edusmile(file: UploadFile = File(...)):
    classsummary,notice,task,test=1,1,1,1
    return {"수업요약":classsummary,"공지사항": notice, "과제/준비물": task,"시험": test}

