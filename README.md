![AI 20조 썸네일](https://github.com/user-attachments/assets/751af4bc-d885-40b9-95b4-aeda6d770586)

 ### 상세 기능 설명서 URL
[ https://www.notion.so/199b474a74fc800390aec3cc5b3bb890](https://equable-mustard-db9.notion.site/199b474a74fc800390aec3cc5b3bb890)

<br>

 # 🏆수상
 ### AIVLE School Big Project 우수상 수상

*********

**KT AIVLE School 빅 프로젝트**
<br>
제작기간 : 2025.1.2~2025.2.17


## <u>👨‍🔧Team
 ### AI트랙 충남/충북 20조
<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>역할</th>
            <th>개발 영역</th>
        </tr>
    </thead>
    <tbody>
      <tr>
            <td>강홍규</td>
            <td>  
                조장, 서버 개발
            </td>
            <td>
              <ul>
                  <li>Full-stack</li>
                  <li>서비스 배포</li>
              </ul>
            </td>
        </tr>
        <tr>
            <td>김대길</td>
            <td>  
               서버 개발
            </td>
            <td>
              <ul>
                  <li>Full-stack</li>
                  <li>보안</li>
              </ul>
            </td>
        </tr>
        <tr>
            <td>김규원</td>
            <td>  
                서버 개발
            </td>
            <td>
              <ul>
                  <li>Back End</li>
                  <li>데이터 수집</li>
              </ul>
            </td>
        </tr>
        <tr>
            <td>김지섭</td>
            <td>  
                AI 개발
            </td>
            <td>
              <ul>
                  <li>모델 튜닝</li>
                  <li>배포</li>
              </ul>
            </td>
        </tr>
        <tr>
          <td>김강용</td>
            <td>  
                 AI 개발
            </td>
            <td>
              <ul>
                  <li>모델 튜닝</li>
                  <li>데이터 수집</li>
              </ul>
            </td>
        </tr>
        <tr>
          <td>손대원</td>
            <td>  
                 AI 개발
            </td>
            <td>
              <ul>
                  <li>모델 튜닝</li>
                  <li>데이터 수집</li>
              </ul>
            </td>
        </tr>
        <tr>
          <td>남윤주</td>
            <td>  
                디자인, 영상 제작
            </td>
            <td>
              <ul>
                  <li>웹 디자인</li>
                  <li>소개 영상 제작</li>
              </ul>
            </td>
        </tr>
      <tr>
          <td>신성호</td>
            <td>  
                디자인
            </td>
            <td>
              <ul>
                  <li>Front End</li>
              </ul>
            </td>
        </tr>
    </tbody>
</table>


## </u> 🧐Project Objectives
**AI를 활용하여 교사의 행정 업무를 획기적으로 줄이고, 학생 맞춤형 교육을 강화**



## </u> 💻System Design
 ### Tech Stack
- FE
    - HTML / CSS / Java Script / BootStrap / Mustache
- BE
    - Spring / Spring Data JPA / Gradle / FastAPI / My Sql
- AI
    - Pandas / Keras / Claude/ BERT / RAG
- ETC
    - Git
    - Figma
    - Notion
  
## System Architecture
![Image](https://github.com/user-attachments/assets/ba5bc215-fe7b-477e-a618-04d337b8322d)

 ## System Requirements
 ![Image](https://github.com/user-attachments/assets/d5cb4c78-565e-4043-8bef-257692ef497c)

*****

## 사용 기술
 ![Image](https://github.com/user-attachments/assets/60fbbead-0552-4254-ad12-90198979c64d)
 ![Image](https://github.com/user-attachments/assets/4e1260c5-3fe2-4e2a-bf01-9f55a60439db)

*****

## 주요 기능

### 1. 수업 분석

#### 가. 수업 녹음
![Image](https://github.com/user-attachments/assets/4854dcc3-307a-4912-a8c3-b9091779e82d)

1️⃣ 음성 파일 : 수업 음성 파일 업로드  

2️⃣ 음성 녹음 : 수업 내용 녹음

3️⃣ 분석 시작 : 수업 내용 분석(공지사항,수업 요약,시험 생성)

#### 나. 수업분석
![Image](https://github.com/user-attachments/assets/db7bc0ee-de1d-4e99-94ab-a1d254b041df)

```
녹음된 수업을 CLOVER STT 를 통해 텍스트로 변환합니다.

변환된 텍스트를 FAST API 서버로 전송해 수업 요약, 공지사항, 시험 문제 등을 생성합니다.

이후 각 게시판에 자동으로 배포합니다.
```

1️⃣ 과목 및 반 선택 : 분석 결과를 게시할 반을 선택 가능

2️⃣ 등록 : 게시물 하나씩 등록 

3️⃣ 한 번에 등록 :  전체 게시물을 한번에 등록

### 2. 생활기록부 분석 및 상담 요약

#### 가. 생활기록부 분석
![Image](https://github.com/user-attachments/assets/c9af9fed-9269-429b-a572-7f08c4057134)

```
생활기록부(PDF)를 분석하여 요약, 직업, 학과, 활동 추천 등의 데이터를 제공합니다.

해당 기능은 비동기로 처리하여 사용자 편의성을 높였습니다.
```

1️⃣ 교사 페이지: 교사 페이지로 이동

2️⃣ 생활기록부 및 상담 내역 확인 : 날짜별 생활기록부 및 상담 내역 확인 가능

3️⃣ 생활기록부 등록: 생활기록부(PDF) 파일 제출

4️⃣ 분석하기: 생활기록부 분석하여 데이터를 제공합니다.

#### 나. 상담 요약
![Image](https://github.com/user-attachments/assets/0c5b789f-e5b4-4a65-a121-7765b4eed09e)

1️⃣ 음성 파일 선택: 상담 녹음 파일을 선택하여 분석 가능

2️⃣ 음성 녹음 바: 녹음 버튼, 일시정지 버튼, 재생 버튼, 녹음 종료 버튼 제공. 직접 상담 내용 녹음 가능

3️⃣ 분석 시작 버튼: 클릭 시 AI가 음성을 분석하여 텍스트로 재구성

### 다. 상담 요약 등록
![Image](https://github.com/user-attachments/assets/2fa50579-3821-4370-bb88-76df21f62371)

1️⃣ STT(Speech to Text): AI가 상담 녹음의 음성을 텍스트로 재구성한 결과를 확인 가능

2️⃣ 상담 요약: AI가 요약한 상담의 결과를 확인 가능

3️⃣ 등록 버튼: 상담 요약 결과를 상담 내역 섹션에 등록 가능

4️⃣ 학생 선택: 상담 내역을 저장할 학생 선택기능

### 라. 생활기록부 분석 내역 및 상담 내역 확인
![Image](https://github.com/user-attachments/assets/1fe7f981-68aa-4981-93f6-6bdabaddd2ae)

1️⃣ 생활기록부 분석 내역 및 상담 내역 선택: 생활기록부 분석 내역과 날짜별 상담 내역을 각각 확인하는 기

2️⃣ 생활기록부 요약: 요약된 생활기록부 내역 확인

3️⃣ 직업 추천:  요약된 생활기록부를 기반으로 직업 추천

4️⃣ 직업 정보: 추천된 직업에 대한 정보, 학과, 활동 등을 추천

5️⃣ 상담 요약: 날짜별 상담 요약 내역을 확인

*************
 



 
