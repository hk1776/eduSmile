package com.example.edusmile.Controller;


import com.example.edusmile.Config.ClovaSpeechClient;
import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Entity.CounselEntity;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Entity.Summary;
import com.example.edusmile.Repository.CounselRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.CounselService;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.SubjectService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Member;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CounselController {

    private final MemberService memberService;
    private final SubjectService subjectService;
    private final CounselService counselService;

    @GetMapping("/teacher/student_recode")
    public String student_recode(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        List<MemberEntity> students = memberService.FindbyRoleTcode("student",member.getTeacherCode());  //자기반학생찾기
        model.addAttribute("students", students);
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "student_recode";
        }
    }

    @GetMapping("/teacher/student_recode/{id}")
    public String student_recode_detail(@AuthenticationPrincipal UserDetails user,@PathVariable Long    id, Model model) {



        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Optional<MemberEntity> stu = memberService.findById(id);
        if(stu.isPresent()) {
            MemberEntity  student = stu.get();
            model.addAttribute("student", student);
            List<CounselEntity> counsels = counselService.getCounselsOrRecode(student.getLoginId(),"counsel");
            model.addAttribute("member", member);

            model.addAttribute("teacher",member.getRole().equals("teacher"));

            model.addAttribute("counsels", counsels);

            List<CounselEntity> record =  counselService.getCounselsOrRecode(student.getLoginId(),"record");

            if(!record.isEmpty()) {
                record.get(0).setTitle(record.get(0).getCounsel().replace("\n", "<br>"));
                model.addAttribute("record", record.get(0).getCounsel());

            }
        }

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지


        List<MemberEntity> students = memberService.FindbyRoleTcode("student",member.getTeacherCode());  //자기반학생찾기
        model.addAttribute("students", students);
        if(!member.getRole().equals("teacher")) {
            return "student_recode_detail_for_student";
        }else{
            return "student_recode_detail";
        }
    }


    @GetMapping("/counsel/Audio")
    public String record(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "counselAudio";
        }
    }

    @PostMapping("/counsel/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails user,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) throws IOException {

        MemberEntity member  = memberService.memberInfo(user.getUsername());
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else {

            if (!Objects.equals(file.getContentType(), "audio/mp3")) {
                log.info("파일 형식 에러" + file.getContentType());
            }
            log.info("파일 이름 = {} 파일 정보 = {} 파일 사이즈 = {} 파일 타입 = {}",file.getOriginalFilename(), file.getContentType(), file.getSize(),file.getContentType());
            String projectDir = Paths.get(System.getProperty("user.dir"), "file", "audio").toString();
            File directory = new File(projectDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs(); // 폴더 생성
                if (created) {
                    System.out.println("디렉토리 생성 성공: " + projectDir);
               } else {
                    System.err.println("디렉토리 생성 실패!");
                }
            } else {
                System.out.println("디렉토리가 이미 존재합니다: " + projectDir);
            }
           File convert = new File(projectDir, Objects.requireNonNull(file.getOriginalFilename()));
            log.info(convert.getPath());
           file.transferTo(convert);

            final ClovaSpeechClient clovaSpeechClient = new ClovaSpeechClient();
            ClovaSpeechClient.NestRequestEntity requestEntity = new ClovaSpeechClient.NestRequestEntity();
            final String result =
                    clovaSpeechClient.upload(convert, requestEntity);
            JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
            String textValue = jsonObject.get("text").getAsString();
            Classification.STT stt = new Classification.STT();
            stt.setText(textValue);
            log.info("request :"+stt.getText());



            String apiUrl = "";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", stt.getText());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            log.info("API Response Body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("FastAPI 요청 실패: {}", response.getStatusCode());
                return "main";
            }
            Gson gson = new Gson();

            System.out.println(response.getBody());
            Classification.Counsel send = gson.fromJson(response.getBody(), Classification.Counsel.class);
            log.info("FastAPI 응답 객체: {}", send);

            log.info("받아온 값 :"+send.getSummary());
            List<Subject> subjects = subjectService.getMemberSubject(member.getId());
            subjects.sort(Comparator
                    .comparing(Subject::getGrade) // 이름 기준 오름차순
                    .thenComparing(Subject::getDivClass));

            Path dirPath = Paths.get(projectDir);
            String uploadedFileName = file.getOriginalFilename();

            try (Stream<Path> files = Files.list(dirPath)) {
                files
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(name -> name.equals(uploadedFileName))
                        .findFirst()
                        .ifPresent(matchedFileName -> {
                            try {
                                Path oldFilePath = Paths.get(projectDir, matchedFileName);
                                Files.deleteIfExists(oldFilePath);
                                log.info("삭제된 파일: " + matchedFileName);
                            } catch (IOException e) {
                                log.error("파일 삭제 실패: " + matchedFileName, e);
                            }
                        });
            } catch (IOException e) {
                log.error("디렉토리 조회 실패", e);
            }


            redirectAttributes.addFlashAttribute("subject", subjects);
            redirectAttributes.addFlashAttribute("stt", stt.getText());
            redirectAttributes.addFlashAttribute("response", send.getSummary());
            redirectAttributes.addFlashAttribute("member", member);
            redirectAttributes.addFlashAttribute("teacher", member.getRole().equals("teacher"));
            session.setAttribute("lastUploadedResponse", send.getSummary());
            session.setAttribute("lastUploadedSTT", stt.getText());
            session.setAttribute("lastSubjects", subjects);
            session.setAttribute("lastMember", member);


            return "redirect:/counsel/upload";
        }
    }

    @GetMapping("/counsel/upload")
    public String redirectUpload(Model model,@AuthenticationPrincipal UserDetails user,HttpSession session) {

        // Flash Attribute 데이터 가져오기
        if (model.containsAttribute("response")) {
            return "counselResult";
        }


        // Flash Attribute가 없을 때 세션 데이터 활용 (새로고침 대비)
        String lastResponse = (String) session.getAttribute("lastUploadedResponse");
        List<Subject> lastSubjects = (List<Subject>) session.getAttribute("lastSubjects");
        String lastSTT = (String) session.getAttribute("lastUploadedSTT");
        MemberEntity lastMember = (MemberEntity) session.getAttribute("lastMember");
        List<String> lastExplains = (List<String>) session.getAttribute("lastExplains");

        log.info("lastResponse: "+lastResponse);
        if(lastExplains != null){
            model.addAttribute("explains", lastExplains);
        }
        if (lastResponse != null) {
            model.addAttribute("response", lastResponse);
            model.addAttribute("stt", lastSTT);
        }
        if (lastSubjects != null) {
            model.addAttribute("subject", lastSubjects);
        }
        if (lastMember != null) {
            model.addAttribute("member", lastMember);
        }


        MemberEntity member = memberService.memberInfo(user.getUsername());



        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberService.FindbyRoleTcode("teacher",member.getTeacherCode());
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지
        List<MemberEntity> students = memberService.FindbyRoleTcode("student",member.getTeacherCode());

        model.addAttribute("students", students);

        return "counselResult";
    }

    @PostMapping("/counsel/summary")
    public ResponseEntity<?> submitSummary( @AuthenticationPrincipal UserDetails user,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                            @RequestParam("content") String content,
                                            @RequestParam("subjectId") String subjectId) {
        log.info(content);
        log.info(subjectId);

        return ResponseEntity.ok("ok");
    }

}
