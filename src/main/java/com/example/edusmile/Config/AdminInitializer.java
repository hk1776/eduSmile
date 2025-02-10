package com.example.edusmile.Config;

import com.example.edusmile.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final MemberService memberService;


    @Override
    public void run(String... args) throws Exception {
        String adminUsername = "admin";
        String adminPassword = "admin123"; // 기본 비밀번호
        String adminRole = "ADMIN"; // 관리자 권한

//        // 관리자 계정이 없으면 생성
//        if (!memberService.existsByUsername(adminUsername)) {
//            memberService.(adminUsername, adminPassword, adminRole);
//            System.out.println("✅ 관리자 계정이 생성되었습니다. (ID: admin, PW: admin123)");
//        } else {
//            System.out.println("🔹 관리자 계정이 이미 존재합니다.");
//        }
    }
}

