package com.lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GradeSubmissionRequest {

    @NotNull(message = "Marks obtained is required")
    @Min(value = 0, message = "Marks cannot be negative")
    private Integer marksObtained;

    @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
    private String feedback;

    public GradeSubmissionRequest() {}

    public GradeSubmissionRequest(Integer marksObtained, String feedback) {
        this.marksObtained = marksObtained;
        this.feedback = feedback;
    }

    public Integer getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
