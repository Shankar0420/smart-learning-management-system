package com.lms.service;

import com.lms.dto.StudyMaterialResponse;
import com.lms.entity.Course;
import com.lms.entity.Role;
import com.lms.entity.StudyMaterial;
import com.lms.entity.StudyMaterialBookmark;
import com.lms.entity.User;
import com.lms.exception.BadRequestException;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.StudyMaterialBookmarkRepository;
import com.lms.repository.StudyMaterialRepository;
import com.lms.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudyMaterialService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final StudyMaterialBookmarkRepository bookmarkRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final FileStorageService fileStorageService;

    public StudyMaterialService(StudyMaterialRepository studyMaterialRepository,
                                StudyMaterialBookmarkRepository bookmarkRepository,
                                CourseRepository courseRepository,
                                UserRepository userRepository,
                                EnrollmentRepository enrollmentRepository,
                                EnrollmentService enrollmentService,
                                FileStorageService fileStorageService) {
        this.studyMaterialRepository = studyMaterialRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public StudyMaterialResponse uploadMaterial(Long courseId,
                                                MultipartFile file,
                                                String title,
                                                String description,
                                                String teacherEmail) {
        return uploadMaterial(courseId, file, title, description, "LECTURE_NOTES", null, null, null, null, teacherEmail);
    }

    @Transactional
    public StudyMaterialResponse uploadMaterial(Long courseId,
                                                MultipartFile file,
                                                String title,
                                                String description,
                                                String category,
                                                String semester,
                                                String academicYear,
                                                String department,
                                                String subject,
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
        material.setCategory(category != null && !category.isBlank() ? category : "LECTURE_NOTES");
        material.setSemester(semester != null && !semester.isBlank() ? semester : null);
        material.setAcademicYear(academicYear != null && !academicYear.isBlank() ? academicYear : "2026-2027");
        material.setDepartment(department != null && !department.isBlank() ? department : course.getTeacher().getDepartment());
        material.setSubject(subject != null && !subject.isBlank() ? subject : course.getTitle());
        material.setPublished(true);
        material.setUploadedBy(teacher);

        StudyMaterial saved = studyMaterialRepository.save(material);
        return mapToResponse(saved, teacher.getId());
    }

    @Transactional
    public StudyMaterialResponse updateMaterial(Long materialId,
                                                String title,
                                                String description,
                                                String category,
                                                String semester,
                                                Boolean published,
                                                String userEmail) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found with id: " + materialId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        boolean isCourseTeacher = material.getCourse().getTeacher().getId().equals(user.getId());
        boolean isUploader = material.getUploadedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAdmin && !isCourseTeacher && !isUploader) {
            throw new ForbiddenException("You are not authorized to edit this study material");
        }

        if (title != null && title.trim().length() >= 2) {
            material.setTitle(title.trim());
        }
        if (description != null) {
            material.setDescription(description.trim());
        }
        if (category != null && !category.isBlank()) {
            material.setCategory(category.trim());
        }
        if (semester != null) {
            material.setSemester(semester.trim());
        }
        if (published != null) {
            material.setPublished(published);
        }

        StudyMaterial updated = studyMaterialRepository.save(material);
        return mapToResponse(updated, user.getId());
    }

    @Transactional
    public StudyMaterialResponse replaceMaterialFile(Long materialId, MultipartFile file, String userEmail) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found with id: " + materialId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        boolean isCourseTeacher = material.getCourse().getTeacher().getId().equals(user.getId());
        boolean isUploader = material.getUploadedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAdmin && !isCourseTeacher && !isUploader) {
            throw new ForbiddenException("You are not authorized to replace this file");
        }

        // Delete old file
        fileStorageService.deleteFile("materials", material.getFileName());

        // Store new file
        String storedFileName = fileStorageService.storeMaterialFile(file);
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "material";
        String ext = fileStorageService.getFileExtension(originalFilename).toUpperCase();

        material.setFileName(storedFileName);
        material.setOriginalFileName(originalFilename);
        material.setContentType(file.getContentType());
        material.setFileSize(file.getSize());
        material.setFileType(ext.isEmpty() ? "DOCUMENT" : ext);
        material.setFilePath("materials/" + storedFileName);

        StudyMaterial updated = studyMaterialRepository.save(material);
        return mapToResponse(updated, user.getId());
    }

    @Transactional
    public boolean toggleBookmark(Long materialId, String userEmail) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found with id: " + materialId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (bookmarkRepository.existsByUserIdAndMaterialId(user.getId(), materialId)) {
            bookmarkRepository.deleteByUserIdAndMaterialId(user.getId(), materialId);
            return false;
        } else {
            StudyMaterialBookmark bookmark = new StudyMaterialBookmark(material, user);
            bookmarkRepository.save(bookmark);
            return true;
        }
    }

    public List<StudyMaterialResponse> getBookmarkedMaterials(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<StudyMaterialBookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return bookmarks.stream()
                .map(b -> mapToResponse(b.getMaterial(), user.getId()))
                .collect(Collectors.toList());
    }

    public List<StudyMaterialResponse> searchMaterials(String userEmail, String keyword, String category, String semester, Long courseId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Long> courseIds = new ArrayList<>();
        if (courseId != null) {
            enrollmentService.checkStudentEnrollmentOrTeacherAdmin(courseId, userEmail);
            courseIds.add(courseId);
        } else if (user.getRole() == Role.STUDENT) {
            courseIds = enrollmentRepository.findByStudentId(user.getId()).stream()
                    .map(e -> e.getCourse().getId())
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.TEACHER) {
            courseIds = courseRepository.findByTeacherId(user.getId()).stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
        } else {
            courseIds = courseRepository.findAll().stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
        }

        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }

        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String searchCategory = (category != null && !category.isBlank()) ? category.trim() : null;
        String searchSemester = (semester != null && !semester.isBlank()) ? semester.trim() : null;

        return studyMaterialRepository.searchMaterials(courseIds, searchCategory, searchSemester, searchKeyword).stream()
                .map(m -> mapToResponse(m, user.getId()))
                .collect(Collectors.toList());
    }

    public List<StudyMaterialResponse> getMaterialsByCourse(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Enforce student enrollment or teacher/admin access
        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(courseId, userEmail);

        return studyMaterialRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(m -> mapToResponse(m, user.getId()))
                .collect(Collectors.toList());
    }

    public List<StudyMaterialResponse> getMaterialsByTeacher(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        return studyMaterialRepository.findByUploadedById(teacher.getId()).stream()
                .map(m -> mapToResponse(m, teacher.getId()))
                .collect(Collectors.toList());
    }

    public StudyMaterial getMaterialEntity(Long materialId, String userEmail) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found with id: " + materialId));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(material.getCourse().getId(), userEmail);
        return material;
    }

    @Transactional
    public Resource loadMaterialFile(Long materialId, String userEmail) {
        StudyMaterial material = getMaterialEntity(materialId, userEmail);
        material.setDownloadCount(material.getDownloadCount() + 1);
        studyMaterialRepository.save(material);
        return fileStorageService.loadFileAsResource("materials", material.getFileName());
    }

    @Transactional
    public Resource previewMaterialFile(Long materialId, String userEmail) {
        StudyMaterial material = getMaterialEntity(materialId, userEmail);
        material.setViewCount(material.getViewCount() + 1);
        studyMaterialRepository.save(material);
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
        return mapToResponse(m, null);
    }

    public StudyMaterialResponse mapToResponse(StudyMaterial m, Long currentUserId) {
        StudyMaterialResponse res = new StudyMaterialResponse(
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
        res.setCategory(m.getCategory());
        res.setSemester(m.getSemester());
        res.setAcademicYear(m.getAcademicYear());
        res.setDepartment(m.getDepartment());
        res.setSubject(m.getSubject());
        res.setPublished(m.isPublished());
        res.setDownloadCount(m.getDownloadCount());
        res.setViewCount(m.getViewCount());
        if (currentUserId != null) {
            res.setBookmarked(bookmarkRepository.existsByUserIdAndMaterialId(currentUserId, m.getId()));
        }
        return res;
    }
}
