package com.example.edusmile.Controller;

import com.example.edusmile.Dto.Classification;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Notice;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.NoticeService;
import com.example.edusmile.Service.SummaryService;
import com.example.edusmile.Service.TestService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final NoticeService noticeService;
    private final SummaryService summaryService;
    private final TestService testService;
    private final MemberService memberService;

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
        Notice save = noticeService.save(subjectId, content, member.getId(), fileCheck);
        String projectDir = Paths.get(System.getProperty("user.dir"), "file", "board").toString();
        if(fileCheck) {
            files.forEach(file -> {
                try {
                    String newFilename = save.getId().toString();
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String savePath = Paths.get(projectDir, newFilename+extension).toString();
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

//    @PostMapping("/test")
//    public ResponseEntity<?> submitTest( @AuthenticationPrincipal UserDetails user,
//                                         @RequestPart(value = "files", required = false) List<MultipartFile> files,
//                                         @RequestParam("content") String questions,
//                                         @RequestParam("subjectId") String subjectId) {
//        MemberEntity member  = memberService.memberInfo(user.getUsername());
//        summaryService.save(subjectId,questions.toString(),member.getId());
//        log.info(questions.toString());
//        log.info(subjectId);
//        return ResponseEntity.ok("시험 등록 성공");
//    }

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
}
