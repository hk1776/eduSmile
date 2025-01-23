package com.example.edusmile.Controller;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Member;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails user, Model model) {

        log.info("user = {}",user.getUsername());
        MemberEntity member  = memberService.memberInfo(user.getUsername());

        //헤더 있는페이지는 이거 필수
        List<MemberEntity> listteacher = memberRepository.findByTeacherCodeTeacher(member.getTeacherCode(),"teacher");
        MemberEntity teacher = listteacher.get(0);
        model.addAttribute("class-teacher", teacher);
        //여기 까지

        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));
        log.info("member: {}", member.getName());
        return "main";
    }
}
