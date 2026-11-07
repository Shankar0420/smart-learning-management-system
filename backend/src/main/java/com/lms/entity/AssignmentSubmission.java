package com.lms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AssignmentSubmission entity for student assignment answers.
 */
@Entity
@Table(name = "assignment_submissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "assignment_id"}))
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(columnDefinition = "TEXT")
    private String answerText;

    private String submissionFileName;
    private String submissionOriginalName;
    private String submissionFileType;
    private Long submissionFileSize;
    private String submissionFilePath;

    private Integer marksObtained;

    @Column(length = 1000)
    private String feedback;

    @Column(nullable = false)
    private boolean graded = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime gradedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }

    public AssignmentSubmission() {}

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getSubmissionFileName() { return submissionFileName; }
    public void setSubmissionFileName(String submissionFileName) { this.submissionFileName = submissionFileName; }

    public String getSubmissionOriginalName() { return submissionOriginalName; }
    public void setSubmissionOriginalName(String submissionOriginalName) { this.submissionOriginalName = submissionOriginalName; }

    public String getSubmissionFileType() { return submissionFileType; }
    public void setSubmissionFileType(String submissionFileType) { this.submissionFileType = submissionFileType; }

    public Long getSubmissionFileSize() { return submissionFileSize; }
    public void setSubmissionFileSize(Long submissionFileSize) { this.submissionFileSize = submissionFileSize; }

    public String getSubmissionFilePath() { return submissionFilePath; }
    public void setSubmissionFilePath(String submissionFilePath) { this.submissionFilePath = submissionFilePath; }

    public Integer getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public boolean isGraded() { return graded; }
    public void setGraded(boolean graded) { this.graded = graded; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
