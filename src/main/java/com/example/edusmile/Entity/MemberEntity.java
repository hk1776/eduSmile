package com.example.edusmile.Entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.security.Timestamp;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(name= "login_id" , updatable = false , nullable = false , unique = true )
    private String loginId;

    @Column(name = "password" , nullable = false )
    private String password;

    @Column(name = "name" , nullable = false)
    private String name;

    @Column(name = "phone_number" , nullable = false)
    private String phoneNumber;

    @Column(name = "school" , nullable = false)
    private String school;

    @Column(name = "school_class" , nullable = false)
    private String schoolClass;

    @Column(name = "role" , nullable = false)
    private String role;

    @Column(name = "teacher_code" , nullable = false)
    private String teacherCode;

    @CreationTimestamp
    private Timestamp createDate;

    @UpdateTimestamp
    private Timestamp updateDate;




}
