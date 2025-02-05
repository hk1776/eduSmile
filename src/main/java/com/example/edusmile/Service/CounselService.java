package com.example.edusmile.Service;

import com.example.edusmile.Entity.CounselEntity;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.CounselRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;



@Service
@RequiredArgsConstructor
public class CounselService {

    private final CounselRepository counselRepository;
    private final MemberRepository memberRepository;
    @Async
    public void upload_pdf(MultipartFile file ,Long studentId) throws IOException {
        // 파일을 ByteArrayResource 변환
        ByteArrayResource byteArrayResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename(); // 파일 이름 설정
            }
        };


        // RestTemplate을 사용하여 FastAPI로 파일 전송
        RestTemplate restTemplate = new RestTemplate();

        // 파일을 Multipart로 보내기 위한 HttpHeaders 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 파일을 MultipartEntity로 감싸기
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // FastAPI 서버에 파일 전송
        ResponseEntity<String> response = restTemplate.exchange(
                "https://edusmile-fastapi-bbd6che3gpg6fuhx.koreacentral-01.azurewebsites.net/process_pdf/", HttpMethod.POST, requestEntity, String.class);

        // JSON 응답을 그대로 반환 (String 형태)
        String fastResult = response.getBody();


/*
        String fastResult = "{\n" +
                "  \"status\": \"success\",\n" +
                "  \"summary\": {\n" +
                "    \"Awards\": [\n" +
                "      \"2014년 봉사상\",\n" +
                "      \"2014년 제3회 반송 문학제 논술 부문 2위\",\n" +
                "      \"1학년 개근상\",\n" +
                "      \"3년 개근상\",\n" +
                "      \"표창장(선행부문)\"\n" +
                "    ],\n" +
                "    \"Certifications\": [],\n" +
                "    \"CareerAspiration\": [\n" +
                "      \"1학년: 컴퓨터 공학자\",\n" +
                "      \"2학년: 기계공학자\",\n" +
                "      \"3학년: 기계공학자\"\n" +
                "    ],\n" +
                "    \"CreativeExperienceActivities\": [\n" +
                "      \"검도 동아리 활동\",\n" +
                "      \"영어회화 동아리 활동\",\n" +
                "      \"학급회 활동\",\n" +
                "      \"독서 감상반 활동\"\n" +
                "    ],\n" +
                "    \"SubjectSpecialtyAndNotes\": [\n" +
                "      \"영어 성적 향상\",\n" +
                "      \"물리와 화학 분야 관심\",\n" +
                "      \"수학과 과학 분야 탐구력\"\n" +
                "    ],\n" +
                "    \"ReadingActivities\": [\n" +
                "      \"정글만리, 정의란 무엇인가 등 다양한 책 읽기\",\n" +
                "      \"과학, 수학 관련 책 독서\"\n" +
                "    ],\n" +
                "    \"BehaviorCharacteristicsAndOverallComments\": [\n" +
                "      \"준법정신이 강함\",\n" +
                "      \"배려심 많음\",\n" +
                "      \"학업에 대한 강한 의지\",\n" +
                "      \"다양한 분야에 관심\"\n" +
                "    ],\n" +
                "    \"RecommendedJobs\": {\n" +
                "      \"기계공학자\": {\n" +
                "        \"추천이유\": [\n" +
                "          \"기계와 관련된 활동에 흥미\",\n" +
                "          \"어릴 때부터 기계에 대한 관심\"\n" +
                "        ],\n" +
                "        \"직업관련학과\": [\n" +
                "          \"기계공학과\",\n" +
                "          \"기계설계공학과\",\n" +
                "          \"메카트로닉스공학과\"\n" +
                "        ],\n" +
                "        \"직업관련자격증\": [\n" +
                "          \"기계설계기사\",\n" +
                "          \"기계정비기능사\",\n" +
                "          \"자동차정비기사\"\n" +
                "        ],\n" +
                "        \"노력\": [\n" +
                "          \"관련 분야 전문 서적 및 논문 지속적 탐독\",\n" +
                "          \"관련 동아리 및 연구 활동 참여\",\n" +
                "          \"전문 기술 및 소프트웨어 학습\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"컴퓨터 공학자\": {\n" +
                "        \"추천이유\": [\n" +
                "          \"어릴 때부터 컴퓨터 조립에 관심\",\n" +
                "          \"정보 기술에 대한 높은 이해도\"\n" +
                "        ],\n" +
                "        \"직업관련학과\": [\n" +
                "          \"컴퓨터공학과\",\n" +
                "          \"소프트웨어공학과\",\n" +
                "          \"정보통신공학과\"\n" +
                "        ],\n" +
                "        \"직업관련자격증\": [\n" +
                "          \"정보처리기사\",\n" +
                "          \"정보처리산업기사\",\n" +
                "          \"네트워크관리사\"\n" +
                "        ],\n" +
                "        \"노력\": [\n" +
                "          \"프로그래밍 언어 지속적 학습\",\n" +
                "          \"최신 IT 트렌드 파악\",\n" +
                "          \"개인 프로젝트 및 포트폴리오 구축\"\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"job_information\": {\n" +
                "    \"기계공학자\": {\n" +
                "      \"error\": \"해당 직업 123412341234보를 찾을 수 없습니다. 하지만 관련 조언을 제공합니다.\",\n" +
                "      \"advice\": \"1. 기계공학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 열역학, 유체역학, 기계설계, 재료역학, 자동제어\\n- 추천 활동: 로봇 경진대회, 자동차 설계 캠프, 메이커 프로젝트\\n\\n2. 신체 단련:\\n- 근력 및 지구력 강화 운동\\n- 손과 손가락 정밀 동작 훈련\\n- 장시간 컴퓨터 작업 대비 스트레칭\\n\\n3. 자격증 및 역량 개발:\\n- 기계설계산업기사\\n- CAD/CAM 전문 자격증\\n- 3D 프린팅 관련 자격증\\n- 프로그래밍 능력(Python, C++)\\n\\n4. 진로 탐색 활동:\\n- 제조업체 인턴십\\n- 기업 연구소 방문 프로그램\\n- 기계공학 관련 학술대회 참석\\n- 엔지니어링 멘토링 프로그램\\n\\n5. 심리적 준비:\\n- 끈기와 인내심 개발\\n- 문제 해결에 대한 적극적 태도\\n- 실패를 학습 기회로 인식\\n- 지속적인 자기혁신 마인드셋\\n\\n6. 행동 계획:\\n- 프로그래밍 독학 시작\\n- 기계 모형 제작 취미 활동\\n- 관련 YouTube 채널 구독\\n- 기계공학 관련 서적 읽기\\n\\n2. 산업공학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 최적화, 시뮬레이션, 데이터분석, 인간공학\\n- 추천 활동: 데이터 분석 경진대회, 생산관리 프로젝트\\n\\n2. 신체 단련:\\n- 장시간 컴퓨터 작업 대비 운동\\n- 집중력 향상 명상\\n- 균형 잡힌 식단 관리\\n\\n3. 자격증 및 역량 개발:\\n- 품질경영기사\\n- 생산자동화산업기사\\n- 데이터분석 관련 자격증\\n- 머신러닝/AI 기초 역량\\n\\n4. 진로 탐색 활동:\\n- 제조업 공정 개선 인턴십\\n- 스마트팩토리 견학\\n- 빅데이터 워크숍 참여\\n- 기업 컨설팅 프로젝트\\n\\n5. 심리적 준비:\\n- 시스템적 사고방식\\n- 팀워크 능력\\n- 변화에 대한 적응력\\n- 데이터 기반 의사결정 능력\\n\\n6. 행동 계획:\\n- R, Python 프로그래밍 학습\\n- 엑셀 고급 기능 마스터\\n- 통계 관련 온라인 강좌 수강\\n- 데이터 분석 동아리 활동\\n\\n3. 전기전자공학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 회로이론, 제어공학, 전자기학, 마이크로프로세서\\n- 추천 활동: 임베디드 시스템 경진대회, IoT 프로젝트\\n\\n2. 신체 단련:\\n- 정밀 작업을 위한 손 근력 운동\\n- 눈 건강 관리\\n- 장시간 집중을 위한 스트레스 관리\\n\\n3. 자격증 및 역량 개발:\\n- 전기산업기사\\n- 전자기기 설계 관련 자격증\\n- 임베디드 시스템 개발 능력\\n- 프로그래밍 언어(C, C++)\\n\\n4. 진로 탐색 활동:\\n- 로봇 개발 연구소 인턴십\\n- 전자기기 기업 현장 방문\\n- 자동화 시스템 워크숍\\n- 기술 세미나 참석\\n\\n5. 심리적 준비:\\n- 지속적인 기술 혁신 마인드\\n- 창의적 문제 해결 능력\\n- 기술 트렌드에 대한 민감성\\n- 평생 학습 태도\\n\\n6. 행동 계획:\\n- 아두이노 키트로 프로젝트\\n- 전자회로 독학\\n- 온라인 코딩 강좌 수강\\n- 관련 전문 블로그/YouTube 구독\\n\\n4. 로봇공학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 로봇공학, 인공지능, 센서공학, 메카트로닉스\\n- 추천 활동: 로봇 경진대회, AI 프로젝트\\n\\n2. 신체 단련:\\n- 섬세한 손동작 훈련\\n- 장시간 집중력 유지 운동\\n- 신경 민첩성 개발 트레이닝\\n\\n3. 자격증 및 역량 개발:\\n- 자동화기구산업기사\\n- 로봇 프로그래밍 자격증\\n- AI/머신러닝 기초 역량\\n- 3D 모델링 능력\\n\\n4. 진로 탐색 활동:\\n- 로봇 연구소 인턴십\\n- 로봇 개발 기업 견학\\n- AI/로봇 컨퍼런스 참석\\n- 국제 로봇 경진대회\\n\\n5. 심리적 준비:\\n- 혁신적 사고방식\\n- 실패를 두려워하지 않는 태도\\n- 지속적인 학습 의지\\n- 창의적 문제 해결 능력\\n\\n6. 행동 계획:\\n- 로봇 키트 조립 및 프로그래밍\\n- Python, ROS 학습\\n- 로봇공학 관련 서적 독서\\n- 온라인 로봇 커뮤니티 활동\\n\\n5. 항공우주공학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 비행역학, 추진공학, 항공기설계, 유체역학\\n- 추천 활동: 드론 경진대회, 위성 설계 프로젝트\\n\\n2. 신체 단련:\\n- 고도의 집중력 유지 훈련\\n- 스트레스 관리 기술\\n- 균형 잡힌 신체 컨디션 유지\\n\\n3. 자격증 및 역량 개발:\\n- 항공정비사 자격증\\n- 드론 조종 자격증\\n- 비행 시뮬레이션 능력\\n- 공간지각능력 개발\\n\\n4. 진로 탐색 활동:\\n- 항공우주 연구소 인턴십\\n- 국방과학연구소 견학\\n- 항공우주 관련 세미나\\n- 국제 항공우주 컨퍼런스\\n\\n5. 심리적 준비:\\n- 극한의 정밀함 추구\\n- 안전 의식\\n- 글로벌 마인드\\n- 도전적 사고방식\\n\\n6. 행동 계획:\\n- 드론 조종 및 제작 학습\\n- 비행 시뮬레이션 게임\\n- 항공우주 관련 다큐멘터리 시청\\n- 수학/물리 심화 학습\"\n" +
                "    },\n" +
                "    \"컴퓨터 공학자\": {\n" +
                "      \"error\": \"해당 직업 정보를 찾을 수 없습니다. 하지만 관련 조언을 제공합니다.\",\n" +
                "      \"advice\": \"1. 컴퓨터공학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 자료구조, 알고리즘, 프로그래밍 언어(C++, Java), 운영체제, 네트워크\\n- 활동: SW 코딩 캠프, 해커톤, 알고리즘 경진대회, 프로그래밍 동아리 참여\\n\\n2. 신체 단련:\\n- 장시간 컴퓨터 작업으로 인한 목, 어깨 스트레칭\\n- 규칙적인 운동(수영, 요가 등 앉아서 하는 업무에 도움되는 운동)\\n- 근력 운동을 통한 체력 관리\\n\\n3. 자격증 및 역량 개발:\\n- 정보처리기사, SQLD, 정보처리산업기사\\n- 추가 역량: 클라우드 컴퓨팅, 빅데이터 분석, AI/머신러닝 기술\\n\\n4. 진로 탐색 활동:\\n- IT 기업 인턴십\\n- 오픈소스 프로젝트 참여\\n- 기술 컨퍼런스 및 세미나 참석\\n\\n5. 심리적 준비:\\n- 지속적인 학습에 대한 열정\\n- 문제 해결 능력과 논리적 사고력 개발\\n- 빠르게 변화하는 기술 트렌드에 대한 적응력\\n\\n6. 행동 계획:\\n- 프로그래밍 언어 독학\\n- GitHub 개인 프로젝트 포트폴리오 구축\\n- 온라인 코딩 플랫폼(LeetCode 등) 활용 알고리즘 훈련\\n\\n2. 소프트웨어학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 소프트웨어 공학, 데이터베이스, 웹 개발, 모바일 앱 개발\\n- 활동: 앱 개발 경진대회, SW 개발 캠프, 프로그래밍 워크샵\\n\\n2. 신체 단련:\\n- 개발자를 위한 바른 자세 교정 운동\\n- 정기적인 유산소 운동\\n- 눈 건강을 위한 스트레칭과 휴식\\n\\n3. 자격증 및 역량 개발:\\n- 정보처리기사, 정보처리산업기사\\n- 클라우드 자격증(AWS, Azure)\\n- 웹/앱 개발 관련 전문 기술\\n\\n4. 진로 탐색 활동:\\n- 스타트업 인턴십\\n- 오픈소스 프로젝트 참여\\n- 기술 세미나 및 워크숍 참석\\n\\n5. 심리적 준비:\\n- 창의적 문제 해결 마인드\\n- 팀 협업 능력\\n- 지속적인 자기 개발 의지\\n\\n6. 행동 계획:\\n- 개인 SW 프로젝트 개발\\n- 온라인 프로그래밍 강좌 수강\\n- 개발 커뮤니티 활동\\n\\n3. 정보통신공학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 네트워크 보안, 데이터 통신, 무선통신, 임베디드 시스템\\n- 활동: 네트워크 보안 캠프, IoT 프로젝트, 해킹 방어 대회\\n\\n2. 신체 단련:\\n- 장시간 집중을 위한 두뇌 트레이닝\\n- 규칙적인 운동으로 체력 유지\\n- 정기적인 스트레칭과 휴식\\n\\n3. 자격증 및 역량 개발:\\n- 정보처리기사, CCNA, 정보보안기사\\n- 네트워크 보안, 클라우드 컴퓨팅 기술\\n\\n4. 진로 탐색 활동:\\n- 네트워크 보안 기업 인턴십\\n- 사이버 보안 컨퍼런스 참여\\n- 기술 연구 프로젝트\\n\\n5. 심리적 준비:\\n- 끊임없는 학습 자세\\n- 세부적이고 정확한 업무 처리 능력\\n- 기술 변화에 대한 빠른 적응력\\n\\n6. 행동 계획:\\n- 네트워크 관련 온라인 강좌 수강\\n- 개인 보안 프로젝트 진행\\n- 관련 커뮤니티 활동\\n\\n4. 데이터사이언스학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 머신러닝, 빅데이터 분석, 통계학, 인공지능\\n- 활동: 데이터 분석 경진대회, AI 프로젝트, 데이터 사이언스 캠프\\n\\n2. 신체 단련:\\n- 장시간 데이터 분석을 위한 집중력 훈련\\n- 정기적인 운동과 명상\\n- 눈 건강 관리\\n\\n3. 자격증 및 역량 개발:\\n- 빅데이터분석전문가, 정보처리기사\\n- 파이썬, R, SQL 등 프로그래밍 언어\\n- 데이터 시각화 도구 활용 능력\\n\\n4. 진로 탐색 활동:\\n- 데이터 분석 기업 인턴십\\n- AI/빅데이터 컨퍼런스 참여\\n- 연구 프로젝트 수행\\n\\n5. 심리적 준비:\\n- 분석적 사고와 문제 해결 능력\\n- 창의적 접근 마인드\\n- 지속적인 학습 열정\\n\\n6. 행동 계획:\\n- Kaggle 등 데이터 분석 플랫폼 활용\\n- 데이터 분석 온라인 강좌 수강\\n- 개인 데이터 프로젝트 포트폴리오 구축\\n\\n5. 인공지능학과\\n\\n1. 학업 및 자기계발:\\n- 주요 과목: 딥러닝, 머신러닝, 로보틱스, 자연어 처리\\n- 활동: AI 해커톤, 로봇 경진대회, 인공지능 연구 프로젝트\\n\\n2. 신체 단련:\\n- 뇌 건강을 위한 두뇌 트레이닝\\n- 규칙적인 운동과 충분한 수면\\n- 스트레스 관리 및 명상\\n\\n3. 자격증 및 역량 개발:\\n- AI 관련 전문 자격증\\n- 파이썬, TensorFlow, PyTorch 등 프로그래밍 기술\\n- 수학 및 통계 역량\\n\\n4. 진로 탐색 활동:\\n- AI 연구소 인턴십\\n- 국제 AI 컨퍼런스 참여\\n- 오픈소스 AI 프로젝트\\n\\n5. 심리적 준비:\\n- 혁신적 사고와 창의성\\n- 복잡한 문제 해결 능력\\n- 지속적인 자기 개발 의지\\n\\n6. 행동 계획:\\n- AI 온라인 강좌 수강\\n- 개인 AI 프로젝트 개발\\n- AI 관련 커뮤니티 활동\"\n" +
                "    }\n" +
                "  }\n" +
                "}";


 */

        ObjectMapper objectMapper = new ObjectMapper();
        String report = "";
        try {
            JsonNode rootNode = objectMapper.readTree(fastResult);

            // 보고서 생성
            report = generateReport(rootNode);

        } catch (IOException e) {
            e.printStackTrace();
        }


        Optional<MemberEntity> st = memberRepository.findById(studentId);


        MemberEntity student = st.get();

        List<CounselEntity> duplicate = counselRepository.duplicateContent_loginId(student.getLoginId(),"record");

        if (!duplicate.isEmpty()) {


            CounselEntity duple = duplicate.get(0);
            duple.setCounsel(report);
            counselRepository.save(duple);

        } else {
            CounselEntity counselEntity = new CounselEntity();

            counselEntity.setClassId(student.getTeacherCode());
            counselEntity.setCounsel(report);
            counselEntity.setTitle(student.getName() + " 님의 생활기록부 분석 내용1");
            counselEntity.setStudent(student.getName());
            counselEntity.setViews(0);
            counselEntity.setType("record");
            counselEntity.setLoginId(student.getLoginId());
            counselRepository.save(counselEntity);
        }
    }

    public static String generateReport(JsonNode data) {             //json string 으로 바꾸기 이쁘게
        StringBuilder report = new StringBuilder();

        // 수상 내역
        report.append(" 1. 수상 내역\n");
        JsonNode awards = data.path("summary").path("Awards");
        for (JsonNode award : awards) {
            report.append("- ").append(award.asText()).append("\n");
        }
        report.append("\n");

        // 자격증
        report.append(" 2. 자격증\n");
        JsonNode certifications = data.path("summary").path("Certifications");
        if (!certifications.isArray() || certifications.size() == 0) {
            report.append("현재 자격증 보유 내역은 없습니다.\n");
        }
        report.append("\n");

        // 진로 목표
        report.append(" 3. 진로 목표\n");
        JsonNode careerAspiration = data.path("summary").path("CareerAspiration");
        for (JsonNode aspiration : careerAspiration) {
            report.append("- ").append(aspiration.asText()).append("\n");
        }
        report.append("\n");

        // 창의적 경험 및 활동
        report.append(" 4. 창의적 경험 및 활동\n");
        JsonNode creativeActivities = data.path("summary").path("CreativeExperienceActivities");
        for (JsonNode activity : creativeActivities) {
            report.append("- ").append(activity.asText()).append("\n");
        }
        report.append("\n");

        // 주요 과목 및 특기 사항
        report.append(" 5. 주요 과목 및 특기 사항\n");
        JsonNode subjectNotes = data.path("summary").path("SubjectSpecialtyAndNotes");
        for (JsonNode note : subjectNotes) {
            report.append("- ").append(note.asText()).append("\n");
        }
        report.append("\n");

        // 독서 활동
        report.append(" 6. 독서 활동\n");
        JsonNode readingActivities = data.path("summary").path("ReadingActivities");
        for (JsonNode activity : readingActivities) {
            report.append("- ").append(activity.asText()).append("\n");
        }
        report.append("\n");

        // 행동 특성 및 전반적인 코멘트
        report.append(" 7. 행동 특성 및 전반적인 코멘트\n");
        JsonNode behaviorComments = data.path("summary").path("BehaviorCharacteristicsAndOverallComments");
        for (JsonNode comment : behaviorComments) {
            report.append("- ").append(comment.asText()).append("\n");
        }
        report.append("\n");

        // 추천 직업
        report.append(" 8. 추천 직업\n");
        JsonNode recommendedJobs = data.path("summary").path("RecommendedJobs");
        for (Iterator<String> jobNames = recommendedJobs.fieldNames(); jobNames.hasNext(); ) {
            String jobName = jobNames.next();
            report.append("추천 직업 : ").append(jobName).append("\n");

            JsonNode jobDetails = recommendedJobs.path(jobName);
            JsonNode reasons = jobDetails.path("추천이유");
            for (JsonNode reason : reasons) {
                report.append("- 추천 이유: ").append(reason.asText()).append("\n");
            }

            JsonNode relatedDepartments = jobDetails.path("직업관련학과");
            report.append("- 직업 관련 학과: ");
            for (int i = 0; i < relatedDepartments.size(); i++) {
                report.append(relatedDepartments.get(i).asText());
                if (i < relatedDepartments.size() - 1) {
                    report.append(", ");
                }
            }
            report.append("\n");

            JsonNode relatedCertifications = jobDetails.path("직업관련자격증");
            report.append("- 직업 관련 자격증: ");
            for (int i = 0; i < relatedCertifications.size(); i++) {
                report.append(relatedCertifications.get(i).asText());
                if (i < relatedCertifications.size() - 1) {
                    report.append(", ");
                }
            }
            report.append("\n");

            JsonNode efforts = jobDetails.path("노력");
            report.append("- 노력: ");
            for (int i = 0; i < efforts.size(); i++) {
                report.append(efforts.get(i).asText());
                if (i < efforts.size() - 1) {
                    report.append(", ");
                }
            }
            report.append("\n");
            report.append("\n");
        }

        report.append(" 9. 직업 정보\n");
        JsonNode jobInformation = data.path("job_information");
        for (Iterator<String> jobNames = jobInformation.fieldNames(); jobNames.hasNext();) {
            String jobName = jobNames.next();
            JsonNode jobDetails = jobInformation.path(jobName);
            String advice = jobDetails.path("advice").asText();

            report.append("추천 직업 :  ").append(jobName).append("\n");
            report.append("- 추천: ").append(advice).append("\n");
            report.append("\n");
        }

        return report.toString();
    }
}
