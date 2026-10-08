package com.lms.dto;

import java.time.LocalDateTime;

public class QuizAttemptResponse {

    private Long id;
    private Long quizId;
    private String quizTitle;
    private Long studentId;
    private String studentName;
    private int score;
    private int totalMarks;
    private double percentage;
    private LocalDateTime attemptedAt;

    public QuizAttemptResponse() {}

    public QuizAttemptResponse(Long id, Long quizId, String quizTitle, Long studentId, String studentName,
                               int score, int totalMarks, double percentage, LocalDateTime attemptedAt) {
        this.id = id;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.score = score;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.attemptedAt = attemptedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
}
