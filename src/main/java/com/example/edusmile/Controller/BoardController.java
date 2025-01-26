package com.example.edusmile.Controller;

import com.example.edusmile.Dto.BoardNextDTO;
import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Entity.*;
import com.example.edusmile.Service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

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

    @GetMapping("/classList")
    public String classList(Model model,@AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        List<Subject> memberSubject = subjectService.getMemberSubject(member.getId());
        memberSubject.sort(Comparator
                .comparing(Subject::getGrade)
                .thenComparing(Subject::getDivClass));
        model.addAttribute("subjects", memberSubject);
        model.addAttribute("member", member);
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
        if(fileCheck){
            Notice save = noticeService.save(subjectId, content, member.getId(), uuid.toString());
        }else{
            Notice save = noticeService.save(subjectId, content, member.getId(), "No");
        }

        String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board").toString();
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
        return ResponseEntity.ok("공지사항 등록 성공");
    }

    @PostMapping("/summary")
    public ResponseEntity<?> submitSummary( @AuthenticationPrincipal UserDetails user,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                            @RequestParam("content") String content,
                                            @RequestParam("subjectId") String subjectId) {
        log.info(content);
        log.info(subjectId);

        MemberEntity member  = memberService.memberInfo(user.getUsername());
        summaryService.save(subjectId,content,member.getId());


        return ResponseEntity.ok("수업요약 등록 성공");
    }

    @PostMapping("/test")
    public ResponseEntity<?> submitTest(@AuthenticationPrincipal UserDetails user
                                        ,@RequestBody Map<String, Object> request) {
        String subjectId = (String) request.get("subjectId");
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        List<Classification.AnalyzeDTO.Quiz> questions = (List<Classification.AnalyzeDTO.Quiz>) request.get("questions");
        Gson gson = new Gson();
        String json = gson.toJson(questions);
        testService.save(subjectId,json,member.getId());
        log.info(questions.toString());
        log.info(subjectId);
        return ResponseEntity.ok("시험 등록 성공");
    }

    @PostMapping("/next")
    public String nextPage(@RequestParam Map<String, String> formData,@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());

        String classId = formData.get("classId");
        boolean notice = Boolean.parseBoolean(formData.get("notice"));
        boolean summary = Boolean.parseBoolean(formData.get("summary"));
        boolean test = Boolean.parseBoolean(formData.get("test"));
        log.info("notice = {}, summary = {}, test={}, id={}",notice, summary, test,classId);

        Optional<Subject> subject = subjectService.findById(classId);


        model.addAttribute("member", member);
        model.addAttribute("notice", notice);
        model.addAttribute("summary",summary);
        model.addAttribute("test", test);
        model.addAttribute("subject", subject.get());

        return "nextPage";
    }


    @GetMapping("/noticeList")
    public String noticeList(@RequestParam("id") String subjectId,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             Model model,
                             @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("created").descending());

        Page<Notice> noticePage  = noticeService.findByClassId(subjectId, pageable);

        log.info("page: " + noticePage.getContent().toString());

        // 페이지 번호 리스트 계산
        List<Integer> pageNums = new ArrayList<>();
        for (int i = 1; i <= noticePage.getTotalPages(); i++) {
            pageNums.add(i);
        }

        model.addAttribute("member", member);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("noticePage", noticePage);
        model.addAttribute("pageNums", pageNums);

        return "notice";
    }
    @GetMapping("/notice")
    public String notice(@RequestParam("id") String subjectId,
                         @RequestParam("num") Long id,
                         Model model,
                         @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());

        Notice notice = noticeService.findById(id);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("member", member);
        model.addAttribute("notice", notice);
        return "noticeDetail";
    }

    @GetMapping("/summaryList")
    public String summaryList(@RequestParam("id") String subjectId,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             Model model,
                             @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("created").descending());

        Page<Summary> summaryPage  = summaryService.findByClassId(subjectId, pageable);

        // 페이지 번호 리스트 계산
        List<Integer> pageNums = new ArrayList<>();
        for (int i = 1; i <= summaryPage.getTotalPages(); i++) {
            pageNums.add(i);
        }

        model.addAttribute("member", member);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("summaryPage", summaryPage);
        model.addAttribute("pageNums", pageNums);

        return "summary";
    }
    @GetMapping("/summary")
    public String summary(@RequestParam("id") String subjectId,
                         @RequestParam("num") Long id,
                         Model model,
                         @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());

        Summary summary = summaryService.findById(id);
        log.info("summary= {}",summary.getSummary());
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("member", member);
        model.addAttribute("summary", summary);
        return "summaryDetail";
    }

    @GetMapping("/testList")
    public String testList(@RequestParam("id") String subjectId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              Model model,
                              @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("created").descending());

        Page<Test>testPage  = testService.findByClassId(subjectId, pageable);

        // 페이지 번호 리스트 계산
        List<Integer> pageNums = new ArrayList<>();
        for (int i = 1; i <= testPage.getTotalPages(); i++) {
            pageNums.add(i);
        }

        model.addAttribute("member", member);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("testPage", testPage);
        model.addAttribute("pageNums", pageNums);

        return "test";
    }
    @GetMapping("/test")
    public String test(@RequestParam("id") String subjectId,
                       @RequestParam("num") Long id,
                       Model model,
                       @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Test test = testService.findById(id);

        Gson gson = new Gson();
        List<Classification.AnalyzeDTO.Question> questions = new ArrayList<>();

        try {
            // test.getExam()이 반환하는 JSON 문자열을 List<Map<String, Object>>로 파싱
            String examJson = test.getExam();

            // JSON 문자열을 List<Map<String, Object>>로 변환
            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> examData = gson.fromJson(examJson, type);

            // 각 문제를 Question 객체로 변환
            for (Map<String, Object> entry : examData) {
                String questionText = (String) entry.get("question");  // questionText 대신 question 사용
                List<String> choices = (List<String>) entry.get("choices");
                int answer = Integer.parseInt((String) entry.get("answer"));  // answer를 String에서 Integer로 변환
                String explanation = (String) entry.get("explanation");

                // Question 객체 생성 후 리스트에 추가
                Classification.AnalyzeDTO.Question question =
                        new Classification.AnalyzeDTO.Question(questionText, choices, answer, explanation);
                questions.add(question);
            }
        } catch (Exception e) {
            log.error("Error parsing exam JSON: {}", e.getMessage());
        }

        // 로그로 test.exam 정보 출력 (디버그용)
        log.info("test exam: {}", test.getExam());

        model.addAttribute("subjectId", subjectId);
        model.addAttribute("member", member);
        model.addAttribute("test", test);
        model.addAttribute("questions", questions); // 문제 리스트 추가

        // 'testDetail' 페이지로 이동
        return "testDetail";
    }
}
