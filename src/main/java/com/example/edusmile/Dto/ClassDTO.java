package com.example.edusmile.Dto;

import com.example.edusmile.Entity.Subject;
import lombok.Data;

@Data
public class ClassDTO {
    private String id;
    private String divClass;
    private String subject;
    private int grade;
    private Long teacherId;
    private String teacherName;
    public ClassDTO(Subject subject) {
        this.id = subject.getId();
        this.divClass = subject.getDivClass();
        this.subject = subject.getSubject();
        this.grade = subject.getGrade();
        this.teacherId = subject.getTeacherId();
    }
}
