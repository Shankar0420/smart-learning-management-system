package com.lms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class QuizSubmitRequest {

    public static class SelectedAnswer {
        @NotNull(message = "Question ID is required")
        private Long questionId;

        @NotNull(message = "Selected option ID is required")
        private Long selectedOptionId;

        public SelectedAnswer() {}

        public SelectedAnswer(Long questionId, Long selectedOptionId) {
            this.questionId = questionId;
            this.selectedOptionId = selectedOptionId;
        }

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public Long getSelectedOptionId() { return selectedOptionId; }
        public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    }

    @NotEmpty(message = "Answers cannot be empty")
    private List<SelectedAnswer> answers;

    public QuizSubmitRequest() {}

    public QuizSubmitRequest(List<SelectedAnswer> answers) {
        this.answers = answers;
    }

    public List<SelectedAnswer> getAnswers() { return answers; }
    public void setAnswers(List<SelectedAnswer> answers) { this.answers = answers; }
}
