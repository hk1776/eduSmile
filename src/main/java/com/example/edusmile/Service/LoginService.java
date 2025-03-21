package com.example.edusmile.Service;


import com.example.edusmile.Dto.MemberDto;
import com.example.edusmile.Dto.ResetPWDto;
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

    private final PasswordEncoder encoder;

    public List<MemberEntity> FindByIdNamePhone(String loginid,String name ,String phone) {
        return memberRepository.findByIDAndNameAndPhoneNumber(loginid,name, phone);
    }

    public List<MemberEntity> FindByNamePhone(String name ,String phone) {
      return memberRepository.findByNameAndPhoneNumber(name, phone);
    }
    public boolean saveMember(MemberDto memberDto) {


        if (memberDto.getRole().equals("teacher")) {
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789"; // 대문자만 포함
            StringBuilder result = new StringBuilder();
            SecureRandom secureRandom = new SecureRandom();

            for (int i = 0; i < 5; i++) {
                int index = secureRandom.nextInt(characters.length());
                result.append(characters.charAt(index));
            }

            memberDto.setTeacherCode(result.toString());
            MemberEntity tm = memberDto.toEntity();
            System.out.println(tm.getPassword());
            tm.setPassword(encoder.encode(memberDto.getPassword())); //비밀번호 암호화
            tm.setImg_path("blank_profile.svg");
            memberRepository.save(tm);
            return true;

        }
        else if(memberDto.getRole().equals("student")) {
                List<MemberEntity> members = memberRepository.findByTeacherCodeTeacher(memberDto.getTeacherCode() , "teacher");

                if(members.isEmpty())
                {
                    return false;
                }
                else
                {
                    MemberEntity teacher = members.get(0);

                    memberDto.setSchoolClass(teacher.getSchoolClass());
                    memberDto.setSchool(teacher.getSchool());
                    memberDto.setSchoolgrade(teacher.getSchoolgrade());
                    MemberEntity tm = memberDto.toEntity();
                    tm.setPassword(encoder.encode(memberDto.getPassword())); //비밀번호 암호화
                    tm.setImg_path("blank_profile.svg");
                    memberRepository.save(tm);
                    return true;
                }

        }
        return false;

    }
    public boolean findmember(MemberDto memberDto) {
        Optional<MemberEntity> _siteUser = this.memberRepository.findByloginId(memberDto.getLoginId());

        if (_siteUser.isPresent()) {
            return true;
        }
        return false;
    }

    public void reset_pw(ResetPWDto resetPWDto)
    {
        if(resetPWDto.getLoginId() == null || resetPWDto.getPassword()==null)
            return ;
        Optional<MemberEntity> member_op = memberRepository.findByloginId(resetPWDto.getLoginId());
        if(member_op.isPresent()) {
            MemberEntity member = member_op.get();
            member.setPassword(encoder.encode(resetPWDto.getPassword()));
            memberRepository.save(member);
        }


    }
}
