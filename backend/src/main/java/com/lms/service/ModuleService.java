package com.lms.service;

import com.lms.dto.ModuleRequest;
import com.lms.dto.ModuleResponse;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.Module;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentService enrollmentService;

    public ModuleService(ModuleRepository moduleRepository,
                         CourseRepository courseRepository,
                         UserRepository userRepository,
                         LessonRepository lessonRepository,
                         LessonProgressRepository lessonProgressRepository,
                         EnrollmentService enrollmentService) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.enrollmentService = enrollmentService;
    }

    public List<ModuleResponse> getModulesByCourseId(Long courseId, String userEmail) {
        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(courseId, userEmail);
        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ModuleResponse getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
        return mapToResponse(module);
    }

    @Transactional
    public ModuleResponse createModule(Long courseId, ModuleRequest request, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can add modules to this course");
        }

        Module module = new Module();
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setCourse(course);
        module.setOrderIndex(request.getOrderIndex());

        Module saved = moduleRepository.save(module);
        return mapToResponse(saved);
    }

    @Transactional
    public ModuleResponse updateModule(Long moduleId, ModuleRequest request, String teacherEmail) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !module.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can edit this module");
        }

        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setOrderIndex(request.getOrderIndex());

        Module updated = moduleRepository.save(module);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteModule(Long moduleId, String teacherEmail) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !module.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can delete this module");
        }

        // Clean deletion of lessons & their progress
        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
        for (Lesson l : lessons) {
            lessonProgressRepository.deleteAll(lessonProgressRepository.findAll().stream()
                    .filter(lp -> lp.getLesson().getId().equals(l.getId()))
                    .collect(Collectors.toList()));
            lessonRepository.delete(l);
        }

        moduleRepository.delete(module);
    }

    public ModuleResponse mapToResponse(Module module) {
        ModuleResponse response = new ModuleResponse();
        response.setId(module.getId());
        response.setTitle(module.getTitle());
        response.setDescription(module.getDescription());
        response.setCourseId(module.getCourse().getId());
        response.setOrderIndex(module.getOrderIndex());
        response.setCreatedAt(module.getCreatedAt());
        return response;
    }
}
