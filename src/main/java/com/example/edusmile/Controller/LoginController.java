package com.example.edusmile.Controller;

import com.example.edusmile.Dto.FindIdDto;
import com.example.edusmile.Dto.FindPWDto;
import com.example.edusmile.Dto.MemberDto;
import com.example.edusmile.Dto.ResetPWDto;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.LoginService;
import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

            if(!loginService.saveMember(memberDto))
            {
                rttr.addFlashAttribute("not_exist_teacherCode", true);
                return "redirect:/user/ssignup";
            }
        }
        return "redirect:/user/login";
    }


    @GetMapping("/user/findLoginId")
    public String findLoginId() {

        return "findLoginId";
    }

    @PostMapping("/user/findLoginId/find")
    public String findLoginId(@ModelAttribute FindIdDto findIdDto, RedirectAttributes rttr) {

        List<MemberEntity> members = loginService.FindByNamePhone(findIdDto.getName(), findIdDto.getPhoneNumber());
        rttr.addFlashAttribute("members", members);

        if(members.isEmpty())
        {
            rttr.addFlashAttribute("NotExist", true);
            return "redirect:/user/findLoginId";
        }

        return "redirect:/user/findLoginId/show";
    }

    @GetMapping("/user/findLoginId/show")
    public String findLoginIdShow() {


        return "findLoginIdShow";
    }

    @GetMapping("/user/reset_password")
    public String reset_password() {


        return "reset_password";
    }


    @PostMapping("/user/reset_password/find")
    public String reset_password(@ModelAttribute FindPWDto findPWDto, RedirectAttributes rttr) {

        List<MemberEntity> members = loginService.FindByIdNamePhone(findPWDto.getLoginId(), findPWDto.getName(), findPWDto.getPhoneNumber());
        rttr.addFlashAttribute("members", members);

        if(members.isEmpty())
        {
            rttr.addFlashAttribute("NotExist", true);
            return "redirect:/user/reset_password";
        }

        return "redirect:/user/reset_password/reset";

    }


    @GetMapping("/user/reset_password/reset")
    public String reset_password_reset() {


        return "reset_password_reset";

    }

    @PostMapping("/user/reset_password/reset/post")
    public String reset_password_post(@ModelAttribute ResetPWDto resetPWDto,RedirectAttributes rttr) {
        System.out.println(resetPWDto.getLoginId());
        loginService.reset_pw(resetPWDto);

        rttr.addFlashAttribute("pw_changed", true);
        return "redirect:/user/login";

    }








}
