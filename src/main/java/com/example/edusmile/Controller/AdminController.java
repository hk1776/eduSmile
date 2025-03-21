package com.example.edusmile.Controller;

import com.example.edusmile.Dto.RoleUpdateDTO;
import com.example.edusmile.Entity.ClassLog;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Service.ClassLogService;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

private final VisitorService visitorService;
private final MemberService memberService;
private final ClassLogService classLogService;

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        if(!member.getRole().equals("ADMIN"))
            return "redirect:/home";
        Map<LocalDate, Integer> visitCnt = visitorService.weeklyVisitor();
        List<LocalDate> localDates = new ArrayList<>(visitCnt.keySet());
        Collections.reverse(localDates);

        List<Integer> visitList = new ArrayList<>();
        for (LocalDate localDate : localDates) {
            visitList.add(visitCnt.get(localDate));
        }

        model.addAttribute("memberCnt",memberService.memberCnt());
        model.addAttribute("visitToday",visitorService.getTodayVisitors());
        model.addAttribute("schoolCnt", memberService.schoolCnt());
        model.addAttribute("teacherCnt", memberService.teacherCnt());
        model.addAttribute("studentCnt", memberService.studentCnt());
        model.addAttribute("adminCnt", memberService.adminCnt());
        model.addAttribute("visitKey",localDates);
        model.addAttribute("visitList",visitList);
        model.addAttribute("classLog",classLogService.findAll());
        return "adminIndex";
    }

    @GetMapping("/memberManage")
    public String memberManage(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        if(!member.getRole().equals("ADMIN"))
            return "redirect:/home";

        model.addAttribute("member",memberService.findAll());
        return "adminMember";
    }

    @PostMapping("/update-role")
    public ResponseEntity<String> updateRole(@RequestBody RoleUpdateDTO request) {
        memberService.updateUserRole(request.getId(), request.getRole());
        return ResponseEntity.ok("역할 변경 완료");
    }

}
