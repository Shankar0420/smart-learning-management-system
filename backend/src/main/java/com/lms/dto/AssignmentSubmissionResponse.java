package com.lms.dto;

import java.time.LocalDateTime;

public class AssignmentSubmissionResponse {

    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private String answerText;
    private String submissionOriginalName;
    private String submissionFileType;
    private Long submissionFileSize;
    private boolean hasSubmissionFile;
    private Integer marksObtained;
    private Integer maxMarks;
    private String feedback;
    private boolean graded;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;

    public AssignmentSubmissionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getSubmissionOriginalName() { return submissionOriginalName; }
    public void setSubmissionOriginalName(String submissionOriginalName) { this.submissionOriginalName = submissionOriginalName; }

    public String getSubmissionFileType() { return submissionFileType; }
    public void setSubmissionFileType(String submissionFileType) { this.submissionFileType = submissionFileType; }

    public Long getSubmissionFileSize() { return submissionFileSize; }
    public void setSubmissionFileSize(Long submissionFileSize) { this.submissionFileSize = submissionFileSize; }

    public boolean isHasSubmissionFile() { return hasSubmissionFile; }
    public void setHasSubmissionFile(boolean hasSubmissionFile) { this.hasSubmissionFile = hasSubmissionFile; }

    public Integer getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public boolean isGraded() { return graded; }
    public void setGraded(boolean graded) { this.graded = graded; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
