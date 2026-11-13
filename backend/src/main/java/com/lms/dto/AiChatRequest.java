package com.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiChatRequest {

    @NotBlank(message = "Prompt cannot be empty")
    @Size(max = 2000, message = "Prompt exceeds maximum 2000 characters")
    private String prompt;

    private String context; // E.g., 'STUDENT_ASSISTANT', 'TEACHER_QUIZ_GEN', 'EXPLAIN_CONCEPT'

    private Long courseId;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}

