package com.example.edusmile.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    long testId;
    String classId;
    long memberId;
    @Column(length = 10000)
    String result;
    int score;

}
