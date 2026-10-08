package com.lms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class QuizQuestionRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @Min(value = 1, message = "Marks must be at least 1")
    private int marks = 1;

    @NotEmpty(message = "At least two options are required")
    @Valid
    private List<QuizOptionRequest> options;

    public QuizQuestionRequest() {}

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public List<QuizOptionRequest> getOptions() { return options; }
    public void setOptions(List<QuizOptionRequest> options) { this.options = options; }
}
