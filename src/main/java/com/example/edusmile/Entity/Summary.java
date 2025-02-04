package com.example.edusmile.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Summary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private Long memberId;
    private int views;
    @Column(length = 10000)
    private String summary;
    private String created;
    private String updated;
    private String classId;
}
