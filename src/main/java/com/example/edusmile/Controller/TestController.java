package com.example.edusmile.Controller;

import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Dto.TestResultDTO;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Test;
import com.example.edusmile.Entity.TestResult;
import com.example.edusmile.Service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board")
public class TestController {
    private final TestService testService;
    private final MemberService memberService;
    private final TestResultService testResultService;

    @GetMapping("/testList")
    public String testList(@RequestParam("id") String subjectId,
                           @RequestParam(value = "page", defaultValue = "1") int page,
                           Model model,
                           @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc("id")));

        Page<Test> testPage  = testService.findByClassId(subjectId, pageable);

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지


        model.addAttribute("teacher",member.getRole().equals("teacher"));
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

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지


        model.addAttribute("teacher",member.getRole().equals("teacher"));

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
        model.addAttribute("author", Objects.equals(test.getMemberId(), member.getId()));
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

    @DeleteMapping("/test/delete")
    public ResponseEntity<?> testSummary(@RequestParam("num") Long id) {

        testService.delete(id);
        return ResponseEntity.ok().build();
    }
}
