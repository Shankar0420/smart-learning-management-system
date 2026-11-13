package com.lms.dto;

import jakarta.validation.constraints.NotBlank;

public class QuizOptionRequest {

    @NotBlank(message = "Option text is required")
    private String optionText;

    private boolean correct = false;

    public QuizOptionRequest() {}

    public QuizOptionRequest(String optionText, boolean correct) {
        this.optionText = optionText;
        this.correct = correct;
    }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
