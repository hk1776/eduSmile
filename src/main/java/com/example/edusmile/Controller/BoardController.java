package com.example.edusmile.Controller;

import com.example.edusmile.Dto.BoardDTO;
import com.example.edusmile.Dto.BoardNextDTO;
import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Dto.TestResultDTO;
import com.example.edusmile.Entity.*;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final NoticeService noticeService;
    private final SummaryService summaryService;
    private final TestService testService;
    private final MemberService memberService;
    private final SubjectService subjectService;
    private final TestResultService testResultService;
    private final FreeBoardService freeBoardService;

    @GetMapping("/classList")
    public String classList(Model model,@AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        List<Subject> memberSubject = subjectService.getMemberSubject(member.getId());
        memberSubject.sort(Comparator
                .comparing(Subject::getGrade)
                .thenComparing(Subject::getDivClass));
        model.addAttribute("subjects", memberSubject);
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지


        return "classList";
    }

    @PostMapping("/notice")
    public ResponseEntity<?> submitNotice(
            @AuthenticationPrincipal UserDetails user,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestParam("content") String content,
            @RequestParam("subjectId") String subjectId) {
        boolean fileCheck = false;
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        // 파일 처리
        if (files != null && !files.isEmpty()) {
          fileCheck = true;
        }
        UUID uuid = UUID.randomUUID();
        Notice save =null;

        if(fileCheck){
             save = noticeService.save("AI",subjectId, content, member, uuid.toString());
        }else{
             save = noticeService.save("AI",subjectId, content, member, "No");
        }

        String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board","notice").toString();
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

        if(fileCheck) {
            files.forEach(file -> {
                try {
                    String originalFilename = file.getOriginalFilename();;
                    String savePath = Paths.get(projectDir, uuid+originalFilename).toString();
                    file.transferTo(new File(savePath));
                    System.out.println("파일 저장 완료: " + savePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패");
                }
            });
        }
        return ResponseEntity.ok(save.getId());
    }

    @PostMapping("/summary")
    public ResponseEntity<?> submitSummary( @AuthenticationPrincipal UserDetails user,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                            @RequestParam("content") String content,
                                            @RequestParam("subjectId") String subjectId) {
        log.info(content);
        log.info(subjectId);

        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Summary save = summaryService.save(subjectId, content, member);


        return ResponseEntity.ok(save.getId());
    }

    @PostMapping("/test")
    public ResponseEntity<?> submitTest(@AuthenticationPrincipal UserDetails user
                                        ,@RequestBody Map<String, Object> request) {
        String subjectId = (String) request.get("subjectId");
        String explain = (String) request.get("explains");
        log.info("explain"+explain);
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Gson gson = new Gson();
        List<LinkedHashMap> rawQuestions = (List<LinkedHashMap>) request.get("questions");

        String jsonQuestions = gson.toJson(rawQuestions);
        Type listType = new TypeToken<List<Classification.AnalyzeDTO.Question>>() {}.getType();
        List<Classification.AnalyzeDTO.Question> questions = gson.fromJson(jsonQuestions, listType);

        String[] explains = explain.split("\\|");

        for(int i = 0; i < explains.length-1; i++){
            if(i==0){
                explains[i] = explains[i].substring(1);
            }
            if(i>=1){
                log.info("nowEx"+explains[i]);
                explains[i] = explains[i].substring(2);
            }
            questions.get(i).setExplanation(explains[i]);
        }

        String json = gson.toJson(questions);


        Test save = testService.save(subjectId, json, member);
        log.info(questions.toString());
        log.info(subjectId);
        return ResponseEntity.ok(save.getId());
    }

    @PostMapping("/next")
    public String nextPage(@RequestParam Map<String, String> formData,
                           @AuthenticationPrincipal UserDetails user,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        MemberEntity member = memberService.memberInfo(user.getUsername());
        String classId = formData.get("classId");

        Long noticeId = parseId(formData.get("noticeId"));
        Long summaryId = parseId(formData.get("summaryId"));
        Long testId = parseId(formData.get("testId"));

        log.info("notice = {}, summary={}, test = {}", noticeId, summaryId, testId);

        boolean nothing = false;
        if(noticeId==null&&summaryId==null&&testId==null){
            nothing = true;
        }

        // Flash Attribute에 데이터 저장
        redirectAttributes.addFlashAttribute("nothing", nothing);
        redirectAttributes.addFlashAttribute("member", member);
        redirectAttributes.addFlashAttribute("noticeId", noticeId);
        redirectAttributes.addFlashAttribute("summaryId", summaryId);
        redirectAttributes.addFlashAttribute("testId", testId);

        // Subject 가져와서 Flash Attribute에 저장
        subjectService.findById(classId).ifPresent(subject -> redirectAttributes.addFlashAttribute("subject", subject));

        // 새로고침 대비해서 세션에도 데이터 저장
        session.setAttribute("nothing", nothing);
        session.setAttribute("lastMember", member);
        session.setAttribute("lastNoticeId", noticeId);
        session.setAttribute("lastSummaryId", summaryId);
        session.setAttribute("lastTestId", testId);
        subjectService.findById(classId).ifPresent(subject -> session.setAttribute("lastSubject", subject));

        return "redirect:/board/next";
    }

    @GetMapping("/next")
    public String showNextPage(HttpSession session, Model model) {
        // 세션에서 데이터 가져와 모델에 추가
        // Flash Attribute 데이터가 있으면 그대로 사용
        if (model.containsAttribute("member")) {
            return "nextPage";
        }

        // Flash Attribute가 없을 때 세션 데이터 활용 (새로고침 대비)
        boolean nothing = (boolean) session.getAttribute("nothing");
        MemberEntity lastMember = (MemberEntity) session.getAttribute("lastMember");
        Long lastNoticeId = (Long) session.getAttribute("lastNoticeId");
        Long lastSummaryId = (Long) session.getAttribute("lastSummaryId");
        Long lastTestId = (Long) session.getAttribute("lastTestId");
        Subject lastSubject = (Subject) session.getAttribute("lastSubject");

        if (lastMember != null) {
            model.addAttribute("nothing", nothing);
            model.addAttribute("member", lastMember);
            model.addAttribute("noticeId", lastNoticeId);
            model.addAttribute("summaryId", lastSummaryId);
            model.addAttribute("testId", lastTestId);
            model.addAttribute("subject", lastSubject);
        }
        return "nextPage";
    }

    private Long parseId(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            log.error("Invalid ID format: {}", id, e);
            return null;
        }
    }


    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("file") String fileName,
                                                 @RequestParam("type") String type) {
        String FILE_DIR = System.getProperty("user.dir")
                + File.separator + "file"
                + File.separator + "board"
                + File.separator + type
                + File.separator;
        log.info(FILE_DIR);
        try {
            // 파일 경로 설정
            Path filePath = Paths.get(FILE_DIR).resolve(fileName).normalize();

            // 파일 리소스 준비
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists() && resource.isReadable()) {
                // 파일 다운로드를 위해 UTF-8로 인코딩한 파일 이름을 헤더에 설정
                String newFileName = fileName.substring(36); // UUID 길이를 잘라낸 나머지 부분이 원본 파일 이름

                // 새로운 파일 이름을 UTF-8로 인코딩하여 헤더에 설정
                String encodedFileName = URLEncoder.encode(newFileName, StandardCharsets.UTF_8.toString())
                        .replaceAll("\\+", "%20");  // +를 %20으로 변경

                // 파일을 다운로드할 수 있도록 설정
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                        .body(resource);
            } else {
                // 파일을 찾을 수 없으면 404 에러
                throw new FileNotFoundException("파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
