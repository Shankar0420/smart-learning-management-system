package com.lms.dto;

public class CourseProgressResponse {

    private Long courseId;
    private String courseTitle;
    private long totalLessons;
    private long completedLessons;
    private double progressPercentage;

    public CourseProgressResponse() {}

    public CourseProgressResponse(Long courseId, String courseTitle, long totalLessons,
                                  long completedLessons, double progressPercentage) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.totalLessons = totalLessons;
        this.completedLessons = completedLessons;
        this.progressPercentage = progressPercentage;
    }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public long getTotalLessons() { return totalLessons; }
    public void setTotalLessons(long totalLessons) { this.totalLessons = totalLessons; }

    public long getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(long completedLessons) { this.completedLessons = completedLessons; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
}
