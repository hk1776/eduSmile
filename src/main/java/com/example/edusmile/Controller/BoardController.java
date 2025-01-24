package com.example.edusmile.Controller;

import com.example.edusmile.Dto.BoardNextDTO;
import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


        return "index";
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
        testService.save(subjectId,questions.toString(),member.getId());
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


}
