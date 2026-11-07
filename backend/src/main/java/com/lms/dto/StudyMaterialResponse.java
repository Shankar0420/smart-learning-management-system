package com.lms.dto;

import java.time.LocalDateTime;

public class StudyMaterialResponse {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseTitle;
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String fileType; // PDF, PPT, PPTX
    private String uploaderName;
    private LocalDateTime createdAt;

    public StudyMaterialResponse() {}

    public StudyMaterialResponse(Long id, String title, String description, Long courseId, String courseTitle,
                                 String fileName, String originalFileName, String contentType, Long fileSize,
                                 String fileType, String uploaderName, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.uploaderName = uploaderName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

