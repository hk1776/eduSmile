package com.example.edusmile.Controller;

import com.example.edusmile.Dto.MemberDto;
import com.example.edusmile.Service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/user/login")
    public String login() {

        return "login";
    }

    @GetMapping("/mainsite")
    public String mainsite() {

        return "main";
    }

    @GetMapping("/user/signup")
    public String signup() {

        return "signup";
    }

    @Transactional
    @PostMapping("/user/signupsave")
    public String signupsave(@ModelAttribute MemberDto memberDto) {

        System.out.print("!");


        loginService.saveMember(memberDto);

        return "";
    }



}
