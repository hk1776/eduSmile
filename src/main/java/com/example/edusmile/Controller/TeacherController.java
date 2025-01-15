package com.example.edusmile.Controller;


import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/teacher")
public class TeacherController {
    private final MemberService memberService;

    @GetMapping()
    public String home(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "teacher";
        }
    }

    @GetMapping("/class")
    public String record(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "class";
        }
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails user) {

        MemberEntity member  = memberService.memberInfo(user.getUsername());
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else {
            if (!file.getContentType().equals("audio/mp3")) {
                log.info("파일 형식 에러" + file.getContentType());
            }
            log.info("파일 이름 = {} 파일 정보 = {} 파일 사이즈 = {}",file.getOriginalFilename(), file.getContentType(), file.getSize());

//            // FastAPI 서버로 파일 전송
//            String fastApiUrl = "";
//
//            RestTemplate restTemplate = new RestTemplate();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            // Request body에 파일과 위치 데이터(emer) 추가
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("file", file.getResource());
//
//            // HTTP 요청 생성
//            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl, requestEntity, String.class);
//            String responseBody = response.getBody();
//
//            log.info("음성 응답" + responseBody.toString());
//
//            // 받은 JSON 결과를 Java 객체로 변환
//            System.out.println(responseBody);

            return "class";  // 응급 상황 처리 결과 화면으로 이동
        }
    }
}
