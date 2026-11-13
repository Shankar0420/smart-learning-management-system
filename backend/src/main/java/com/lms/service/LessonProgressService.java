package com.lms.service;

import com.lms.dto.CourseProgressResponse;
import com.lms.dto.MessageResponse;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.LessonProgress;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonProgressRepository;
import com.lms.repository.LessonRepository;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;

    public LessonProgressService(LessonProgressRepository lessonProgressRepository,
                                 LessonRepository lessonRepository,
                                 CourseRepository courseRepository,
                                 UserRepository userRepository,
                                 EnrollmentService enrollmentService) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
    }

    @Transactional
    public MessageResponse completeLesson(Long lessonId, String studentEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + studentEmail));

        if (student.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can mark lessons as completed");
        }

        Long courseId = lesson.getModule().getCourse().getId();
        if (!enrollmentService.isStudentEnrolled(student.getId(), courseId)) {
            throw new ForbiddenException("You must be enrolled in the course to complete its lessons");
        }

        Optional<LessonProgress> existing = lessonProgressRepository.findByStudentIdAndLessonId(student.getId(), lessonId);
        if (existing.isPresent()) {
            LessonProgress progress = existing.get();
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
        } else {
            LessonProgress progress = new LessonProgress(student, lesson);
            lessonProgressRepository.save(progress);
        }

        return new MessageResponse("Lesson marked as completed successfully");
    }

    public CourseProgressResponse getCourseProgress(Long courseId, String studentEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + studentEmail));

        // Calculated directly from MariaDB data
        long totalLessons = lessonRepository.countByCourseId(courseId);
        long completedLessons = lessonProgressRepository.countCompletedByStudentAndCourse(student.getId(), courseId);

        double progressPercentage = 0.0;
        if (totalLessons > 0) {
            progressPercentage = ((double) completedLessons / totalLessons) * 100.0;
            progressPercentage = Math.round(progressPercentage * 10.0) / 10.0;
        }

        return new CourseProgressResponse(
                course.getId(),
                course.getTitle(),
                totalLessons,
                completedLessons,
                progressPercentage
        );
    }
}
