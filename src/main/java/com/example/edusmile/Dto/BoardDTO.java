package com.example.edusmile.Dto;

import com.example.edusmile.Entity.MemberEntity;
import lombok.Data;

public class BoardDTO {
    @Data
    public static class Free {
        private String title;
        private String category;
        private String createdAt;
        private String author;
        private String content;
        private String classId;
    }

    @Data
    public static class Update {
        private String title;
        private String category;
        private String createdAt;
        private String author;
        private String content;
        private Long id;
        private String classId;
        private String fileStatus;
    }

    @Data
    public static class Summary {
        private Long id;
        private String title;
        private String content;
    }

    @Data
    public static class subjectTeacherName {
        private String id;
        private String divClass;
        private int grade;
        private String subject;
        private MemberEntity teacher;
    }
}
