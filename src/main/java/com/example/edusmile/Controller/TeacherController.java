package com.example.edusmile.Controller;
import com.example.edusmile.Config.ClovaSpeechClient;
import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Entity.ClassLog;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.*;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/teacher")
public class TeacherController {
    private final MemberService memberService;
    private final SubjectService subjectService;


    private final NoticeService noticeService;
    private final FreeBoardService freeBoardService;
    private final SummaryService summaryService;
    private final TestService testService;
    private final TestResultService testResultService;
    private final AttendService attendService;
    private final ClassLogService classLogService;

    @GetMapping()
    public String home(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        List<Subject> subjects = subjectService.getMemberSubject(member.getId());
        subjects.sort(Comparator
                .comparing(Subject::getGrade) // 이름 기준 오름차순
                .thenComparing(Subject::getDivClass));
        List<MemberEntity> student = memberService.myStudent(member.getTeacherCode());

        if(!subjects.isEmpty()){
            model.addAttribute("firstSubject", subjects.get(0));
        }else {
            model.addAttribute("firstSubject", false);
        }
        model.addAttribute("student", student);
        model.addAttribute("member", member);
        model.addAttribute("subjects", subjects);
        model.addAttribute("subLen",subjects.size());
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

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

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "class";
        }
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails user,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) throws IOException {

        MemberEntity member  = memberService.memberInfo(user.getUsername());
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else {
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
            Classification.AnalyzeDTO send = gson.fromJson(response.getBody(), Classification.AnalyzeDTO.class);
            log.info("FastAPI 응답 객체: {}", send);

            log.info("받아온 값 :"+response.toString());
            List<Subject> subjects = subjectService.getMemberSubject(member.getId());
            subjects.sort(Comparator
                    .comparing(Subject::getGrade) // 이름 기준 오름차순
                    .thenComparing(Subject::getDivClass));

            List<String>explains = new ArrayList<>();
            for(Classification.AnalyzeDTO.Quiz.Text.Question i : send.getQuiz().getText().getQuestions()){
                explains.add(i.getExplanation()+"|");
                log.info("inputExplain"+i.getExplanation());
            }

            Path dirPath = Paths.get(projectDir);
            String uploadedFileName = file.getOriginalFilename();

            try (Stream<Path> files = Files.list(dirPath)) {
                ClassLog classLog= new ClassLog();
                classLog.setTeacherId(member.getId());
                classLog.setTeacherName(member.getName());
                classLog.setSchoolName(member.getSchool());
                classLog.setPhoneNumber(member.getPhoneNumber());
                classLog.setDate(LocalDate.now());
                classLog.setFileSize(getFileSizeReadable(file.getSize()));
                classLogService.save(classLog);

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

            redirectAttributes.addFlashAttribute("explains", explains);
            redirectAttributes.addFlashAttribute("subject", subjects);
            redirectAttributes.addFlashAttribute("stt", stt.getText());
            redirectAttributes.addFlashAttribute("response", send);
            redirectAttributes.addFlashAttribute("member", member);
            redirectAttributes.addFlashAttribute("teacher", member.getRole().equals("teacher"));
            session.setAttribute("lastExplains", explains);
            session.setAttribute("lastUploadedResponse", send);
            session.setAttribute("lastUploadedSTT", stt.getText());
            session.setAttribute("lastSubjects", subjects);
            session.setAttribute("lastMember", member);
            return "redirect:/teacher/upload";
        }
    }
    @GetMapping("/upload")
    public String redirectUpload(Model model,@AuthenticationPrincipal UserDetails user,HttpSession session) {

        // Flash Attribute 데이터 가져오기
        if (model.containsAttribute("response")) {
            return "classResult";
        }
        // Flash Attribute가 없을 때 세션 데이터 활용 (새로고침 대비)
        Classification.AnalyzeDTO lastResponse = (Classification.AnalyzeDTO) session.getAttribute("lastUploadedResponse");
        List<Subject> lastSubjects = (List<Subject>) session.getAttribute("lastSubjects");
        String lastSTT = (String) session.getAttribute("lastUploadedSTT");
        MemberEntity lastMember = (MemberEntity) session.getAttribute("lastMember");
        List<String> lastExplains = (List<String>) session.getAttribute("lastExplains");

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
        return "classResult";
    }

    @PostMapping("/classAdd")
    public String addClass(@RequestParam("grade") String grade, @RequestParam("class") String divClass,
                           @RequestParam("subject") String subject, Model model,@AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        int gradeId = Integer.parseInt(grade);
        subjectService.save(member, subject,gradeId, divClass);
        model.addAttribute("message", "수업이 성공적으로 추가되었습니다!");
        return "redirect:/teacher"; // 리다이렉트할 URL
    }

    @PostMapping("/classUpdate")
    public String updateClass(@RequestParam("updateCode") String code,@RequestParam("updateGrade") String grade, @RequestParam("updateClass") String divClass,
                           @RequestParam("updateSubject") String subject, Model model,@AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        int gradeId = Integer.parseInt(grade);
        subjectService.update(code, subject,gradeId, divClass);
        model.addAttribute("message", "수업이 성공적으로 수정되었습니다!");
        return "redirect:/teacher"; // 리다이렉트할 URL
    }

    @PostMapping("/classDelete")
    public String deleteClass(@RequestParam("deleteCode") String code, Model model,@AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());

        subjectService.delete(code);

        freeBoardService.deleteByClassId(code);
        noticeService.deleteByClassId(code);
        summaryService.deleteByClassId(code);
        testService.deleteByClassId(code);
        testResultService.deleteByClassId(code);
        model.addAttribute("message", "수업이 성공적으로 수정되었습니다!");
        return "redirect:/teacher"; // 리다이렉트할 URL
    }
    @PostMapping("/studentDel")
    public String studentDel(@RequestParam("studentId") Long id, Model model,@AuthenticationPrincipal UserDetails user) {
        log.info("받아온 값"+id);

        memberService.studentClassDel(id);
        attendService.memberDelete(id);
        model.addAttribute("message", "학생을 성공적으로 반에서 제외하였습니다!");
        return "redirect:/teacher"; // 리다이렉트할 URL
    }


    @PostMapping("/classManage")
    public String classManage(@RequestParam Long id,
                              @RequestParam String school,
                              @RequestParam int schoolgrade,
                              @RequestParam int schoolClass,
                              RedirectAttributes redirectAttributes) {
        memberService.changeClass(id, school, schoolgrade, schoolClass);

        return "redirect:/teacher";
    }

    public static String getFileSizeReadable(long size) {
        if (size >= 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else if (size >= 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return size + " bytes";
        }
    }
}
