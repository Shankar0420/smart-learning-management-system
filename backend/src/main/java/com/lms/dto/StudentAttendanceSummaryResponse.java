package com.lms.dto;

import java.util.List;

public class StudentAttendanceSummaryResponse {
    private long totalClasses;
    private long presentClasses;
    private long absentClasses;
    private double overallPercentage;
    private List<CourseAttendanceItem> courseBreakdown;

    public static class CourseAttendanceItem {
        private Long courseId;
        private String courseTitle;
        private long totalSessions;
        private long attendedSessions;
        private double percentage;

        public CourseAttendanceItem() {
        }

        public CourseAttendanceItem(Long courseId, String courseTitle, long totalSessions, long attendedSessions, double percentage) {
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.totalSessions = totalSessions;
            this.attendedSessions = attendedSessions;
            this.percentage = percentage;
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

        public long getTotalSessions() {
            return totalSessions;
        }

        public void setTotalSessions(long totalSessions) {
            this.totalSessions = totalSessions;
        }

        public long getAttendedSessions() {
            return attendedSessions;
        }

        public void setAttendedSessions(long attendedSessions) {
            this.attendedSessions = attendedSessions;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(long totalClasses) {
        this.totalClasses = totalClasses;
    }

    public long getPresentClasses() {
        return presentClasses;
    }

    public void setPresentClasses(long presentClasses) {
        this.presentClasses = presentClasses;
    }

    public long getAbsentClasses() {
        return absentClasses;
    }

    public void setAbsentClasses(long absentClasses) {
        this.absentClasses = absentClasses;
    }

    public double getOverallPercentage() {
        return overallPercentage;
    }

    public void setOverallPercentage(double overallPercentage) {
        this.overallPercentage = overallPercentage;
    }

    public List<CourseAttendanceItem> getCourseBreakdown() {
        return courseBreakdown;
    }

    public void setCourseBreakdown(List<CourseAttendanceItem> courseBreakdown) {
        this.courseBreakdown = courseBreakdown;
    }
}

