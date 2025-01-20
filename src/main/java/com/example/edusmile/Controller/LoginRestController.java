package com.example.edusmile.Controller;


import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;                  //비동기 처리 위해 js 사용

@RestController
@RequiredArgsConstructor
public class LoginRestController {

    private final MemberRepository memberRepository;

    @PostMapping("/user/tcode")
    public String receiveString(@RequestBody String text) {          //학생 회원가입시 교사코드 인증하기

        System.out.println(text+"1234");
        List<MemberEntity> members = memberRepository.findByTeacherCodeTeacher(text , "teacher");

        if(members.isEmpty()) {

            return "등록 실패";
        }
        else {

            return "등록 성공";
        }

    }



}
