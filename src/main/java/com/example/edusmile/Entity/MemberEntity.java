package com.example.edusmile.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id",unique = true, nullable = false)
    private Long id;

    @Column(name= "login_id" , updatable = false , nullable = false , unique = true )
    private String loginId;

    @Column(name = "password" , nullable = false )
    private String password;

    @Column(name = "name" , nullable = false)
    private String name;

    @Column(name = "phone_number" , nullable = false)
    private Long phoneNumber;

    @Column(name = "school" , nullable = false)
    private String school;

    @Column(name = "school_class" , nullable = false)
    private int schoolClass;

    @Column(name = "role" , nullable = false)
    private String role;

    @Column(name = "teacher_code" )
    private String teacherCode;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private Set<Attend> attends = new HashSet<>();

    private String img_path;


}
