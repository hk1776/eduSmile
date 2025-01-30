package com.example.edusmile.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class FreeBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private int views;
    @Column(length = 10000)
    private String content;
    private String created;
    private String updated;
    private String file;
    private String classId;
}
