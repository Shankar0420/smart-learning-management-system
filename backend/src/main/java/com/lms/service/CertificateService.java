package com.lms.service;

import com.lms.dto.CertificateResponse;
import com.lms.dto.CourseProgressResponse;
import com.lms.entity.Certificate;
import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CertificateRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonProgressService progressService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public CertificateService(CertificateRepository certificateRepository,
                              CourseRepository courseRepository,
                              UserRepository userRepository,
                              LessonProgressService progressService,
                              NotificationService notificationService,
                              AuditLogService auditLogService) {
        this.certificateRepository = certificateRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.progressService = progressService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    public List<CertificateResponse> getStudentCertificates(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        return certificateRepository.findByStudentIdOrderByIssuedAtDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CertificateResponse generateCertificate(Long courseId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        // Check if certificate already exists
        if (certificateRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            Certificate existing = certificateRepository.findByStudentIdAndCourseId(student.getId(), courseId).get();
            return mapToResponse(existing);
        }

        CourseProgressResponse progress = progressService.getCourseProgress(courseId, student.getEmail());
        if (progress.getProgressPercentage() < 100) {
            throw new BadRequestException("Certificate can only be issued upon 100% course syllabus completion (Current progress: " + progress.getProgressPercentage() + "%)");
        }

        String code = "GUL-" + course.getId() + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Certificate certificate = new Certificate(code, student, course, "HONORS", 100);
        Certificate saved = certificateRepository.save(certificate);

        notificationService.sendNotification(
                student,
                "Academic Certificate Issued!",
                "Congratulations! Your official certificate for '" + course.getTitle() + "' has been generated.",
                "RESULT",
                "/student/grades.html"
        );

        auditLogService.logAction(studentEmail, "ISSUE_CERTIFICATE", "CERTIFICATE", String.valueOf(saved.getId()), "Issued certificate: " + code, null);
        return mapToResponse(saved);
    }

    public CertificateResponse verifyCertificate(String code) {
        Certificate cert = certificateRepository.findByCertificateCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found or invalid certificate code: " + code));

        return mapToResponse(cert);
    }

    private CertificateResponse mapToResponse(Certificate c) {
        CertificateResponse res = new CertificateResponse();
        res.setId(c.getId());
        res.setCertificateCode(c.getCertificateCode());
        res.setStudentId(c.getStudent().getId());
        res.setStudentName(c.getStudent().getName());
        res.setStudentEmail(c.getStudent().getEmail());
        res.setCourseId(c.getCourse().getId());
        res.setCourseTitle(c.getCourse().getTitle());
        res.setTeacherName(c.getCourse().getTeacher() != null ? c.getCourse().getTeacher().getName() : "Faculty");
        res.setGrade(c.getGrade());
        res.setCompletionPercentage(c.getCompletionPercentage());
        res.setVerified(c.isVerified());
        res.setIssuedAt(c.getIssuedAt());
        return res;
    }
}
