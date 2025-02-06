package com.example.edusmile.Dto;

import com.example.edusmile.Entity.Subject;
import lombok.Data;
@Data
public class SubjectDTO {
    private String id;
    private Long teacherId;
    private String subject;
    private int grade;
    private String divClass;

    public SubjectDTO(Subject subject) {
        this.id = subject.getId();
        this.divClass = subject.getDivClass();
        this.subject = subject.getSubject();
        this.grade = subject.getGrade();
        this.teacherId = subject.getTeacherId();
    }

}
