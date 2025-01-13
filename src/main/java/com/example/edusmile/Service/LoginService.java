package com.example.edusmile.Service;


import com.example.edusmile.Dto.MemberDto;
import com.example.edusmile.Entity.MemberEntity;
import com.example.edusmile.Repository.MemberRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void saveMember(MemberDto memberDto) {
        if (memberDto.getRole().equals("teacher")) {
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789"; // 대문자만 포함
            StringBuilder result = new StringBuilder();
            SecureRandom secureRandom = new SecureRandom();

            for (int i = 0; i < 5; i++) {
                int index = secureRandom.nextInt(characters.length());
                result.append(characters.charAt(index));
            }

            memberDto.setTeacherCode(result.toString());

            memberRepository.save(memberDto.toEntity());

        }

    }
    public boolean findmember(MemberDto memberDto) {
        Optional<MemberEntity> _siteUser = this.memberRepository.findByloginId(memberDto.getLoginId());

        if (_siteUser.isPresent()) {
            return true;
        }
        return false;
    }
}
