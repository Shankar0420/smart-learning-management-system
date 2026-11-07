package com.lms.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboardResponse {

    private String professionalId;
    private long totalEnrolledCourses;
    private long activeCourses;
    private long completedLessons;
    private long pendingAssignments;
    private long availableMaterialsCount;
    private long publishedQuizzesCount;
    private long totalAssignmentsCount;
    private long submittedAssignmentsCount;

    private List<GradeResponse> recentGrades = new ArrayList<>();
    private List<CourseProgressResponse> courseProgress = new ArrayList<>();
    private List<UpcomingAssignmentItem> upcomingAssignments = new ArrayList<>();
    private List<QuizScoreItem> recentQuizScores = new ArrayList<>();
    private List<RecentMaterialItem> recentMaterials = new ArrayList<>();

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

    public static class UpcomingAssignmentItem {
        private Long id;
        private String title;
        private Long courseId;
        private String courseTitle;
        private LocalDateTime deadline;
        private int maxMarks;
        private boolean submitted;
        private boolean graded;
        private Integer marksObtained;

        public UpcomingAssignmentItem() {}

        public UpcomingAssignmentItem(Long id, String title, Long courseId, String courseTitle,
                                    LocalDateTime deadline, int maxMarks, boolean submitted,
                                    boolean graded, Integer marksObtained) {
            this.id = id;
            this.title = title;
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.deadline = deadline;
            this.maxMarks = maxMarks;
            this.submitted = submitted;
            this.graded = graded;
            this.marksObtained = marksObtained;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
        public int getMaxMarks() { return maxMarks; }
        public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
        public boolean isSubmitted() { return submitted; }
        public void setSubmitted(boolean submitted) { this.submitted = submitted; }
        public boolean isGraded() { return graded; }
        public void setGraded(boolean graded) { this.graded = graded; }
        public Integer getMarksObtained() { return marksObtained; }
        public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }
    }

    public static class QuizScoreItem {
        private Long attemptId;
        private Long quizId;
        private String quizTitle;
        private String courseTitle;
        private int score;
        private int totalMarks;
        private LocalDateTime attemptedAt;

        public QuizScoreItem() {}

        public QuizScoreItem(Long attemptId, Long quizId, String quizTitle, String courseTitle, int score, int totalMarks, LocalDateTime attemptedAt) {
            this.attemptId = attemptId;
            this.quizId = quizId;
            this.quizTitle = quizTitle;
            this.courseTitle = courseTitle;
            this.score = score;
            this.totalMarks = totalMarks;
            this.attemptedAt = attemptedAt;
        }

        public Long getAttemptId() { return attemptId; }
        public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
        public Long getQuizId() { return quizId; }
        public void setQuizId(Long quizId) { this.quizId = quizId; }
        public String getQuizTitle() { return quizTitle; }
        public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getTotalMarks() { return totalMarks; }
        public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
        public LocalDateTime getAttemptedAt() { return attemptedAt; }
        public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
    }

    public static class RecentMaterialItem {
        private Long id;
        private String title;
        private Long courseId;
        private String courseTitle;
        private String fileType;
        private Long fileSize;
        private LocalDateTime createdAt;

        public RecentMaterialItem() {}

        public RecentMaterialItem(Long id, String title, Long courseId, String courseTitle, String fileType, Long fileSize, LocalDateTime createdAt) {
            this.id = id;
            this.title = title;
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public String getProfessionalId() { return professionalId; }
    public void setProfessionalId(String professionalId) { this.professionalId = professionalId; }

    public long getTotalEnrolledCourses() { return totalEnrolledCourses; }
    public void setTotalEnrolledCourses(long totalEnrolledCourses) { this.totalEnrolledCourses = totalEnrolledCourses; }

    public long getActiveCourses() { return activeCourses; }
    public void setActiveCourses(long activeCourses) { this.activeCourses = activeCourses; }

    public long getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(long completedLessons) { this.completedLessons = completedLessons; }

    public long getPendingAssignments() { return pendingAssignments; }
    public void setPendingAssignments(long pendingAssignments) { this.pendingAssignments = pendingAssignments; }

    public long getAvailableMaterialsCount() { return availableMaterialsCount; }
    public void setAvailableMaterialsCount(long availableMaterialsCount) { this.availableMaterialsCount = availableMaterialsCount; }

    public long getPublishedQuizzesCount() { return publishedQuizzesCount; }
    public void setPublishedQuizzesCount(long publishedQuizzesCount) { this.publishedQuizzesCount = publishedQuizzesCount; }

    public long getTotalAssignmentsCount() { return totalAssignmentsCount; }
    public void setTotalAssignmentsCount(long totalAssignmentsCount) { this.totalAssignmentsCount = totalAssignmentsCount; }

    public long getSubmittedAssignmentsCount() { return submittedAssignmentsCount; }
    public void setSubmittedAssignmentsCount(long submittedAssignmentsCount) { this.submittedAssignmentsCount = submittedAssignmentsCount; }

    public List<GradeResponse> getRecentGrades() { return recentGrades; }
    public void setRecentGrades(List<GradeResponse> recentGrades) { this.recentGrades = recentGrades; }

    public List<CourseProgressResponse> getCourseProgress() { return courseProgress; }
    public void setCourseProgress(List<CourseProgressResponse> courseProgress) { this.courseProgress = courseProgress; }

    public List<UpcomingAssignmentItem> getUpcomingAssignments() { return upcomingAssignments; }
    public void setUpcomingAssignments(List<UpcomingAssignmentItem> upcomingAssignments) { this.upcomingAssignments = upcomingAssignments; }

    public List<QuizScoreItem> getRecentQuizScores() { return recentQuizScores; }
    public void setRecentQuizScores(List<QuizScoreItem> recentQuizScores) { this.recentQuizScores = recentQuizScores; }

    public List<RecentMaterialItem> getRecentMaterials() { return recentMaterials; }
    public void setRecentMaterials(List<RecentMaterialItem> recentMaterials) { this.recentMaterials = recentMaterials; }
}
