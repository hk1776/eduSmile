package com.example.edusmile.Dto;

import lombok.Data;

import java.util.List;


public class Classification {
    @Data
    public static class STT {
        private String text;
    }

//    @Data
//    public static class AnalyzeDTO {
//        private String notice;
//        private String class_summary;
//        private QuizDTO quiz;
//
//        @Data
//        // 내부 클래스: QuizDTO
//        public static class QuizDTO {
//            private List<QuestionDTO> questions;
//            @Data
//            public static class QuestionDTO {
//                private String question;
//                private List<String> choices;
//                private int correct_answer;
//                private String explanation;
//            }
//        }
//
//    }

    @Data
    public static class AnalyzeDTO {
        private Notice notice;
        private ClassSummary class_summary;
        private Quiz quiz;

        @Data
        public static class Notice {
            private String text;
        }

        @Data
        public static class ClassSummary {
            private String text;
        }

        @Data
        public static class Quiz {
            private Text text;

            @Data
            public static class Text {
                private List<Question> questions;

                @Data
                public static class Question {
                    private String question;
                    private List<String> choices;
                    private int correct_answer;
                    private String explanation;
                    
                }
            }
        }

        @Data
        public static class Question {
            private String question;
            private List<String> choices;
            private int answer;
            private String explanation;

            public Question(String question, List<String> choices, int answer, String explanation) {
                this.question = question;
                this.choices = choices;
                this.answer = answer;
                this.explanation = explanation;
            }
        }
    }
}
