package com.lms.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignmentSubmissionRequest {

    @NotBlank(message = "Answer text cannot be blank")
    private String answerText;

    public AssignmentSubmissionRequest() {}

    public AssignmentSubmissionRequest(String answerText) {
        this.answerText = answerText;
    }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
}
