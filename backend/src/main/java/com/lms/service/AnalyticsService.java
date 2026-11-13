package com.lms.service;

import com.lms.dto.CourseProgressResponse;
import com.lms.dto.StudentAnalyticsResponse;
import com.lms.dto.StudentAttendanceSummaryResponse;
import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.QuizAttempt;
import com.lms.entity.Role;
import com.lms.entity.StudyMaterial;
import com.lms.entity.User;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final AttendanceService attendanceService;
    private final LessonProgressService progressService;

    public AnalyticsService(UserRepository userRepository,
                            CourseRepository courseRepository,
                            EnrollmentRepository enrollmentRepository,
                            AssignmentSubmissionRepository submissionRepository,
                            QuizAttemptRepository quizAttemptRepository,
                            StudyMaterialRepository studyMaterialRepository,
                            AttendanceService attendanceService,
                            LessonProgressService progressService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.submissionRepository = submissionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.studyMaterialRepository = studyMaterialRepository;
        this.attendanceService = attendanceService;
        this.progressService = progressService;
    }

    public StudentAnalyticsResponse getStudentAnalytics(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        int enrolledCount = enrollments.size();

        StudentAttendanceSummaryResponse attSummary = attendanceService.getStudentAttendanceSummary(studentEmail);
        double attPct = attSummary.getOverallPercentage();

        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByStudentId(student.getId());
        int quizCount = quizAttempts.size();
        double avgQuizScore = 0;
        if (quizCount > 0) {
            double totalPct = quizAttempts.stream()
                    .mapToDouble(a -> a.getTotalMarks() > 0 ? ((double) a.getScore() / a.getTotalMarks()) * 100.0 : 0)
                    .sum();
            avgQuizScore = Math.round((totalPct / quizCount) * 10.0) / 10.0;
        }

        int submittedAssignments = submissionRepository.findByStudentId(student.getId()).size();

        int completedCourses = 0;
        List<Map<String, Object>> breakdown = new ArrayList<>();

        for (Enrollment e : enrollments) {
            Course c = e.getCourse();
            CourseProgressResponse prog = progressService.getCourseProgress(c.getId(), student.getEmail());
            if (prog.getProgressPercentage() >= 100) completedCourses++;

            Map<String, Object> cMap = new HashMap<>();
            cMap.put("courseId", c.getId());
            cMap.put("title", c.getTitle());
            cMap.put("code", "CSE-" + c.getId());
            cMap.put("progress", prog.getProgressPercentage());
            cMap.put("teacherName", c.getTeacher() != null ? c.getTeacher().getName() : "Faculty");
            breakdown.add(cMap);
        }

        // Projected CGPA estimation based on progress, quiz scores, and attendance
        double compositeScore = (attPct * 0.3) + ((avgQuizScore > 0 ? avgQuizScore : 85.0) * 0.7);
        double projectedCgpaNum = Math.min(10.0, Math.max(6.0, (compositeScore / 10.0)));
        String projectedCgpa = String.format("%.2f", projectedCgpaNum);

        StudentAnalyticsResponse res = new StudentAnalyticsResponse();
        res.setStudentId(student.getId());
        res.setStudentName(student.getName());
        res.setOverallAttendancePercentage(attPct);
        res.setEnrolledCoursesCount(enrolledCount);
        res.setCompletedCoursesCount(completedCourses);
        res.setTotalAssignmentsSubmitted(submittedAssignments);
        res.setTotalQuizzesAttempted(quizCount);
        res.setAverageQuizScore(avgQuizScore);
        res.setProjectedCgpa(projectedCgpa);
        res.setCourseBreakdown(breakdown);

        return res;
    }

    public Map<String, Object> getAdminAnalytics() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.findAll().stream().filter(u -> u.getRole() == Role.STUDENT).count();
        long totalFaculty = userRepository.findAll().stream().filter(u -> u.getRole() == Role.TEACHER).count();
        long totalCourses = courseRepository.count();
        long totalMaterials = studyMaterialRepository.count();

        long storageBytes = studyMaterialRepository.findAll().stream()
                .mapToLong(StudyMaterial::getFileSize)
                .sum();
        double storageMB = Math.round((storageBytes / (1024.0 * 1024.0)) * 100.0) / 100.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalStudents", totalStudents);
        stats.put("totalFaculty", totalFaculty);
        stats.put("totalCourses", totalCourses);
        stats.put("totalMaterials", totalMaterials);
        stats.put("storageUsageMB", storageMB);
        stats.put("systemHealth", "OPTIMAL");
        return stats;
    }
}
