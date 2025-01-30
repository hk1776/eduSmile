package com.example.edusmile.Controller;

import com.example.edusmile.Dto.BoardNextDTO;
import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Dto.TestResultDTO;
import com.example.edusmile.Entity.*;
import com.example.edusmile.Service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final TestResultService testResultService;

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
        Notice save =null;
        if(fileCheck){
             save = noticeService.save(subjectId, content, member.getId(), uuid.toString());
        }else{
             save = noticeService.save(subjectId, content, member.getId(), "No");
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
        Summary save = summaryService.save(subjectId, content, member.getId());


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


        Test save = testService.save(subjectId, json, member.getId());
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

        // Flash Attribute에 데이터 저장
        redirectAttributes.addFlashAttribute("member", member);
        redirectAttributes.addFlashAttribute("noticeId", noticeId);
        redirectAttributes.addFlashAttribute("summaryId", summaryId);
        redirectAttributes.addFlashAttribute("testId", testId);

        // Subject 가져와서 Flash Attribute에 저장
        subjectService.findById(classId).ifPresent(subject -> redirectAttributes.addFlashAttribute("subject", subject));

        // 새로고침 대비해서 세션에도 데이터 저장
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
        MemberEntity lastMember = (MemberEntity) session.getAttribute("lastMember");
        Long lastNoticeId = (Long) session.getAttribute("lastNoticeId");
        Long lastSummaryId = (Long) session.getAttribute("lastSummaryId");
        Long lastTestId = (Long) session.getAttribute("lastTestId");
        Subject lastSubject = (Subject) session.getAttribute("lastSubject");

        if (lastMember != null) {
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
        Integer prevPageNum = (noticePage.hasPrevious()) ? page - 1 : null;
        Integer nextPageNum = (noticePage.hasNext()) ? page + 1 : null;

        model.addAttribute("member", member);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("noticePage", noticePage);
        model.addAttribute("pageNums", pageNums);
        model.addAttribute("prevPageNum", prevPageNum);
        model.addAttribute("nextPageNum", nextPageNum);

        return "notice";
    }
    @GetMapping("/notice")
    public String notice(@RequestParam("id") String subjectId,
                         @RequestParam("num") Long id,
                         Model model,
                         @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());

        Notice notice = noticeService.findById(id);
        String file = notice.getFile();

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
        MemberEntity member = memberService.memberInfo(user.getUsername());
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("created").descending());

        Page<Summary> summaryPage = summaryService.findByClassId(subjectId, pageable);

        // 페이지 번호 리스트 계산
        List<Integer> pageNums = new ArrayList<>();
        for (int i = 1; i <= summaryPage.getTotalPages(); i++) {
            pageNums.add(i);
        }

        // 이전/다음 페이지 번호 계산
        Integer prevPageNum = (summaryPage.hasPrevious()) ? page - 1 : null;
        Integer nextPageNum = (summaryPage.hasNext()) ? page + 1 : null;

        model.addAttribute("member", member);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("summaryPage", summaryPage);
        model.addAttribute("pageNums", pageNums);
        model.addAttribute("prevPageNum", prevPageNum);
        model.addAttribute("nextPageNum", nextPageNum);

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
        Integer prevPageNum = (testPage.hasPrevious()) ? page - 1 : null;
        Integer nextPageNum = (testPage.hasNext()) ? page + 1 : null;

        model.addAttribute("member", member);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("testPage", testPage);
        model.addAttribute("pageNums", pageNums);
        model.addAttribute("prevPageNum", prevPageNum);
        model.addAttribute("nextPageNum", nextPageNum);
        return "test";
    }
    @GetMapping("/test")
    public String test(@RequestParam("id") String subjectId,
                       @RequestParam("num") Long id,
                       Model model,
                       @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Test test = testService.findById(id);

        boolean saveCheck = false;
        TestResult result=null;
        List<Integer> selects = new ArrayList<>();

        List<TestResult> testResult = testResultService.findByTestId(id);
        if (!testResult.isEmpty()) {
            for(TestResult t : testResult) {
                if(t.getMemberId()==member.getId()){
                    result = t;
                }
            }
            Gson gson = new Gson();

            if(result!=null){
                Type listType = new TypeToken<List<TestResultDTO.AnswerData>>(){}.getType();
                List<TestResultDTO.AnswerData> answers = gson.fromJson(result.getResult(), listType);
                for(TestResultDTO.AnswerData a : answers) {
                    selects.add(Integer.parseInt(a.getSelectedAnswer()));
                }
                saveCheck = true;
            }


        }

        Gson gson = new Gson();
        List<Classification.AnalyzeDTO.Question> questions = new ArrayList<>();

        try {
            String examJson = test.getExam();

            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> examData = gson.fromJson(examJson, type);

            for (Map<String, Object> entry : examData) {
                String questionText = (String) entry.get("question");
                List<String> choices = (List<String>) entry.get("choices");
                String explanation = (String) entry.get("explanation");
                Object answerObj = entry.get("answer");
                int answer = 0;
                if (answerObj instanceof Number) {
                    answer = ((Number) answerObj).intValue();
                } else if (answerObj instanceof String) {
                    answer = Integer.parseInt((String) answerObj);
                }

                // Question 객체 생성 후 리스트에 추가
                Classification.AnalyzeDTO.Question question =
                        new Classification.AnalyzeDTO.Question(questionText, choices, answer, explanation);
                questions.add(question);
                log.info("question= {}",question.toString());
            }
        } catch (Exception e) {
            log.error("Error parsing exam JSON: {}", e.getMessage());
        }

        // 로그로 test.exam 정보 출력 (디버그용)
        log.info("test exam: {}", test.getExam());
        model.addAttribute("selects", selects);
        model.addAttribute("result", result);
        model.addAttribute("saveCheck", saveCheck);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("member", member);
        model.addAttribute("test", test);
        model.addAttribute("questions", questions); // 문제 리스트 추가

        // 'testDetail' 페이지로 이동
        return "testDetail";
    }

    @PostMapping("/testSave")
    public ResponseEntity<String> saveTestResult(@RequestBody TestResultDTO testResult) {
        List<TestResultDTO.AnswerData> answers = testResult.getAnswers();
        Gson gson = new Gson();
        String jsonAnswer = gson.toJson(testResult.getAnswers());
        testResultService.save(testResult.getTestId(),testResult.getClassId(),testResult.getMemberId(),jsonAnswer,testResult.getScore());
        return ResponseEntity.ok("채점 결과가 서버에 저장되었습니다.");
    }
}
