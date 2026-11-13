package com.lms.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuizResponse {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private int totalMarks;
    private boolean active;
    private List<QuizQuestionResponse> questions;
    private LocalDateTime createdAt;

    public QuizResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<QuizQuestionResponse> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestionResponse> questions) { this.questions = questions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
