package com.lms.dto;

public class AdminDashboardResponse {

    private long totalStudents;
    private long totalTeachers;
    private long totalCourses;
    private long totalEnrollments;
    private long pendingTeachers;
    private long pendingStudents;
    private long activeAccounts;
    private long inactiveAccounts;

    public AdminDashboardResponse() {}

    public AdminDashboardResponse(long totalStudents, long totalTeachers, long totalCourses, long totalEnrollments) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalCourses = totalCourses;
        this.totalEnrollments = totalEnrollments;
        this.pendingTeachers = 0;
        this.pendingStudents = 0;
        this.activeAccounts = 0;
        this.inactiveAccounts = 0;
    }

    public AdminDashboardResponse(long totalStudents, long totalTeachers, long totalCourses, long totalEnrollments, long pendingTeachers) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalCourses = totalCourses;
        this.totalEnrollments = totalEnrollments;
        this.pendingTeachers = pendingTeachers;
        this.pendingStudents = 0;
        this.activeAccounts = 0;
        this.inactiveAccounts = 0;
    }

    public AdminDashboardResponse(long totalStudents, long totalTeachers, long totalCourses, long totalEnrollments, long pendingTeachers, long pendingStudents) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalCourses = totalCourses;
        this.totalEnrollments = totalEnrollments;
        this.pendingTeachers = pendingTeachers;
        this.pendingStudents = pendingStudents;
        this.activeAccounts = 0;
        this.inactiveAccounts = 0;
    }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }

    public long getTotalCourses() { return totalCourses; }
    public void setTotalCourses(long totalCourses) { this.totalCourses = totalCourses; }

    public long getTotalEnrollments() { return totalEnrollments; }
    public void setTotalEnrollments(long totalEnrollments) { this.totalEnrollments = totalEnrollments; }

    public long getPendingTeachers() { return pendingTeachers; }
    public void setPendingTeachers(long pendingTeachers) { this.pendingTeachers = pendingTeachers; }

    public long getPendingStudents() { return pendingStudents; }
    public void setPendingStudents(long pendingStudents) { this.pendingStudents = pendingStudents; }

    public long getActiveAccounts() { return activeAccounts; }
    public void setActiveAccounts(long activeAccounts) { this.activeAccounts = activeAccounts; }

    public long getInactiveAccounts() { return inactiveAccounts; }
    public void setInactiveAccounts(long inactiveAccounts) { this.inactiveAccounts = inactiveAccounts; }
}
