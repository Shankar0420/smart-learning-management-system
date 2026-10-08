package com.lms.service;

import com.lms.dto.LessonRequest;
import com.lms.dto.LessonResponse;
import com.lms.entity.Lesson;
import com.lms.entity.Module;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.LessonProgressRepository;
import com.lms.repository.LessonRepository;
import com.lms.repository.ModuleRepository;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentService enrollmentService;

    public LessonService(LessonRepository lessonRepository,
                         ModuleRepository moduleRepository,
                         UserRepository userRepository,
                         LessonProgressRepository lessonProgressRepository,
                         EnrollmentService enrollmentService) {
        this.lessonRepository = lessonRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.enrollmentService = enrollmentService;
    }

    public List<LessonResponse> getLessonsByModuleId(Long moduleId, String userEmail) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(module.getCourse().getId(), userEmail);

        return lessonRepository.findByModuleOrderByOrderIndexAsc(module).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse getLessonById(Long id, String userEmail) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(lesson.getModule().getCourse().getId(), userEmail);

        return mapToResponse(lesson);
    }

    @Transactional
    public LessonResponse createLesson(Long moduleId, LessonRequest request, String teacherEmail) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !module.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can add lessons to this module");
        }

        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setModule(module);
        lesson.setOrderIndex(request.getOrderIndex());

        Lesson saved = lessonRepository.save(lesson);
        return mapToResponse(saved);
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonRequest request, String teacherEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !lesson.getModule().getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can edit this lesson");
        }

        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setOrderIndex(request.getOrderIndex());

        Lesson updated = lessonRepository.save(lesson);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteLesson(Long lessonId, String teacherEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !lesson.getModule().getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can delete this lesson");
        }

        // Clean dependent progress records
        lessonProgressRepository.deleteAll(lessonProgressRepository.findAll().stream()
                .filter(lp -> lp.getLesson().getId().equals(lessonId))
                .collect(Collectors.toList()));

        lessonRepository.delete(lesson);
    }

    public LessonResponse mapToResponse(Lesson lesson) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setTitle(lesson.getTitle());
        response.setContent(lesson.getContent());
        response.setModuleId(lesson.getModule().getId());
        response.setOrderIndex(lesson.getOrderIndex());
        response.setCreatedAt(lesson.getCreatedAt());
        return response;
    }
}
