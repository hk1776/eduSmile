package com.example.edusmile.Controller;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Entity.Subject;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.AttendService;
import com.example.edusmile.Service.MemberService;
import com.example.edusmile.Service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    private final AttendService attendService;
    private final SubjectService subjectService;

    @GetMapping("/member/mypage")
    public String memberPage(@AuthenticationPrincipal UserDetails user, Model model) {
        log.info("user = {}",user.getUsername());



        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));
        model.addAttribute("st", member.getRole().equals("student"));

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        log.info("member: {}", member.getName());
        System.out.println("member: " + member.getId());
        return "mypage";
    }

    @PostMapping("/member/mypage/classAdd")
    public String classAdd(@AuthenticationPrincipal UserDetails user, @RequestParam("subject") String subject, Model model) {

       MemberEntity member = memberService.memberInfo(user.getUsername());


        Optional<Subject> sub = subjectService.findById(subject);

        Subject subject1 = sub.get();

        attendService.save(member,subject1);

        return"redirect:/member/mypage";

    }

    @GetMapping("/mypage/deleteaccount")
    public String deleteAccount(@AuthenticationPrincipal UserDetails user, Model model) {

        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));
        model.addAttribute("st", member.getRole().equals("student"));

        //헤더 있는페이지는 이거 필수
        MemberEntity my= memberService.memberInfo(user.getUsername());
        model.addAttribute("my", my);
        //여기 까지

        return "deleteaccount";
    }



}