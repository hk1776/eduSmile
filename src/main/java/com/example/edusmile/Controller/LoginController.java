package com.example.edusmile.Controller;

import com.example.edusmile.Dto.MemberDto;
import com.example.edusmile.Service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/user/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", true); // 에러 플래그 전달
        }
        return "login";
    }

    @GetMapping("/mainsite")
    public String mainsite() {

        return "main";
    }

    @GetMapping("/user/tsignup")
    public String signup_teacher() {

        return "signup_teacher";
    }

    @GetMapping("/user/ssignup")
    public String signup_student() {

        return "signup_student";
    }

    @Transactional
    @PostMapping("/user/signupsave")
    public String signupsave(@ModelAttribute MemberDto memberDto, RedirectAttributes rttr) {

        if(loginService.findmember(memberDto))
        {
            System.out.println("!");
            rttr.addFlashAttribute("duplication", true);
            if(memberDto.getRole().equals("student"))
            {
                return "redirect:/user/ssignup";
            }
            else
            {
                return "redirect:/user/tsignup";
            }
        }
        else {
            loginService.saveMember(memberDto);
        }
        return "/user/login";
    }





}
