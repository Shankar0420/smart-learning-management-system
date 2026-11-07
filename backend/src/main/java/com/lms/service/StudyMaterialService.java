package com.lms.service;

import com.lms.dto.StudyMaterialResponse;
import com.lms.entity.Course;
import com.lms.entity.Role;
import com.lms.entity.StudyMaterial;
import com.lms.entity.User;
import com.lms.exception.BadRequestException;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.StudyMaterialRepository;
import com.lms.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudyMaterialService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final FileStorageService fileStorageService;

    public StudyMaterialService(StudyMaterialRepository studyMaterialRepository,
                                CourseRepository courseRepository,
                                UserRepository userRepository,
                                EnrollmentService enrollmentService,
                                FileStorageService fileStorageService) {
        this.studyMaterialRepository = studyMaterialRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public StudyMaterialResponse uploadMaterial(Long courseId,
                                                MultipartFile file,
                                                String title,
                                                String description,
                                                String teacherEmail) {
        if (title == null || title.trim().length() < 2) {
            throw new BadRequestException("Material title must be at least 2 characters");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        // Authorization: Only the course's teacher or an Admin can upload
        if (teacher.getRole() != Role.ADMIN && !course.getTeacher().getId().equals(teacher.getId())) {
            throw new ForbiddenException("Only the course faculty or an administrator can upload study materials for this course");
        }

        String storedFileName = fileStorageService.storeMaterialFile(file);
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "material";
        String ext = fileStorageService.getFileExtension(originalFilename).toUpperCase();

        StudyMaterial material = new StudyMaterial();
        material.setTitle(title.trim());
        material.setDescription(description != null ? description.trim() : null);
        material.setCourse(course);
        material.setFileName(storedFileName);
        material.setOriginalFileName(originalFilename);
        material.setContentType(file.getContentType());
        material.setFileSize(file.getSize());
        material.setFileType(ext.isEmpty() ? "DOCUMENT" : ext);
        material.setFilePath("materials/" + storedFileName);
        material.setUploadedBy(teacher);

        StudyMaterial saved = studyMaterialRepository.save(material);
        return mapToResponse(saved);
    }

    public List<StudyMaterialResponse> getMaterialsByCourse(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Enforce student enrollment or teacher/admin access
        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(courseId, userEmail);

        return studyMaterialRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<StudyMaterialResponse> getMaterialsByTeacher(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        return studyMaterialRepository.findByUploadedById(teacher.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public StudyMaterial getMaterialEntity(Long materialId, String userEmail) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found with id: " + materialId));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(material.getCourse().getId(), userEmail);
        return material;
    }

    public Resource loadMaterialFile(Long materialId, String userEmail) {
        StudyMaterial material = getMaterialEntity(materialId, userEmail);
        return fileStorageService.loadFileAsResource("materials", material.getFileName());
    }

    @Transactional
    public void deleteMaterial(Long materialId, String userEmail) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found with id: " + materialId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        boolean isCourseTeacher = material.getCourse().getTeacher().getId().equals(user.getId());
        boolean isUploader = material.getUploadedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAdmin && !isCourseTeacher && !isUploader) {
            throw new ForbiddenException("You are not authorized to delete this study material");
        }

        // Delete physical file
        fileStorageService.deleteFile("materials", material.getFileName());
        studyMaterialRepository.delete(material);
    }

    public StudyMaterialResponse mapToResponse(StudyMaterial m) {
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

