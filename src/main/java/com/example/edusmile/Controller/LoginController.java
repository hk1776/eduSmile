package com.example.edusmile.Controller;

import com.example.edusmile.Dto.FindIdDto;
import com.example.edusmile.Dto.MemberDto;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import com.example.edusmile.Service.LoginService;
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
    private final MemberRepository memberRepository;

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


    @GetMapping("/findLoginId")
    public String findLoginId() {

        return "findLoginId";
    }

    @PostMapping("/findLoginId/find")
    public String findLoginId(@ModelAttribute FindIdDto findIdDto, RedirectAttributes rttr) {

        List<MemberEntity> members = memberRepository.findByNameAndPhoneNumber(findIdDto.getName(), findIdDto.getPhoneNumber());
        rttr.addFlashAttribute("members", members);

        if(members.isEmpty())
        {
            rttr.addFlashAttribute("NotExist", true);
            return "redirect:/findLoginId";
        }

        return "redirect:/findLoginId/show";
    }

    @GetMapping("/findLoginId/show")
    public String findLoginIdShow() {


        return "findLoginIdShow";
    }





}
