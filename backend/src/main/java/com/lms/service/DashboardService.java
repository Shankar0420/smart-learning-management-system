package com.lms.service;

import com.lms.dto.*;
import com.lms.entity.ApprovalStatus;
import com.lms.entity.Enrollment;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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
    private final StudyMaterialRepository studyMaterialRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AssignmentRepository assignmentRepository;

    public DashboardService(UserRepository userRepository,
                            CourseRepository courseRepository,
                            EnrollmentRepository enrollmentRepository,
                            LessonRepository lessonRepository,
                            LessonProgressRepository lessonProgressRepository,
                            AssignmentSubmissionRepository submissionRepository,
                            GradeRepository gradeRepository,
                            LessonProgressService lessonProgressService,
                            GradeService gradeService,
                            StudyMaterialRepository studyMaterialRepository,
                            QuizRepository quizRepository,
                            QuizAttemptRepository quizAttemptRepository,
                            AssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
        this.lessonProgressService = lessonProgressService;
        this.gradeService = gradeService;
        this.studyMaterialRepository = studyMaterialRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.assignmentRepository = assignmentRepository;
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

        List<Long> enrolledCourseIds = enrollments.stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toList());

        long availableMaterialsCount = enrolledCourseIds.isEmpty() ? 0 : studyMaterialRepository.countByCourseIdIn(enrolledCourseIds);

        long publishedQuizzesCount = enrolledCourseIds.isEmpty() ? 0 : quizRepository.findAll().stream()
                .filter(q -> enrolledCourseIds.contains(q.getCourse().getId()) && q.isActive())
                .count();

        List<com.lms.entity.Assignment> allEnrolledAssignments = enrolledCourseIds.isEmpty() ? new ArrayList<>() :
                assignmentRepository.findAll().stream()
                        .filter(a -> enrolledCourseIds.contains(a.getCourse().getId()))
                        .collect(Collectors.toList());

        long totalAssignmentsCount = allEnrolledAssignments.size();

        List<com.lms.entity.AssignmentSubmission> studentSubmissions = submissionRepository.findByStudentId(student.getId());
        long submittedAssignmentsCount = studentSubmissions.size();

        long pendingAssignments = Math.max(0, totalAssignmentsCount - submittedAssignmentsCount);

        List<GradeResponse> recentGrades = gradeRepository.findByStudentId(student.getId()).stream()
                .limit(5)
                .map(gradeService::mapToResponse)
                .collect(Collectors.toList());

        List<CourseProgressResponse> courseProgressList = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            courseProgressList.add(lessonProgressService.getCourseProgress(enrollment.getCourse().getId(), studentEmail));
        }

        StudentDashboardResponse response = new StudentDashboardResponse(
                totalEnrolledCourses,
                activeCourses,
                completedLessons,
                pendingAssignments,
                recentGrades,
                courseProgressList
        );
        response.setProfessionalId(student.getProfessionalId());
        response.setAvailableMaterialsCount(availableMaterialsCount);
        response.setPublishedQuizzesCount(publishedQuizzesCount);
        response.setTotalAssignmentsCount(totalAssignmentsCount);
        response.setSubmittedAssignmentsCount(submittedAssignmentsCount);

        // Upcoming assignments with submission status
        List<StudentDashboardResponse.UpcomingAssignmentItem> upcoming = allEnrolledAssignments.stream()
                .sorted(java.util.Comparator.comparing(com.lms.entity.Assignment::getDeadline))
                .limit(5)
                .map(a -> {
                    Optional<com.lms.entity.AssignmentSubmission> subOpt = studentSubmissions.stream()
                            .filter(s -> s.getAssignment().getId().equals(a.getId()))
                            .findFirst();
                    boolean isSub = subOpt.isPresent();
                    boolean isGraded = isSub && subOpt.get().isGraded();
                    Integer marks = isSub ? subOpt.get().getMarksObtained() : null;
                    return new StudentDashboardResponse.UpcomingAssignmentItem(
                            a.getId(),
                            a.getTitle(),
                            a.getCourse().getId(),
                            a.getCourse().getTitle(),
                            a.getDeadline(),
                            a.getMaxMarks(),
                            isSub,
                            isGraded,
                            marks
                    );
                })
                .collect(Collectors.toList());
        response.setUpcomingAssignments(upcoming);

        // Recent Quiz scores
        List<StudentDashboardResponse.QuizScoreItem> recentScores = quizAttemptRepository.findByStudentIdOrderByAttemptedAtDesc(student.getId()).stream()
                .limit(5)
                .map(qa -> new StudentDashboardResponse.QuizScoreItem(
                        qa.getId(),
                        qa.getQuiz().getId(),
                        qa.getQuiz().getTitle(),
                        qa.getQuiz().getCourse().getTitle(),
                        qa.getScore(),
                        qa.getTotalMarks(),
                        qa.getAttemptedAt()
                ))
                .collect(Collectors.toList());
        response.setRecentQuizScores(recentScores);

        // Recent study materials
        if (!enrolledCourseIds.isEmpty()) {
            List<StudentDashboardResponse.RecentMaterialItem> recentMats = studyMaterialRepository.findAll().stream()
                    .filter(m -> enrolledCourseIds.contains(m.getCourse().getId()))
                    .sorted((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()))
                    .limit(5)
                    .map(m -> new StudentDashboardResponse.RecentMaterialItem(
                            m.getId(),
                            m.getTitle(),
                            m.getCourse().getId(),
                            m.getCourse().getTitle(),
                            m.getFileType(),
                            m.getFileSize(),
                            m.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            response.setRecentMaterials(recentMats);
        }

        return response;
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

        long totalMaterials = teacherCourseIds.isEmpty() ? 0 : studyMaterialRepository.countByCourseIdIn(teacherCourseIds);
        long totalQuizzes = teacherCourseIds.isEmpty() ? 0 : quizRepository.countByCourseIdIn(teacherCourseIds);
        long totalAssignments = teacherCourseIds.isEmpty() ? 0 : assignmentRepository.countByCourseIdIn(teacherCourseIds);

        List<StudyMaterialResponse> recentMats = studyMaterialRepository.findByUploadedById(teacher.getId()).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(this::mapMaterialToResponse)
                .collect(Collectors.toList());

        TeacherDashboardResponse response = new TeacherDashboardResponse(
                totalCourses,
                totalStudents,
                pendingSubmissions
        );
        response.setTotalMaterials(totalMaterials);
        response.setTotalQuizzes(totalQuizzes);
        response.setTotalAssignments(totalAssignments);
        response.setRecentMaterials(recentMats);
        return response;
    }

    public AdminDashboardResponse getAdminDashboard() {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalTeachers = userRepository.countByRole(Role.TEACHER);
        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long pendingTeachers = userRepository.countByRoleAndApprovalStatus(Role.TEACHER, ApprovalStatus.PENDING);
        long pendingStudents = userRepository.countByRoleAndApprovalStatus(Role.STUDENT, ApprovalStatus.PENDING);
        long activeAccounts = userRepository.countByActive(true);
        long inactiveAccounts = userRepository.countByActive(false);

        AdminDashboardResponse response = new AdminDashboardResponse(
                totalStudents,
                totalTeachers,
                totalCourses,
                totalEnrollments,
                pendingTeachers,
                pendingStudents
        );
        response.setActiveAccounts(activeAccounts);
        response.setInactiveAccounts(inactiveAccounts);
        return response;
    }

    private StudyMaterialResponse mapMaterialToResponse(com.lms.entity.StudyMaterial m) {
        return new StudyMaterialResponse(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getCourse().getId(),
                m.getCourse().getTitle(),
                m.getFileName(),
                m.getOriginalFileName(),
                m.getContentType(),
                m.getFileSize(),
                m.getFileType(),
                m.getUploadedBy() != null ? m.getUploadedBy().getName() : "Faculty",
                m.getCreatedAt()
        );
    }
}

