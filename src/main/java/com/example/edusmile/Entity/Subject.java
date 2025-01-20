package com.example.edusmile.Entity;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
public class Subject {
    @Id
    private String id;
    private Long memberId;
    private String subject;
    private int grade;
    @Column(name = "CLASS")
    private String divClass;
}
