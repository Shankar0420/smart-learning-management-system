package com.lms.dto;

import java.time.LocalDateTime;

public class DiscussionReplyResponse {

    private Long id;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String content;
    private boolean acceptedSolution;
    private LocalDateTime createdAt;

    public DiscussionReplyResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isAcceptedSolution() { return acceptedSolution; }
    public void setAcceptedSolution(boolean acceptedSolution) { this.acceptedSolution = acceptedSolution; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

