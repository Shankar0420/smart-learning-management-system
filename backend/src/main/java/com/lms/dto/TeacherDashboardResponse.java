package com.lms.dto;

public class TeacherDashboardResponse {

    private long totalCourses;
    private long totalStudents;
    private long pendingSubmissions;

    public TeacherDashboardResponse() {}

    public TeacherDashboardResponse(long totalCourses, long totalStudents, long pendingSubmissions) {
        this.totalCourses = totalCourses;
        this.totalStudents = totalStudents;
        this.pendingSubmissions = pendingSubmissions;
    }

    public long getTotalCourses() { return totalCourses; }
    public void setTotalCourses(long totalCourses) { this.totalCourses = totalCourses; }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getPendingSubmissions() { return pendingSubmissions; }
    public void setPendingSubmissions(long pendingSubmissions) { this.pendingSubmissions = pendingSubmissions; }
}
