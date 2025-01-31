package com.example.edusmile.Controller;


import com.example.edusmile.Entity.CounselEntity;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.CounselRepository;
import com.example.edusmile.Repository.MemberRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
public class CounselRestController {


    private final MemberRepository memberRepository;
    private final CounselRepository counselRepository;

    public CounselRestController(MemberRepository memberRepository, CounselRepository counselRepository) {
        this.memberRepository = memberRepository;
        this.counselRepository = counselRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("studentId") Long studentId) throws IOException {


        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"message\": \"파일이 없습니다.\"}");
        }

        // PDF 파일만 허용
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body("{\"message\": \"PDF 파일만 업로드 가능합니다.\"}");
        }


        // 파일을 ByteArrayResource로 변환 (InputStream을 한 번만 읽도록 변경)
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
                "http://127.0.0.1:8000/process_pdf/", HttpMethod.POST, requestEntity, String.class);

        // JSON 응답을 그대로 반환 (String 형태)
        String fastResult = response.getBody();

        Optional<MemberEntity> st = memberRepository.findById(studentId);



        MemberEntity student = st.get();

        List<CounselEntity> duplicate = counselRepository.duplicateContent(student.getName());

        if(!duplicate.isEmpty())
        {
            CounselEntity duple = duplicate.get(0);
            duple.setCounsel(fastResult);
            counselRepository.save(duple);

        }
        else {
            CounselEntity counselEntity = new CounselEntity();

            counselEntity.setClassId(student.getTeacherCode());
            counselEntity.setCounsel(fastResult);
            counselEntity.setTitle(student.getName() + " 님의 생활기록부 분석 내용");
            counselEntity.setStudent(student.getName());
            counselEntity.setViews(0);
            counselRepository.save(counselEntity);
        }






        return ResponseEntity.ok("{\"message\": \"파일 업로드 성공\"}");
    }
}

