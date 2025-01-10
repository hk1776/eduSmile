package com.example.edusmile.Dto;

import com.example.edusmile.Entity.MemberEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.security.Timestamp;

public class MemberDto {


    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String phoneNumber;
    private String school;
    private String schoolClass;
    private String role;
    private String teacherCode;
    private Timestamp createDate;
    private Timestamp updateDate;

    public MemberEntity toEntity() {
        return new MemberEntity(null,loginId,password,name,phoneNumber,school,schoolClass,
                role,teacherCode,null,null);
    }
}
