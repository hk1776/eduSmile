package com.example.edusmile.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {


    @GetMapping("/user/login")
    public String login() {

        return "login";
    }

    @GetMapping("/mainsite")
    public String mainsite() {

        return "main";
    }

    @GetMapping("user/signup")
    public String signup() {

        return "signup";
    }

    @PostMapping("user/signupsave")
    public String signupsave() {

        return "signup";
    }
}
