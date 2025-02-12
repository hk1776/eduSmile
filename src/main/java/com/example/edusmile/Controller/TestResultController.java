package com.example.edusmile.Controller;

import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Dto.SubjectDTO;
import com.example.edusmile.Dto.TestResultDTO;
import com.example.edusmile.Entity.*;
import com.example.edusmile.Repository.AttendRepository;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Repository.TestResultRepository;
import com.example.edusmile.Service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Type;
import java.util.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/testResult")
public class TestResultController {
    private final MemberService memberService;

    private final SubjectService subjectService;
    private final TestResultService testResultService;
    private final TestService testService;

    @GetMapping("/list")
    public String myClassList(@RequestParam("id") String subjectId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              Model model,
                              @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc("id")));

        Page<Test>testPage  = testService.findByClassId(subjectId, pageable);

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지
        // 페이지 번호 리스트 계산
        List<Integer> pageNums = new ArrayList<>();
        for (int i = 1; i <= testPage.getTotalPages(); i++) {
            pageNums.add(i);
        }
        Integer prevPageNum = (testPage.hasPrevious()) ? page - 1 : null;
        Integer nextPageNum = (testPage.hasNext()) ? page + 1 : null;

        List<Subject> subjects = subjectService.getMemberSubject(member.getId());
        subjects.sort(Comparator
                .comparing(Subject::getGrade) // 이름 기준 오름차순
                .thenComparing(Subject::getDivClass));

        SubjectDTO dto = new SubjectDTO(subjectService.findById(subjectId).get());


        model.addAttribute("subjects", subjects);
        model.addAttribute("thisSubject", dto);
        model.addAttribute("member", member);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("testPage", testPage);
        model.addAttribute("pageNums", pageNums);
        model.addAttribute("prevPageNum", prevPageNum);
        model.addAttribute("nextPageNum", nextPageNum);
        return "testListForTeacher";
    }

    @GetMapping("/result")
    public String result(@RequestParam("id") String subjectId,
                       @RequestParam("num") Long id,
                       Model model,
                       @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Test test = testService.findById(id);

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지


        List<TestResult> testResult = testResultService.findByTestId(id);
        int score = 0;
        double []rate = {0,0,0,0,0};
        List<MemberEntity>members = new ArrayList<>();

        if (!testResult.isEmpty()) {
            Gson gson = new Gson();
            for(TestResult t : testResult) {
                members.add(memberService.findById(t.getMemberId()).get());
                log.info("멤버 = {},시험 ={}",t.getMemberId(),t.getTestId());
                score += t.getScore();
                Type listType = new TypeToken<List<TestResultDTO.AnswerData>>(){}.getType();
                List<TestResultDTO.AnswerData> answers = gson.fromJson(t.getResult(), listType);
                for(int i = 0 ;i<answers.size();i++) {
                    if(answers.get(i).getScore()==20){
                        rate[i]=rate[i]+1;
                    }
                }
            }
            model.addAttribute("meanScore", score/testResult.size());
        }else{
            model.addAttribute("meanScore", 0);
        }

        for(int i = 0 ;i<5;i++) {
            rate[i] = (rate[i]/testResult.size())*100;
        }

        Gson gson = new Gson();
        List<Classification.AnalyzeDTO.Question> questions = new ArrayList<>();

        try {
            String examJson = test.getExam();

            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> examData = gson.fromJson(examJson, type);
            int index = 0;
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
                        new Classification.AnalyzeDTO.Question(questionText, choices, answer, explanation,rate[index]);
                questions.add(question);
                index++;
                log.info("question= {}",question.toString());
            }
        } catch (Exception e) {
            log.error("Error parsing exam JSON: {}", e.getMessage());
        }

        // 로그로 test.exam 정보 출력 (디버그용)
        log.info("test exam: {}", test.getExam());
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("member", member);
        model.addAttribute("members", members);
        model.addAttribute("saveCheck", true);
        model.addAttribute("test", test);
        model.addAttribute("questions", questions); // 문제 리스트 추가 -> 정답률 추가

        // 'testDetail' 페이지로 이동
        return "testDetailForTeacher";
    }

    @GetMapping("/result/Detail")
    public String detail(@RequestParam("mid") Long mid,
                         @RequestParam("sid") Long sid,
                         Model model,
                         @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Test test = testService.findById(sid);

        boolean saveCheck = false;
        TestResult result=null;
        List<Integer> selects = new ArrayList<>();

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        Optional<MemberEntity> tester = memberService.findById(mid);
        List<TestResult> testResult = testResultService.findByTestId(sid);
        List<MemberEntity>members = new ArrayList<>();
        if (!testResult.isEmpty()) {
            for(TestResult t : testResult) {
                members.add(memberService.findById(t.getMemberId()).get());
                if(t.getMemberId()==mid){
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
        model.addAttribute("members", members);
        model.addAttribute("mid",mid);
        model.addAttribute("sid",sid);
        model.addAttribute("result", result);
        model.addAttribute("saveCheck", saveCheck);
        model.addAttribute("subjectId", sid);
        model.addAttribute("member", member);
        model.addAttribute("tester", tester.get());
        model.addAttribute("test", test);
        model.addAttribute("author", Objects.equals(test.getMemberId(), member.getId()));
        model.addAttribute("questions", questions); // 문제 리스트 추가

        // 'testDetail' 페이지로 이동
        return "studentTestDetail";
    }
}
