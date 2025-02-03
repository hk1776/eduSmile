package com.example.edusmile.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


public class Classification {
    @Data
    public static class STT {
        private String text;
    }

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

    @Data
    public static class TestData {
        private String subjectId;
        private String quizData;
    }

    @Data
    public static class Counsel {
        private Summary summary;

        @Getter
        @Setter
        public static class Summary {
            private List<String> text;
        }
    }
}
