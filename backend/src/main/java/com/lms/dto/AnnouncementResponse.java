package com.lms.dto;

import java.time.LocalDateTime;

public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private Long courseId;
    private String courseTitle;
    private String priority;
    private Long authorId;
    private String authorName;
    private String targetRole;
    private LocalDateTime createdAt;

    public AnnouncementResponse() {
    }

    public AnnouncementResponse(Long id, String title, String content, Long courseId, String courseTitle,
                                String priority, Long authorId, String authorName, String targetRole, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.priority = priority;
        this.authorId = authorId;
        this.authorName = authorName;
        this.targetRole = targetRole;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

