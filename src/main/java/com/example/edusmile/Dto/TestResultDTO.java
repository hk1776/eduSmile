package com.example.edusmile.Dto;

import lombok.Data;

import java.util.List;

@Data
public class TestResultDTO {
    private long testId;
    private String classId;
    private long memberId;
    private List<AnswerData> answers;
    private int score;

    @Data
    public static class AnswerData {
        private String question;
        private String selectedAnswer;
        private String correctAnswer;
        private int score;
    }
}
