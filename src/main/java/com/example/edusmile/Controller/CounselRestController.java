package com.example.edusmile.Controller;


import com.example.edusmile.Entity.CounselEntity;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.CounselRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.CounselService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import retrofit2.http.GET;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class CounselRestController {


    private final MemberRepository memberRepository;

    private final CounselRepository counselRepository;

    private final CounselService counselService;



    @GetMapping("/api/record/{studentId}")
    public ResponseEntity<String> getStudentRecord(@PathVariable Long studentId) {

        MemberEntity member = memberRepository.findById(studentId).orElse(null);

        List<CounselEntity> counsel = counselRepository.duplicateContent(member.getName(),"record");
        String c = counsel.get(0).getCounsel();
        return ResponseEntity.ok(c);
    }

    @Transactional
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("studentId") Long studentId) throws IOException {


        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"message\": \"파일이 없습니다.\"}");
        }

        // PDF 파일만 허용
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body("{\"message\": \"PDF 파일만 업로드 가능합니다.\"}");
        }


        counselService.upload_pdf(file,studentId);


        return ResponseEntity.ok("{\"message\": \"생활기록부 분석시 5분정도 소요됩니다.\"}");
    }

    public static String generateReport(JsonNode data) {             //json string 으로 바꾸기 이쁘게
        StringBuilder report = new StringBuilder();

        // 수상 내역
        report.append("#### 1. 수상 내역\n");
        JsonNode awards = data.path("summary").path("Awards");
        for (JsonNode award : awards) {
            report.append("- ").append(award.asText()).append("\n");
        }
        report.append("\n");

        // 자격증
        report.append("#### 2. 자격증\n");
        JsonNode certifications = data.path("summary").path("Certifications");
        if (!certifications.isArray() || certifications.size() == 0) {
            report.append("현재 자격증 보유 내역은 없습니다.\n");
        }
        report.append("\n");

        // 진로 목표
        report.append("#### 3. 진로 목표\n");
        JsonNode careerAspiration = data.path("summary").path("CareerAspiration");
        for (JsonNode aspiration : careerAspiration) {
            report.append("- ").append(aspiration.asText()).append("\n");
        }
        report.append("\n");

        // 창의적 경험 및 활동
        report.append("#### 4. 창의적 경험 및 활동\n");
        JsonNode creativeActivities = data.path("summary").path("CreativeExperienceActivities");
        for (JsonNode activity : creativeActivities) {
            report.append("- ").append(activity.asText()).append("\n");
        }
        report.append("\n");

        // 주요 과목 및 특기 사항
        report.append("#### 5. 주요 과목 및 특기 사항\n");
        JsonNode subjectNotes = data.path("summary").path("SubjectSpecialtyAndNotes");
        for (JsonNode note : subjectNotes) {
            report.append("- ").append(note.asText()).append("\n");
        }
        report.append("\n");

        // 독서 활동
        report.append("#### 6. 독서 활동\n");
        JsonNode readingActivities = data.path("summary").path("ReadingActivities");
        for (JsonNode activity : readingActivities) {
            report.append("- ").append(activity.asText()).append("\n");
        }
        report.append("\n");

        // 행동 특성 및 전반적인 코멘트
        report.append("#### 7. 행동 특성 및 전반적인 코멘트\n");
        JsonNode behaviorComments = data.path("summary").path("BehaviorCharacteristicsAndOverallComments");
        for (JsonNode comment : behaviorComments) {
            report.append("- ").append(comment.asText()).append("\n");
        }
        report.append("\n");

        // 추천 직업
        report.append("#### 8. 추천 직업\n");
        JsonNode recommendedJobs = data.path("summary").path("RecommendedJobs");
        for (Iterator<String> jobNames = recommendedJobs.fieldNames(); jobNames.hasNext(); ) {
            String jobName = jobNames.next();
            report.append("##### ").append(jobName).append("\n");

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

        report.append("#### 9. 직업 정보\n");
        JsonNode jobInformation = data.path("job_information");
        for (Iterator<String> jobNames = jobInformation.fieldNames(); jobNames.hasNext();) {
            String jobName = jobNames.next();
            JsonNode jobDetails = jobInformation.path(jobName);
            String advice = jobDetails.path("advice").asText();

            report.append("##### ").append(jobName).append("\n");
            report.append("- 추천: ").append(advice).append("\n");
            report.append("\n");
        }

        return report.toString();
    }
}
