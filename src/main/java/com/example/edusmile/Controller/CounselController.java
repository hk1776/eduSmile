package com.example.edusmile.Controller;


import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Member;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher")
public class CounselController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @GetMapping("/student_recode")
    public String student_recode(@AuthenticationPrincipal UserDetails user, Model model) {
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));

        List<MemberEntity> students = memberRepository.findByRoleAndTeacherCode("student",member.getTeacherCode());  //자기반학생찾기
        model.addAttribute("students", students);
        if(!member.getRole().equals("teacher")) {
            return "main";
        }else{
            return "student_recode";
        }
    }

    @GetMapping("/student_recode/{id}")
    public String student_recode_detail(@PathVariable Long id, Model model) {

        MemberEntity member = memberRepository.findById(id).orElse(null);
        model.addAttribute("member", member);
        return "student_recode_detail";
    }

}
