package com.example.edusmile.Entity;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    private String subject;
}
