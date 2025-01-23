package com.example.edusmile.Controller;

import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final MemberService memberService;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails user, Model model) {
        log.info("user = {}",user.getUsername());
        MemberEntity member  = memberService.memberInfo(user.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("teacher",member.getRole().equals("teacher"));
        log.info("member: {}", member.getName());
        return "main";
    }
}
