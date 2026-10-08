package com.lms.dto;

import java.util.List;

public class StudentDashboardResponse {

    private long totalEnrolledCourses;
    private long activeCourses;
    private long completedLessons;
    private long pendingAssignments;
    private List<GradeResponse> recentGrades;
    private List<CourseProgressResponse> courseProgress;

    public StudentDashboardResponse() {}

    public StudentDashboardResponse(long totalEnrolledCourses, long activeCourses, long completedLessons,
                                  long pendingAssignments, List<GradeResponse> recentGrades,
                                  List<CourseProgressResponse> courseProgress) {
        this.totalEnrolledCourses = totalEnrolledCourses;
        this.activeCourses = activeCourses;
        this.completedLessons = completedLessons;
        this.pendingAssignments = pendingAssignments;
        this.recentGrades = recentGrades;
        this.courseProgress = courseProgress;
    }

    public long getTotalEnrolledCourses() { return totalEnrolledCourses; }
    public void setTotalEnrolledCourses(long totalEnrolledCourses) { this.totalEnrolledCourses = totalEnrolledCourses; }

    public long getActiveCourses() { return activeCourses; }
    public void setActiveCourses(long activeCourses) { this.activeCourses = activeCourses; }

    public long getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(long completedLessons) { this.completedLessons = completedLessons; }

    public long getPendingAssignments() { return pendingAssignments; }
    public void setPendingAssignments(long pendingAssignments) { this.pendingAssignments = pendingAssignments; }

    public List<GradeResponse> getRecentGrades() { return recentGrades; }
    public void setRecentGrades(List<GradeResponse> recentGrades) { this.recentGrades = recentGrades; }

    public List<CourseProgressResponse> getCourseProgress() { return courseProgress; }
    public void setCourseProgress(List<CourseProgressResponse> courseProgress) { this.courseProgress = courseProgress; }
}
