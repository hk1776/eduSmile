package com.example.edusmile.Dto;

import com.example.edusmile.Entity.MemberEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;



@Getter
@Setter
@NoArgsConstructor
public class MemberDto {


    private String loginId;
    private String password;
    private String name;
    private Long phoneNumber;
    private String school;
    private int schoolClass;
    private String role;
    private String teacherCode;
    private String subject;
    private int schoolgrade;
    public MemberEntity toEntity() {
        return new MemberEntity(null,loginId,password,name,phoneNumber,school,schoolClass,schoolgrade,
                role,teacherCode,null,null,null,null);
    }
}
