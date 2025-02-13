# Claude-api 사용

import os
import pandas as pd
import anthropic
import json
from typing import Dict, Any
from pathlib import Path  

class CareerAdvisor:
    def __init__(self, anthro_api_key: str, base_folder: str):
        """Career Advisor Initialization with dynamic path management."""
        self.client = anthropic.Anthropic(api_key=anthro_api_key)
        self.base_folder = Path(base_folder)

        # 필요한 파일 경로 설정
        self.job_file = self.base_folder / "Job_View_All_Data.csv"
        self.major_file = self.base_folder / "Major_View_Data.csv"

        # 데이터 로드
        self.job_data = pd.read_csv(self.job_file)
        self.major_data = pd.read_csv(self.major_file)

    def get_job_info(self, desired_job: str) -> pd.DataFrame:
        """Filter job information based on the desired job."""
        return self.job_data[self.job_data['직업명'] == desired_job]

    def get_related_departments(self, desired_job: str) -> pd.DataFrame:
        """Filter departments related to the desired job."""
        return self.major_data[self.major_data['관련직업'].str.contains(desired_job, na=False)]

    def generate_advice_with_missing_job(self, desired_job: str) -> str:
        """Generate detailed career advice for a job when no direct information is available."""
        messages = [
            {
                "role": "user",
                "content": f"""
                학생이 '{desired_job}' 직업을 목표로 하고 있습니다.
                해당 직업에 대해 적합한 관련 학과를 5개 추천해 주세요. 
                각 학과에 대해 다음 구조에 따라 조언을 작성해 주세요:

                1. 학업 및 자기계발:
                - 학과에서 집중적으로 학습해야 할 주요 과목.
                - 학과와 관련된 활동(캠프, 프로젝트, 대회 등).

                2. 신체 단련:
                - 학과 특성 및 직업 관련 건강 유지 및 신체 단련 방법.

                3. 자격증 및 역량 개발:
                - 해당 학과 및 직업에 필요한 주요 자격증.
                - 추천되는 추가 역량 및 기술.

                4. 진로 탐색 활동:
                - 참여할 수 있는 활동(인턴십, 연구 프로젝트, 워크숍 등).

                5. 심리적 준비:
                - 성공적인 커리어를 위해 필요한 정신적 태도와 준비.

                6. 행동 계획:
                - 학생이 지금 당장 실천할 수 있는 구체적인 활동.

                모든 학과에 대해 위 구조에 맞게 실질적이고 구체적인 조언을 작성해 주세요.
                **중요**: 응답에는 "나머지 학과도 동일한 방식으로 작성 가능합니다."와 같은 불필요한 문구를 포함하지 마세요.
                """
            }
        ]

        response = self.client.messages.create(
            model="claude-3-5-haiku-20241022",
            system="당신은 진로 상담 전문가입니다.",
            messages=messages,
            max_tokens=3000,
            temperature=0.7,
        )

        # Process the response content
        if hasattr(response, 'content') and isinstance(response.content, list):
            advice = "\n".join(
                block.text.strip() for block in response.content if hasattr(block, 'text')
            )
            return advice

        return "Claude API 응답을 처리할 수 없습니다."

    def generate_advice(self, job_info: pd.Series, department_info: pd.Series, desired_job: str) -> str:
        """Generate career advice using Claude API."""
        messages = [
            {
                "role": "user",
                "content": f"""
                학생이 '{desired_job}' 직업을 목표로 하고 있습니다.
                관련 학과에서 구체적으로 어떤 준비를 해야 하는지에 대한 상세한 조언을 작성해 주세요.

                제시된 모든 학과별로 개별적인 상세 조언을 작성해 주세요:
                - 학과명: 경찰행정과
                - 학과명: 경찰행정학과
                - 학과명: 경호스포츠과
                - 학과명: 경호학과
                - 학과명: 체육학과

                {desired_job} 관련된 학과에 대해 다음 내용을 포함하여 작성해 주세요:
                1. 학업 및 자기계발:
                - 학생이 집중적으로 학습해야 할 과목과 주요 학습 내용.
                - 학과와 관련된 활동(캠프, 박람회 등).
                2. 신체 단련:
                - 필요한 신체 단련 방법과 운동 계획.
                - 관련 신체 능력 시험 대비 방법.
                3. 자격증 및 역량 개발:
                - 학과와 직업에 필요한 자격증 및 기술.
                - 추천되는 추가 역량.
                4. 진로 탐색 활동:
                - 학생이 참여할 수 있는 진로 관련 활동 및 경험.
                5. 심리적 준비:
                - 해당 직업에 필요한 정신적 태도와 준비.
                6. 행동 계획:
                - 학생이 지금 당장 실천할 수 있는 구체적인 활동.

                위의 구조에 따라  {desired_job} 관련된 학과에 대해 구체적이고 실질적인 조언을 작성해 주세요. 
                """
            }
        ]

        response = self.client.messages.create(
            model="claude-3-5-haiku-20241022",
            system="당신은 진로 상담 전문가입니다.",
            messages=messages,
            max_tokens=1000,
            temperature=0.7,
        )

        if hasattr(response, 'content') and isinstance(response.content, list):
            advice = "\n".join(
                block.text.strip() for block in response.content if hasattr(block, 'text')
            )
            return advice

        return "Claude API 응답이 예상치 못한 형식으로 반환되었습니다."

    def process(self, desired_job: str) -> Dict[str, Any]:
        """Process the desired job and related departments for advice."""
        job_info = self.get_job_info(desired_job)
        related_departments = self.get_related_departments(desired_job)

        if job_info.empty:
            advice = self.generate_advice_with_missing_job(desired_job)
            return {"error": f"해당 직업 정보를 찾을 수 없습니다. 하지만 관련 조언을 제공합니다.", "advice": advice}

        if related_departments.empty:
            return {"error": "해당 직업과 관련된 학과 정보를 찾을 수 없습니다."}

        advice = []
        for _, department_info in related_departments.iterrows():
            try:
                advice_text = self.generate_advice(job_info.iloc[0], department_info, desired_job)
                if advice_text.strip():
                    advice.append({"department": department_info['학과명'], "advice": advice_text})
                else:
                    advice.append({"department": department_info['학과명'], "advice": "조언을 생성하지 못했습니다."})
            except Exception as e:
                advice.append({"department": department_info['학과명'], "advice": f"Error generating advice: {str(e)}"})

        return {"advice": advice}


if __name__ == "__main__":
    base_folder = os.path.dirname(os.path.abspath(__file__))
    anthro_api_key = ""

    advisor = CareerAdvisor(anthro_api_key, base_folder)

    with open(advisor.json_file_path, "r", encoding="utf-8") as file:
        data = json.load(file)


    job_recommendations = data.get("추출된_정보", {}).get("추천직업", {}).keys()
    for desired_job in job_recommendations:
        print(f"추천 직업: {desired_job}")
        result = advisor.process(desired_job)
        if "error" in result:
            print(result["error"])
            print(result.get("advice", ""))
        else:
            for item in result["advice"]:
                print(f"학과명: {item['department']}")
                print(f"조언: {item['advice']}")
                print("=" * 50)






