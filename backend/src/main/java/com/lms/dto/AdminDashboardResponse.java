package com.lms.dto;

public class AdminDashboardResponse {

    private long totalStudents;
    private long totalTeachers;
    private long totalCourses;
    private long totalEnrollments;

    public AdminDashboardResponse() {}

    public AdminDashboardResponse(long totalStudents, long totalTeachers, long totalCourses, long totalEnrollments) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalCourses = totalCourses;
        this.totalEnrollments = totalEnrollments;
    }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }

    public long getTotalCourses() { return totalCourses; }
    public void setTotalCourses(long totalCourses) { this.totalCourses = totalCourses; }

    public long getTotalEnrollments() { return totalEnrollments; }
    public void setTotalEnrollments(long totalEnrollments) { this.totalEnrollments = totalEnrollments; }
}
