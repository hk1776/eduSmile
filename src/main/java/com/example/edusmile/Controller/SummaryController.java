package com.example.edusmile.Controller;

import com.example.edusmile.Dto.BoardDTO;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Summary;
import com.example.edusmile.Service.*;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board") // 공지사항 관련 경로
public class SummaryController {
    private final SummaryService summaryService;
    private final MemberService memberService;


    @GetMapping("/summaryList")
    public String summaryList(@RequestParam("id") String subjectId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              Model model,
                              @AuthenticationPrincipal UserDetails user) {
        MemberEntity member = memberService.memberInfo(user.getUsername());
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc("id")));

        Page<Summary> summaryPage = summaryService.findByClassId(subjectId, pageable);


        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        model.addAttribute("teacher",member.getRole().equals("teacher"));
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

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지



        model.addAttribute("teacher",member.getRole().equals("teacher"));

        Summary summary = summaryService.findById(id);
        log.info("summary= {}",summary.getSummary());
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("member", member);
        model.addAttribute("summary", summary);
        model.addAttribute("author", Objects.equals(summary.getMemberId(), member.getId()));
        return "summaryDetail";
    }

    @GetMapping("/summary/update")
    public String summaryUpdateForm(@RequestParam("id") String subjectId,
                                    @RequestParam("num") Long id,
                                    Model model,
                                    @AuthenticationPrincipal UserDetails user) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        Summary summary = summaryService.findById(id);

        model.addAttribute("summary", summary);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("member", member);

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        model.addAttribute("teacher",member.getRole().equals("teacher"));
        return "summaryUpdate";
    }

    @PostMapping("/summary/update")
    public String summaryUpdate(Model model,
                                @AuthenticationPrincipal UserDetails user,
                                @ModelAttribute BoardDTO.Summary summary) {
        Summary update = summaryService.update(summary.getId(), summary.getTitle(), summary.getContent());

        return "redirect:/board/summary?id=" + update.getClassId() + "&num=" + update.getId();
    }

    @DeleteMapping("/summary/delete")
    public ResponseEntity<?> deleteSummary(@RequestParam("num") Long id) {

        summaryService.delete(id);
        return ResponseEntity.ok().build();
    }

}
