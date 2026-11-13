package com.lms.dto;

import java.util.List;
import java.util.Map;

public class StudentAnalyticsResponse {

    private Long studentId;
    private String studentName;
    private double overallAttendancePercentage;
    private int enrolledCoursesCount;
    private int completedCoursesCount;
    private int totalAssignmentsSubmitted;
    private int totalQuizzesAttempted;
    private double averageQuizScore;
    private String projectedCgpa;
    private List<Map<String, Object>> courseBreakdown;

    public StudentAnalyticsResponse() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public double getOverallAttendancePercentage() { return overallAttendancePercentage; }
    public void setOverallAttendancePercentage(double overallAttendancePercentage) { this.overallAttendancePercentage = overallAttendancePercentage; }

    public int getEnrolledCoursesCount() { return enrolledCoursesCount; }
    public void setEnrolledCoursesCount(int enrolledCoursesCount) { this.enrolledCoursesCount = enrolledCoursesCount; }

    public int getCompletedCoursesCount() { return completedCoursesCount; }
    public void setCompletedCoursesCount(int completedCoursesCount) { this.completedCoursesCount = completedCoursesCount; }

    public int getTotalAssignmentsSubmitted() { return totalAssignmentsSubmitted; }
    public void setTotalAssignmentsSubmitted(int totalAssignmentsSubmitted) { this.totalAssignmentsSubmitted = totalAssignmentsSubmitted; }

    public int getTotalQuizzesAttempted() { return totalQuizzesAttempted; }
    public void setTotalQuizzesAttempted(int totalQuizzesAttempted) { this.totalQuizzesAttempted = totalQuizzesAttempted; }

    public double getAverageQuizScore() { return averageQuizScore; }
    public void setAverageQuizScore(double averageQuizScore) { this.averageQuizScore = averageQuizScore; }

    public String getProjectedCgpa() { return projectedCgpa; }
    public void setProjectedCgpa(String projectedCgpa) { this.projectedCgpa = projectedCgpa; }

    public List<Map<String, Object>> getCourseBreakdown() { return courseBreakdown; }
    public void setCourseBreakdown(List<Map<String, Object>> courseBreakdown) { this.courseBreakdown = courseBreakdown; }
}

