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

    public void saveCounsel(CounselEntity counselEntity) {

        counselRepository.save(counselEntity);
    }
    public List<CounselEntity> getCounselsOrRecode(String loginId, String kind) {

        return counselRepository.duplicateContent_loginId(loginId,kind);
    }

    public CounselEntity getCounselById(Long id) {

        Optional<CounselEntity> counsel = counselRepository.findById(id);
        if(counsel.isPresent())
        {
            return counsel.get();
        }
        else {
            return null;
        }
    }
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
