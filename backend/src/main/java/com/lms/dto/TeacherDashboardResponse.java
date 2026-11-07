package com.lms.dto;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardResponse {

    private long totalCourses;
    private long totalStudents;
    private long pendingSubmissions;
    private long totalMaterials;
    private long totalQuizzes;
    private long totalAssignments;
    private List<StudyMaterialResponse> recentMaterials = new ArrayList<>();

    public TeacherDashboardResponse() {}

    public TeacherDashboardResponse(long totalCourses, long totalStudents, long pendingSubmissions) {
        this.totalCourses = totalCourses;
        this.totalStudents = totalStudents;
        this.pendingSubmissions = pendingSubmissions;
        this.totalMaterials = 0;
        this.totalQuizzes = 0;
        this.totalAssignments = 0;
    }

    public long getTotalCourses() { return totalCourses; }
    public void setTotalCourses(long totalCourses) { this.totalCourses = totalCourses; }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getPendingSubmissions() { return pendingSubmissions; }
    public void setPendingSubmissions(long pendingSubmissions) { this.pendingSubmissions = pendingSubmissions; }

    public long getTotalMaterials() { return totalMaterials; }
    public void setTotalMaterials(long totalMaterials) { this.totalMaterials = totalMaterials; }

    public long getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(long totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public long getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(long totalAssignments) { this.totalAssignments = totalAssignments; }

    public List<StudyMaterialResponse> getRecentMaterials() { return recentMaterials; }
    public void setRecentMaterials(List<StudyMaterialResponse> recentMaterials) { this.recentMaterials = recentMaterials; }
}
