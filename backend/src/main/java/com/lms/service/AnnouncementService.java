package com.lms.service;

import com.lms.dto.AnnouncementRequest;
import com.lms.dto.AnnouncementResponse;
import com.lms.entity.Announcement;
import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.AnnouncementRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditLogService auditLogService;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               CourseRepository courseRepository,
                               UserRepository userRepository,
                               EnrollmentRepository enrollmentRepository,
                               AuditLogService auditLogService) {
        this.announcementRepository = announcementRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with email: " + authorEmail));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

            if (author.getRole() == Role.TEACHER && !course.getTeacher().getId().equals(author.getId())) {
                throw new ForbiddenException("You are not authorized to post announcements for this course");
            }
        } else {
            // University-wide announcement requires ADMIN
            if (author.getRole() != Role.ADMIN) {
                throw new ForbiddenException("Only Administrators can post university-wide announcements");
            }
        }

        Announcement announcement = new Announcement(
                request.getTitle(),
                request.getContent(),
                course,
                request.getPriority(),
                author,
                request.getTargetRole()
        );

        announcement = announcementRepository.save(announcement);

        auditLogService.logAction(authorEmail, "ANNOUNCEMENT_CREATED", "Announcement",
                String.valueOf(announcement.getId()), "Title: " + announcement.getTitle(), null);

        return mapToResponse(announcement);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAnnouncementsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() == Role.ADMIN) {
            return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (user.getRole() == Role.TEACHER) {
            List<Course> teacherCourses = courseRepository.findByTeacherId(user.getId());
            List<Long> courseIds = teacherCourses.stream().map(Course::getId).collect(Collectors.toList());
            if (courseIds.isEmpty()) {
                return announcementRepository.findUniversityWideAnnouncements().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
            }
            return announcementRepository.findForStudentCourses(courseIds).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        // Student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(user.getId());
        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());
        if (courseIds.isEmpty()) {
            return announcementRepository.findUniversityWideAnnouncements().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return announcementRepository.findForStudentCourses(courseIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getCourseAnnouncements(Long courseId) {
        return announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAnnouncement(Long id, String userEmail) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() != Role.ADMIN && !announcement.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to delete this announcement");
        }

        announcementRepository.delete(announcement);
        auditLogService.logAction(userEmail, "ANNOUNCEMENT_DELETED", "Announcement", String.valueOf(id), "Deleted by " + userEmail, null);
    }

    private AnnouncementResponse mapToResponse(Announcement a) {
        return new AnnouncementResponse(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCourse() != null ? a.getCourse().getId() : null,
                a.getCourse() != null ? a.getCourse().getTitle() : "University-Wide",
                a.getPriority(),
                a.getAuthor().getId(),
                a.getAuthor().getName(),
                a.getTargetRole(),
                a.getCreatedAt()
        );
    }
}

