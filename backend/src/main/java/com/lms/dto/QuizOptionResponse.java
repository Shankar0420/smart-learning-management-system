package com.lms.dto;

public class QuizOptionResponse {

    private Long id;
    private String optionText;
    private Boolean correct; // null when hidden from students taking quiz

    public QuizOptionResponse() {}

    public QuizOptionResponse(Long id, String optionText, Boolean correct) {
        this.id = id;
        this.optionText = optionText;
        this.correct = correct;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }
}
