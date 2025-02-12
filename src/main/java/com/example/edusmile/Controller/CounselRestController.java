package com.example.edusmile.Controller;


import com.example.edusmile.Entity.CounselEntity;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.CounselRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.CounselService;
import com.example.edusmile.Service.MemberService;
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
import java.time.LocalDate;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class CounselRestController {




    private final CounselService counselService;

    private final MemberService memberService;

    public  String escapeJsonString(String input) {
        return input.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @GetMapping("/api/counsel/{CounselId}")
    public ResponseEntity<String> getStudentCounsel(@PathVariable Long CounselId) {
        CounselEntity counselEntity = counselService.getCounselById(CounselId);
        String counsel1 = counselEntity.getCounsel();

        MemberEntity member = memberService.memberInfo(counselEntity.getLoginId());


        List<CounselEntity> recode = counselService.getCounselsOrRecode(member.getLoginId(),"record");

        String c = "";
        String answerJson = "";
        String escapedContent = escapeJsonString(counsel1);

        String escapedAnswer0 = " ";
        String escapedAnswer1 = " ";
        String escapedAnswer2 = " ";

        answerJson = String.format("[\"%s\", \"%s\", \"%s\"]", escapedAnswer0, escapedAnswer1, escapedAnswer2);

        if(recode.isEmpty()) {




        }
        else {
            c = recode.get(0).getCounsel();

            String[] answer = new String[3];

            answer[0] = c.split(" 8\\.")[0];
            answer[1] = c.split(" 9\\.")[0].split(" 8\\.")[1];
            answer[1] = answer[1].substring(6);
            answer[2] = c.split(" 9\\.")[1];
            answer[2] = answer[2].substring(6);


            // 각 항목을 이스케이프 처리

              escapedAnswer0 = escapeJsonString(answer[0]);
              escapedAnswer1 = escapeJsonString(answer[1]);
              escapedAnswer2 = escapeJsonString(answer[2]);



            // answer 배열을 JSON 형식으로 변환
             answerJson = String.format("[\"%s\", \"%s\", \"%s\"]", escapedAnswer0, escapedAnswer1, escapedAnswer2);
        }
        // JSON 객체로 응답을 반환

        String jsonResponse = String.format("{\"content\": \"%s\", \"answer\": %s}", escapedContent, answerJson);
        return ResponseEntity.ok(jsonResponse);
    }


    @GetMapping("/api/record/{studentId}")
    public ResponseEntity<String[]> getStudentRecord(@PathVariable Long studentId) {

        Optional<MemberEntity> mem = memberService.findById(studentId);
        if(mem.isPresent()) {
            MemberEntity member =mem.get();
            List<CounselEntity> counsel = counselService.getCounselsOrRecode(member.getLoginId(),"record");
            String c = counsel.get(0).getCounsel();

            String[] answer = new String[3];

            answer[0] = c.split(" 8\\.")[0];
            answer[1] = c.split(" 9\\.")[0].split(" 8\\.")[1];
            answer[1] = answer[1].substring(6);
            answer[2] = c.split(" 9\\.")[1];
            answer[2] = answer[2].substring(6);

            return ResponseEntity.ok(answer);
        }
        else
            return null;

    }

    @Transactional
    @PostMapping("/counsel/save")
    public ResponseEntity<?> saveCounsel(@RequestParam("content") String content , @RequestParam("id") Long id) {

        LocalDate today = LocalDate.now();

        MemberEntity member = memberService.findById(id).get();

        CounselEntity counsel = new CounselEntity();

        counsel.setCounsel(content);
        counsel.setClassId(member.getTeacherCode());
        counsel.setStudent(member.getName());
        counsel.setType("counsel");
        counsel.setTitle("상담 : " + today);
        counsel.setLoginId(member.getLoginId());
        counselService.saveCounsel(counsel);

        return ResponseEntity.ok("ok");
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


}
