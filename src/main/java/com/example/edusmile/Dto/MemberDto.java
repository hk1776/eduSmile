package com.example.edusmile.Dto;

import com.example.edusmile.Entity.MemberEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.Timestamp;


@Getter
@Setter
@NoArgsConstructor
public class MemberDto {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private String loginId;
    private String password;
    private String name;
    private String phoneNumber;
    private String school;
    private int schoolClass;
    private String role;
    private String teacherCode;

    public MemberEntity toEntity() {
        return new MemberEntity(null,loginId,encoder.encode(password),name,phoneNumber,school,schoolClass,
                role,teacherCode,null,null);
    }
}
