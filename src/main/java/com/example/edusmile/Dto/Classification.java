package com.example.edusmile.Dto;

import lombok.Data;

import java.util.List;


public class Classification {
    @Data
    public static class STT {
        private String text;
    }

    @Data
    public static class AnalyzeDTO {
        private String notice;
        private String class_summary;
        private QuizDTO quiz;

        @Data
        // 내부 클래스: QuizDTO
        public static class QuizDTO {
            private List<QuestionDTO> questions;
            @Data
            public static class QuestionDTO {
                private String question;
                private List<String> choices;
                private int correct_answer;
                private String explanation;
            }
        }

    }
}
