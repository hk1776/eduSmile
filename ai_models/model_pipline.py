# model_pipeline.py

import os
from pathlib import Path
from anthropic import Anthropic
import openai
import json
from datetime import datetime
import time
from typing import Dict, List, Any

class EduContentProcessor:
    def __init__(self, openai_api_key: str, claude_api_key: str, base_folder: str):
        """통합 교육 컨텐츠 처리 파이프라인"""
        self.openai_api_key = openai_api_key
        self.claude_client = Anthropic(api_key=claude_api_key)
        self.base_folder = Path(base_folder)
        
        # 필요한 폴더 경로 설정
        self.audio_folder = self.base_folder / "audio"
        self.chunks_folder = self.base_folder / "chunks"
        self.text_folder = self.base_folder / "speech2text"
        self.results_folder = self.base_folder / "results"
        
        # 결과 폴더 구조 생성
        self._create_folder_structure()
        
        # API 호출 제어
        self.last_api_call = 0
        self.min_delay = 1
    
    def _create_folder_structure(self):
        """필요한 모든 폴더 구조를 생성합니다."""
        folders = [
            self.chunks_folder,
            self.text_folder,
            self.results_folder / '공지사항',
            self.results_folder / '수업내용',
            self.results_folder / '문제',
            self.results_folder / '해설'
        ]
        for folder in folders:
            folder.mkdir(parents=True, exist_ok=True)
    
    def split_audio(self, audio_path: Path, chunk_length_ms: int = 100000) -> List[Path]:
        """오디오 파일을 작은 청크로 분할합니다."""
        try:
            print(f"오디오 파일 분할 중: {audio_path}")
            audio = AudioSegment.from_file(str(audio_path))
            chunks = [audio[i:i + chunk_length_ms] 
                     for i in range(0, len(audio), chunk_length_ms)]
            
            chunk_files = []
            for idx, chunk in enumerate(chunks):
                chunk_file = self.chunks_folder / f"{audio_path.stem}_chunk_{idx}.wav"
                chunk.export(str(chunk_file), format="wav")
                chunk_files.append(chunk_file)
            
            print(f"분할 완료: {len(chunks)}개의 청크 생성")
            return chunk_files
            
        except Exception as e:
            print(f"오디오 분할 중 오류 발생: {str(e)}")
            raise
    
    def transcribe_chunks(self, chunk_files: List[Path]) -> str:
        """청크 파일들을 텍스트로 변환합니다."""
        try:
            client = openai.OpenAI(api_key=self.openai_api_key)
            transcripts = []
            
            for chunk_file in chunk_files:
                print(f"청크 변환 중: {chunk_file.name}")
                with open(chunk_file, "rb") as audio_file:
                    try:
                        transcript = client.audio.transcriptions.create(
                            model="whisper-1",
                            file=audio_file,
                            language="ko"
                        )
                        transcripts.append(transcript.text)
                        time.sleep(0.5)  # API 호출 간격 조절
                    except Exception as e:
                        print(f"청크 처리 중 오류 발생: {str(e)}")
                        transcripts.append("")
            
            return "\n".join(transcripts)
            
        except Exception as e:
            print(f"텍스트 변환 중 오류 발생: {str(e)}")
            raise
    
    def transcribe_audio(self, audio_path: Path) -> str:
        """WAV 파일을 청크 단위로 처리하여 텍스트로 변환합니다."""
        try:
            # 오디오 파일을 청크로 분할
            chunk_files = self.split_audio(audio_path)
            
            # 청크들을 텍스트로 변환
            combined_text = self.transcribe_chunks(chunk_files)
            
            # 결과 저장
            text_filename = audio_path.stem + "_transcript.txt"
            text_path = self.text_folder / text_filename
            with open(text_path, "w", encoding="utf-8") as f:
                f.write(combined_text)
            
            print(f"변환 완료: {text_path}")
            return str(text_path)
            
        except Exception as e:
            print(f"음성-텍스트 변환 중 오류 발생: {str(e)}")
            raise
        finally:
            # 청크 파일들 정리
            for chunk_file in self.chunks_folder.glob(f"{audio_path.stem}_chunk_*.wav"):
                try:
                    os.remove(chunk_file)
                except Exception as e:
                    print(f"청크 파일 삭제 중 오류 발생: {str(e)}")
    
    def get_claude_response(self, prompt: str) -> str:
        """Claude API를 호출하여 응답을 받습니다."""
        try:
            response = self.claude_client.messages.create(
                model="claude-3-haiku-20240307",
                max_tokens=2000,
                temperature=0.7,
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
            print(f"Claude API 호출 중 오류 발생: {str(e)}")
            raise
    
    
    def get_openai_answer(self, question_text, choices):
        """OpenAI GPT-3.5 Turbo를 이용해 수학 문제의 정답을 검증"""
        prompt = f"""
        다음 객관식 문제의 정답을 구하세요. 반드시 하나의 선택지를 반환하세요.
        
        문제: {question_text}
        선택지: {choices}
        
        답변 형식: 반드시 선택지 중 하나를 그대로 반환하세요.
        """
        try:
            client = openai.OpenAI(api_key=self.openai_api_key)
            response = client.chat.completions.create(
                model="gpt-3.5-turbo",
                messages=[{"role": "user", "content": prompt}]
            )
            return response.choices[0].message.content.strip()
        except Exception as e:
            print(f"🚨 OpenAI API 오류 발생: {str(e)}")
            return None
    
    def validate_answers_with_openai(self, quiz_data):
        """Claude가 생성한 문제에서 OpenAI를 이용하여 정답 검증"""
        for question in quiz_data.get("questions", []):
            correct_index = question.get("correct_answer", -1)
            choices = question.get("choices", [])
            if 0 <= correct_index < len(choices):
                correct_choice = choices[correct_index]
                openai_answer = self.get_openai_answer(question["question"], choices)
                if openai_answer and openai_answer in choices:
                    if openai_answer != correct_choice:
                        print(f"⚠️ 정답 오류 발견! Claude의 정답: {correct_choice}, OpenAI 정답: {openai_answer}")
                        question["correct_answer"] = choices.index(openai_answer)
            
            
    
    
    def generate_quiz(self, content: str, num_questions: int = 5) -> Dict:
        """객관식 문제를 생성합니다."""
        prompt = f"""
        다음 수업 내용을 바탕으로 {num_questions}개의 객관식 문제를 생성해주세요.
        반드시 아래의 JSON 형식만을 사용하여 응답해주세요.
        추가 설명이나 다른 텍스트는 포함하지 마세요.

        {{
            "questions": [
                {{
                    "question": "문제 내용",
                    "choices": ["보기1", "보기2", "보기3", "보기4"],
                    "correct_answer": 0,
                    "explanation": "해설"
                }}
            ]
        }}

        수업 내용:
        {content}
        """
        
        try:
            response_text = self.get_claude_response(prompt)
            json_start = response_text.find('{')
            json_end = response_text.rfind('}') + 1
            
            if json_start == -1 or json_end == 0:
                raise ValueError("JSON 형식을 찾을 수 없습니다")
                
            json_str = response_text[json_start:json_end]
            quiz = json.loads(json_str)
            
            return quiz
            
        except Exception as e:
            print(f"문제 생성 중 오류 발생: {str(e)}")
            return {
                "questions": [
                    {
                        "question": "문제를 생성하는 중 오류가 발생했습니다.",
                        "choices": ["다시 시도해주세요"] * 4,
                        "correct_answer": 0,
                        "explanation": "문제 생성에 실패했습니다."
                    }
                ]
            }
    
    def _generate_filename(self, prefix: str, original_filename: str) -> str:
        """결과 파일명을 생성합니다."""
        date_str = datetime.now().strftime('%Y%m%d_%H%M%S')
        original_name = Path(original_filename).stem
        return f"{prefix}_{original_name}_{date_str}"
    
    def process_content_path(self, text_path: Path) -> Dict:
        """텍스트 컨텐츠를 처리하고 결과물을 생성합니다."""
        try:
            with open(text_path, 'r', encoding='utf-8') as f:
                content = f.read()
            results = {}
            filename = text_path.name
            
            # 공지사항 추출
            notice = self.get_claude_response(f"""
            다음 수업 내용에서 공지사항만을 추출해주세요. 
            공지사항이 없다면 "공지사항이 없습니다."라고만만 답변해주세요.
            
            수업 내용:
            {content}
            """)
            results['notice'] = notice
            
            # 수업내용 요약
            summary = self.get_claude_response(f"""
            다음 수업 내용을 핵심 개념과 중요 포인트 위주로 요약해주세요.
            다른 추가적인 말없이 수업내용만 적어주세요.
            각 개념에 대해 구체적인 예시를 포함해주세요.
            
            수업 내용:
            {content}
            """)
            results['class_summary'] = summary
            
            # 객관식 문제 생성
            quiz = self.generate_quiz(content)
            results['quiz'] = quiz
            
            # 해설 생성
            explanations = {
                "quiz_commentary": [
                    {
                        "quiz_number": i + 1,
                        "quiz": q["question"],
                        "answer": f"{int(q['correct_answer']) + 1}번",
                        "commentary": q["explanation"]
                    }
                    for i, q in enumerate(quiz["questions"])
                ]
            }
            results['quiz_commentary'] = explanations
            
            return results
        except Exception as e:
            print(f"컨텐츠 처리 중 오류 발생: {str(e)}")
            return {'오류': {'내용': str(e), '파일명': str(text_path)}}
    def process_content_class_text(self, text) -> Dict:
        """텍스트 컨텐츠를 처리하고 결과물을 생성합니다."""
        try:
            results = {}
            content = text
            
            # 공지사항 추출
            notice = self.get_claude_response(f"""
            다음 수업 내용에서 공지사항만을 추출해주세요.
            공지사항만 반환해주세요.
            공지사항이 없다면 "공지사항이 없습니다."라고 답변해주세요.
            
            수업 내용:
            {content}
            """)
            results['notice'] = {'text': notice}
            
            # 수업내용 요약
            summary = self.get_claude_response(f"""
            다음 수업 내용을 핵심 개념과 중요 포인트 위주로 요약해주세요.
            대화형 문구는 포함하지 말고, 바로 요약 내용만 적어 주세요.
            각 개념에 대해 구체적인 예시를 포함해주세요.
            
            수업 내용:
            {content}
            """)
            results['class_summary'] = {'text': summary}
            
            # 객관식 문제 생성
            quiz = self.generate_quiz(content)
            results['quiz'] = {'text': quiz}
            
            # 해설 생성
            explanations = {
                "quiz_commentary": [
                    {
                        "quiz_number": i + 1,
                        "quiz": q["question"],
                        "answer": f"{int(q['correct_answer']) + 1}번",
                        "commentary": q["explanation"]
                    }
                    for i, q in enumerate(quiz["questions"])
                ]
            }
            results['quiz_commentary'] = {'data': explanations}
            
            return results
        except Exception as e:
            print(f"컨텐츠 처리 중 오류 발생: {str(e)}")
            return {'오류': {'내용': str(e)}}
        
    def process_content_counsel_text(self, text) -> Dict:
        """텍스트 컨텐츠를 처리하고 결과물을 생성합니다."""
        try:
            results = {}
            content = text
            
            # 공지사항 추출
            notice = self.get_claude_response(f"""
            다음 텍스트에서 상담내용을 요약하여 정리해주세요.
            맨앞에 말머리를 붙이지 말고 요약해주세요.
            1000자 이상이 되게 요약해주세요
            이름말고 학생과 상담사로 바꿔주세요.
            상담내용요약:text가 되게 요약해주세요.
            일정 택스트 길이마다 \n\n을 적용해 두줄씩 띄어주세요.
            
            상담 내용:
            {content}
            """)
            
            results['summary'] = notice
            return results
        except Exception as e:
            print(f"컨텐츠 처리 중 오류 발생: {str(e)}")
            return {'오류': {'내용': str(e)}}


def load_api_keys(base_path: str) -> Dict[str, str]:
    """API 키들을 텍스트 파일에서 로드합니다."""
    try:
        # OpenAI API 키 로드
        api_key_path = Path(base_path) / "api_key.txt"
        with open(api_key_path, 'r', encoding='utf-8') as f:
            openai_api_key = f.read().strip()
            
        # Claude API 키 로드
        claude_api_key_path = Path(base_path) / "claude_api_key.txt"
        with open(claude_api_key_path, 'r', encoding='utf-8') as f:
            claude_api_key = f.read().strip()
            
        return {
            'openai_api_key': openai_api_key,
            'claude_api_key': claude_api_key,
            'base_folder': base_path
        }
    except Exception as e:
        raise Exception(f"API 키 파일 로드 중 오류 발생: {str(e)}")

def process_audio_file(processor: EduContentProcessor, audio_path: Path):
    """단일 오디오 파일을 처리합니다."""
    try:
        print(f"\n오디오 파일 처리 중: {audio_path.name}")
        
        print("\n1. 음성-텍스트 변환 단계")
        text_path = processor.transcribe_audio(audio_path)
        
        print("\n2. 컨텐츠 처리 단계")
        results = processor.process_content_path(Path(text_path))
        
        return results

    except Exception as e:
        print(f"\n오디오 파일 처리 중 오류 발생: {str(e)}")
        return {'오류': {'내용': str(e), '파일': str(audio_path)}}
    
def process_class_text_file(processor: EduContentProcessor, text):
    """단일 수업내용 텍스트 파일을 처리합니다."""
    try:
        print("\n1. 컨텐츠 처리 단계")
        results = processor.process_content_class_text(text)
        return results
    except Exception as e:
        print(f"\텍스트 파일 처리 중 오류 발생: {str(e)}")
        return {'오류': {'내용': str(e)}}
def process_counsel_text_file(processor: EduContentProcessor, text):
    """단일 상담내용 텍스트 파일을 처리합니다."""
    try:
        print("\n1. 컨텐츠 처리 단계")
        results = processor.process_content_counsel_text(text)
        return results
    except Exception as e:
        print(f"\텍스트 파일 처리 중 오류 발생: {str(e)}")
        return {'오류': {'내용': str(e)}}

def process_all_files(base_folder: str):
    """모든 오디오 파일을 처리합니다."""
    try:
        # API 키 로드
        config = load_api_keys(base_folder)
        
        # 처리기 초기화
        processor = EduContentProcessor(
            config['openai_api_key'],
            config['claude_api_key'],
            config['base_folder']
        )
        
        # WAV 파일 목록 가져오기
        audio_folder = Path(base_folder) / "audio"
        audio_files = list(audio_folder.glob("*.wav"))
        
        if not audio_files:
            print("처리할 WAV 파일이 없습니다.")
            return {}
        
        # 각 WAV 파일 처리
        all_results = {}
        for audio_file in audio_files:
            try:
                results = process_audio_file(processor, audio_file)
                all_results[audio_file.name] = results
                
                # 결과 출력
                print(f"\n=== 파일 처리 결과: {audio_file.name} ===")
                for category, data in results.items():
                    if category != '오류':
                        print(f"\n{category}:")
                        print(f"저장경로: {data['저장경로']}")
                    else:
                        print(f"\n오류 발생: {data['내용']}")
                        
            except Exception as e:
                print(f"\n파일 {audio_file.name} 처리 중 오류 발생: {str(e)}")
                all_results[audio_file.name] = {'오류': {'내용': str(e)}}
                continue
        
        print("\n모든 파일 처리가 완료되었습니다.")
        
        # 청크 폴더 내 모든 파일 삭제
        chunks_folder = Path(base_folder) / "chunks"
        if chunks_folder.exists():
            for chunk_file in chunks_folder.glob("*.wav"):
                try:
                    os.remove(chunk_file)
                except Exception as e:
                    print(f"청크 파일 삭제 중 오류 발생: {str(e)}")
            try:
                os.rmdir(chunks_folder)
                print("청크 폴더 정리 완료")
            except Exception as e:
                print(f"청크 폴더 삭제 중 오류 발생: {str(e)}")
                
        return all_results
        
    except Exception as e:
        print(f"\n실행 중 오류가 발생했습니다: {str(e)}")
        return {'오류': {'내용': str(e)}}


if __name__ == "__main__":
    base_folder = os.path.dirname(os.path.abspath(__file__))
    process_all_files(base_folder)