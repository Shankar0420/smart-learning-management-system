package com.lms.service;

import com.lms.dto.*;
import com.lms.entity.Enrollment;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final LessonProgressService lessonProgressService;
    private final GradeService gradeService;

    public DashboardService(UserRepository userRepository,
                            CourseRepository courseRepository,
                            EnrollmentRepository enrollmentRepository,
                            LessonRepository lessonRepository,
                            LessonProgressRepository lessonProgressRepository,
                            AssignmentSubmissionRepository submissionRepository,
                            GradeRepository gradeRepository,
                            LessonProgressService lessonProgressService,
                            GradeService gradeService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
        this.lessonProgressService = lessonProgressService;
        this.gradeService = gradeService;
    }

    public StudentDashboardResponse getStudentDashboard(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + studentEmail));

        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        long totalEnrolledCourses = enrollments.size();

        long activeCourses = enrollments.stream()
                .filter(e -> e.getCourse().isActive())
                .count();

        long completedLessons = lessonProgressRepository.countCompletedByStudentId(student.getId());

        long pendingAssignments = submissionRepository.countPendingByStudentId(student.getId());

        List<GradeResponse> recentGrades = gradeRepository.findByStudentId(student.getId()).stream()
                .limit(5)
                .map(gradeService::mapToResponse)
                .collect(Collectors.toList());

        List<CourseProgressResponse> courseProgressList = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            courseProgressList.add(lessonProgressService.getCourseProgress(enrollment.getCourse().getId(), studentEmail));
        }

        return new StudentDashboardResponse(
                totalEnrolledCourses,
                activeCourses,
                completedLessons,
                pendingAssignments,
                recentGrades,
                courseProgressList
        );
    }

    public TeacherDashboardResponse getTeacherDashboard(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));

        long totalCourses = courseRepository.countByTeacher(teacher);

        List<Long> teacherCourseIds = courseRepository.findByTeacher(teacher).stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());

        long totalStudents = teacherCourseIds.isEmpty() ? 0 : enrollmentRepository.countByCourseIdIn(teacherCourseIds);

        long pendingSubmissions = submissionRepository.countPendingByTeacherId(teacher.getId());

        return new TeacherDashboardResponse(
                totalCourses,
                totalStudents,
                pendingSubmissions
        );
    }

    public AdminDashboardResponse getAdminDashboard() {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalTeachers = userRepository.countByRole(Role.TEACHER);
        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();

        return new AdminDashboardResponse(
                totalStudents,
                totalTeachers,
                totalCourses,
                totalEnrollments
        );
    }
}
