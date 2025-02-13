from google.cloud import vision,documentai
import os
from pdf2image import convert_from_path
import io
from anthropic import Anthropic
from datetime import datetime
from pathlib import Path
import json
import time
from PyPDF2 import PdfReader, PdfWriter

class PDFProcessor:
    #기본설정
    def __init__(self, api_key: str,base_folder: str,output_folder: str = "pdf2summary"):
        """PDF와 학생 정보를 처리하는 클래스"""
        self.output_folder = Path(output_folder)
        self.api_key = api_key
        self.claude_client = Anthropic(api_key=self.api_key)
        self.base_folder = Path(base_folder)
        self._create_folders()
        self.last_api_call = 0
        self.min_delay = 1
    # pdf를 분할하는 함수
    def split_pdf(self,input_pdf_path: str) -> str:
        output_path = self.base_folder / "pdf2json"
        """PDF 파일을 15페이지씩 분할"""
        reader = PdfReader(input_pdf_path)
        total_pages = len(reader.pages)

        os.makedirs(output_path, exist_ok=True)

        split_files = []
        for i in range(0, total_pages, 15):
            writer = PdfWriter()
            for j in range(i, min(i + 15, total_pages)):
                writer.add_page(reader.pages[j])

            split_file_path = os.path.join(output_path, f"split_{i // 15 + 1}.pdf")
            with open(split_file_path, "wb") as output_pdf:
                writer.write(output_pdf)
            split_files.append(split_file_path)

        return split_files
    
    #OCR를 활용하여 pdf파일을 json파일로 저장
    def extract_text_from_pdf(self,pdf_path: str) -> str:
        project_id = "core-appliance-448400-j0"  # GCP 프로젝트 ID
        location = "us"  # Document AI 위치
        processor_id = "88019ccdcd45082a"  # 프로세서 ID

        client = documentai.DocumentProcessorServiceClient()
        name = f"projects/{project_id}/locations/{location}/processors/{processor_id}"

        with open(pdf_path, "rb") as file:
            pdf = file.read()

        document = {"content": pdf, "mime_type": "application/pdf"}  # PDF 파일
        request = {"name": name, "raw_document": document}

        response = client.process_document(request=request)

        extracted_data = {
            "text": response.document.text,
            "entities": [
                {"type": entity.type_, "value": entity.mention_text}
                for entity in response.document.entities
            ]
        }

        return extracted_data

    def process_large_pdf(self,input_pdf_path: str) -> str:
        output_path = self.base_folder / "pdf2json"
        """큰 PDF를 분할 처리하고 결과 병합"""
        split_dir = os.path.join(output_path, "split_pdfs")
        os.makedirs(output_path, exist_ok=True)

        # PDF를 분할
        split_files = self.split_pdf(input_pdf_path)

        # 분할된 PDF 처리
        merged_text = ""
        merged_entities = []
        for split_file in split_files:
            extracted_data = self.extract_text_from_pdf(split_file)
            merged_text += extracted_data["text"] + "\n"
            merged_entities.extend(extracted_data["entities"])

        # 최종 병합 데이터 저장
        final_data = {
            "text": merged_text,
            "entities": merged_entities
        }
        output_file = os.path.join(output_path, "final_extracted_data.json")
        with open(output_file, "w", encoding="utf-8") as json_file:
            json.dump(final_data, json_file, ensure_ascii=False, indent=4)

        print(f"모든 데이터를 병합하여 저장했습니다: {output_file}")
        return final_data["text"]

    def _create_folders(self):
        """필요한 폴더를 생성합니다."""
        self.output_folder.mkdir(parents=True, exist_ok=True)

    def _generate_filename(self, original_filename: str) -> str:
        """결과 파일명을 생성합니다."""
        date_str = datetime.now().strftime('%Y%m%d_%H%M%S')
        original_name = Path(original_filename).stem
        return f"summary_{original_name}_{date_str}"

    def _respect_rate_limit(self):
        """API 호출 간격을 조절합니다."""
        current_time = time.time()
        time_since_last_call = current_time - self.last_api_call
        if time_since_last_call < self.min_delay:
            time.sleep(self.min_delay - time_since_last_call)
        self.last_api_call = time.time()

    def get_claude_response(self, prompt: str) -> str:
        """Claude API를 호출하여 응답을 받습니다."""
        self._respect_rate_limit()
        try:
            response = self.claude_client.messages.create(
                model="claude-3-5-haiku-20241022",
                max_tokens=2000,
                temperature=0,
                messages=[
                    {
                        "role": "user",
                        "content": prompt
                    }
                ]
            )
            if isinstance(response.content, list):
                if len(response.content) > 0 and hasattr(response.content[0], 'text'):
                    return response.content[0].text
                return ' '.join(str(item) for item in response.content)
            elif hasattr(response.content, 'text'):
                return response.content.text
            return str(response.content)
        except Exception as e:
            print(f"API 호출 중 오류 발생: {str(e)}")
            raise

    def analyze_student_info(self, text_content: str) -> dict:
        """Claude API를 사용하여 학생 정보를 분석합니다."""
        prompt = f"""
        다음 규칙을 준수하여 작성해주세요.
        다음 텍스트에서 학생 정보를 추출하여 JSON 형식으로 정리해주세요.
        반드시 아래의 JSON 형식만을 사용하여 응답해주세요.
        몇학년때 진행한 것인지 알려주세요.
        
        정리한 내용을 기반으로 직업을 몇개 추천해주시고 그 직업을 추천한 이유를 2줄이내로 작성해주세요
        추천해준 직업에 관련된 학과를 추천해주세요
        추천해준 직업에 관련된 자격증을 추천해주세요
        추천해준 직업에 가지기위해 해야되는 노력을 각각 1줄이상 작성해주세요
        수상경력은 Awards 자격증 및 인증 취득상황은 Certifications 진로희망사항은 CareerAspiration 창의적 체험활동상황은 CreativeExperienceActivities 과목 세부능력 및 특기사항은 SubjectSpecialtyAndNotes 독서활동상황은 ReadingActivities 행동특성 및 종합의견은 BehaviorCharacteristicsAndOverallComments로 추천직업은 RecommendedJobs로 바꿔서 만들어주세요
        JSON 형식 : 
        {{
            "수상경력": ["특기사항1",
                        "특기사항2", 
                        ...],
            "자격증 및 인증 취득상황": ["진로1",
                                        "진로2", 
                                        ...],
            "진로희망사항": ["진로희망사항1",
                            "진로희망사항2",
                            ...],
            "창의적 체험활동상황": ["창의적 체험활동상황1", 
                                    "창의적 체험활동상황2", 
                                    ...],
            "과목 세부능력 및 특기사항": [
                                    "과목 세부능력 및 특기사항1",
                                    "과목 세부능력 및 특기사항2",
                                    ...],
            "독서활동상황": [
                            "독서활동상황1",
                            "독서활동상황2",
                            ...],
            "행동특성 및 종합의견": [
                                    "행동특성 및 종합의견1",
                                    "행동특성 및 종합의견2",
                                    ...],
            "추천직업": {{
                "추천직업1" : [
                                "추천이유" : ["추천이유"],
                                "직업관련학과" : ["직업관련학과1",
                                                "직업관련학과2", 
                                                ...],
                                "직업관련자격증" : ["직업관련자격증1",
                                                    "직업관련자격증2",
                                                    ...
                                                    ],
                                "노력" : ["노력1",
                                        "노력2",
                                        ...],
                "추천직업2" : [
                                "추천이유" : ["추천이유"],
                                "직업관련학과" : ["직업관련학과1",
                                                "직업관련학과2", 
                                                ...],
                                "직업관련자격증" : ["직업관련자격증1",
                                                    "직업관련자격증2",
                                                    ...
                                                    ],
                                "노력" : ["노력1",
                                        "노력2",
                                        ...],
                ...
            }}
        }}
        
        텍스트 내용:
        {text_content}
        
       
        """

        try:
            response_text = self.get_claude_response(prompt)
            json_start = response_text.find('{')
            json_end = response_text.rfind('}') + 1
            
            if json_start == -1 or json_end == 0:
                raise ValueError("JSON 형식을 찾을 수 없습니다")
                
            json_str = response_text[json_start:json_end]
            info = json.loads(json_str)
            
            if not isinstance(info, dict):
                raise ValueError("잘못된 JSON 구조")
            
            return info
            
        except json.JSONDecodeError as e:
            print(f"JSON 파싱 오류: {str(e)}")
            print(f"응답 텍스트: {response_text}")
            raise
        except Exception as e:
            print(f"정보 분석 중 오류 발생: {str(e)}")
            raise

    def process_pdf(self, pdf_path: str) -> dict:
        """PDF를 처리하고 결과를 반환환합니다."""
        try:
            # PDF에서 텍스트 추출
            print("PDF에서 텍스트 추출 중...")
            extracted_text = self.process_large_pdf(pdf_path)
            print("텍스트 추출 완료")

            # Claude API를 사용하여 정보 분석
            print("\n텍스트 분석 중...")
            student_info = self.analyze_student_info(extracted_text)

            return {
                "status": "success",
                "summary": student_info
            }

        except Exception as e:
            return {
                "status": "error",
                "error": str(e)
            }

    #저장할 필요없이 바로 return해줘 사용하지않는코드
    def process_pdf1(self, pdf_path: str) -> dict:
        """PDF를 처리하고 결과를 저장합니다."""
        # 텍스트 파일 경로
        file_path1 = "./직업종류.txt"

        # 빈 딕셔너리 생성
        job_dict = {}

        # 파일 읽어서 딕셔너리에 저장
        with open(file_path1, "r", encoding="utf-8") as file:
            for index, line in enumerate(file, start=1):
                job = line.strip()  # 줄바꿈 제거
                job_dict[index] = job

        # JSON 파일 경로
        file_path2 = "./pdf2json/final_extracted_data.json"

        # JSON 파일 읽고 변수에 저장
        with open(file_path2, "r", encoding="utf-8") as file:
            extracted_text = json.load(file)
        
        
        # Claude API를 사용하여 정보 분석
        print("\n텍스트 분석 중...")
        student_info = self.analyze_student_info(extracted_text)
        print("\n결과저장")
        # 결과 파일 저장
        output_filename = self._generate_filename(os.path.basename(pdf_path))
        output_path = self.output_folder / f"{output_filename}.json"
        
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(student_info, f, ensure_ascii=False, indent=2)

        return {
            "status": "success",
            "파일명": os.path.basename(pdf_path),
            "저장경로": str(output_path),
            "추출된_정보": student_info
        }
