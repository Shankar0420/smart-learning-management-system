package com.lms.service;

import com.lms.dto.FacultyDocumentResponse;
import com.lms.dto.FacultyProfileResponse;
import com.lms.entity.ApprovalStatus;
import com.lms.entity.Course;
import com.lms.entity.FacultyDocument;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.BadRequestException;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.FacultyDocumentRepository;
import com.lms.repository.StudyMaterialRepository;
import com.lms.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FacultyManagementService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final FacultyDocumentRepository facultyDocumentRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    public FacultyManagementService(UserRepository userRepository,
                                  CourseRepository courseRepository,
                                  StudyMaterialRepository studyMaterialRepository,
                                  FacultyDocumentRepository facultyDocumentRepository,
                                  FileStorageService fileStorageService,
                                  AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.studyMaterialRepository = studyMaterialRepository;
        this.facultyDocumentRepository = facultyDocumentRepository;
        this.fileStorageService = fileStorageService;
        this.auditLogService = auditLogService;
    }

    public List<FacultyProfileResponse> getAllFaculty(String department, String search) {
        List<User> facultyList = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TEACHER)
                .collect(Collectors.toList());

        if (department != null && !department.isBlank()) {
            facultyList = facultyList.stream()
                    .filter(u -> u.getDepartment() != null && u.getDepartment().equalsIgnoreCase(department.trim()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            facultyList = facultyList.stream()
                    .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(q)) ||
                                 (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) ||
                                 (u.getEmployeeId() != null && u.getEmployeeId().toLowerCase().contains(q)) ||
                                 (u.getDesignation() != null && u.getDesignation().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        return facultyList.stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    public FacultyProfileResponse getFacultyById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
        if (user.getRole() != Role.TEACHER) {
            throw new BadRequestException("User is not a faculty member");
        }
        return mapToProfileResponse(user);
    }

    @Transactional
    public FacultyProfileResponse updateFacultyProfile(Long facultyId, Map<String, Object> payload, String requesterEmail) {
        User user = userRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + facultyId));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));

        if (requester.getRole() != Role.ADMIN && !requester.getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to update this faculty profile");
        }

        if (payload.containsKey("name")) user.setName((String) payload.get("name"));
        if (payload.containsKey("phone")) user.setPhone((String) payload.get("phone"));
        if (payload.containsKey("department")) user.setDepartment((String) payload.get("department"));
        if (payload.containsKey("designation")) user.setDesignation((String) payload.get("designation"));
        if (payload.containsKey("qualification")) user.setQualification((String) payload.get("qualification"));
        if (payload.containsKey("specialization")) user.setSpecialization((String) payload.get("specialization"));
        if (payload.containsKey("joiningDate") && payload.get("joiningDate") != null) {
            try {
                user.setJoiningDate(LocalDate.parse(payload.get("joiningDate").toString()));
            } catch (Exception ignored) {}
        }

        User saved = userRepository.save(user);
        auditLogService.logAction(requesterEmail, "UPDATE_FACULTY_PROFILE", "USER", String.valueOf(user.getId()), "Updated profile for " + user.getName(), null);
        return mapToProfileResponse(saved);
    }

    @Transactional
    public FacultyProfileResponse updateFacultyStatus(Long facultyId, Boolean active, ApprovalStatus status, String adminEmail) {
        User user = userRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + facultyId));

        if (active != null) user.setActive(active);
        if (status != null) user.setApprovalStatus(status);

        User saved = userRepository.save(user);
        auditLogService.logAction(adminEmail, "UPDATE_FACULTY_STATUS", "USER", String.valueOf(user.getId()), "Status updated: active=" + active + ", approval=" + status, null);
        return mapToProfileResponse(saved);
    }

    @Transactional
    public FacultyDocumentResponse uploadFacultyDocument(MultipartFile file, String documentType, String title, String facultyEmail) {
        User faculty = userRepository.findByEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + facultyEmail));

        if (faculty.getRole() != Role.TEACHER && faculty.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only faculty or administrators can upload faculty documents");
        }

        if (title == null || title.trim().length() < 2) {
            throw new BadRequestException("Document title must be at least 2 characters");
        }

        String storedFileName = fileStorageService.storeFacultyDocument(file);
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";

        FacultyDocument doc = new FacultyDocument();
        doc.setFaculty(faculty);
        doc.setDocumentType(documentType != null ? documentType.toUpperCase() : "OTHER");
        doc.setTitle(title.trim());
        doc.setFileName(storedFileName);
        doc.setOriginalFileName(originalFilename);
        doc.setContentType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setFilePath("faculty-docs/" + storedFileName);

        FacultyDocument saved = facultyDocumentRepository.save(doc);
        auditLogService.logAction(facultyEmail, "UPLOAD_FACULTY_DOCUMENT", "FACULTY_DOC", String.valueOf(saved.getId()), "Uploaded " + doc.getDocumentType() + ": " + doc.getTitle(), null);
        return mapToDocResponse(saved);
    }

    public List<FacultyDocumentResponse> getFacultyDocuments(Long facultyId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));

        if (requester.getRole() != Role.ADMIN && !requester.getId().equals(facultyId)) {
            throw new ForbiddenException("You are not authorized to view these private documents");
        }

        return facultyDocumentRepository.findByFacultyIdOrderByCreatedAtDesc(facultyId).stream()
                .map(this::mapToDocResponse)
                .collect(Collectors.toList());
    }

    public Resource loadFacultyDocument(Long documentId, String requesterEmail) {
        FacultyDocument doc = facultyDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));

        if (requester.getRole() != Role.ADMIN && !requester.getId().equals(doc.getFaculty().getId())) {
            throw new ForbiddenException("You are not authorized to download this private document");
        }

        return fileStorageService.loadFileAsResource("faculty-docs", doc.getFileName());
    }

    public FacultyDocument getDocumentEntity(Long documentId, String requesterEmail) {
        FacultyDocument doc = facultyDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));

        if (requester.getRole() != Role.ADMIN && !requester.getId().equals(doc.getFaculty().getId())) {
            throw new ForbiddenException("You are not authorized to access this private document");
        }

        return doc;
    }

    @Transactional
    public void deleteFacultyDocument(Long documentId, String requesterEmail) {
        FacultyDocument doc = facultyDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));

        if (requester.getRole() != Role.ADMIN && !requester.getId().equals(doc.getFaculty().getId())) {
            throw new ForbiddenException("You are not authorized to delete this document");
        }

        fileStorageService.deleteFile("faculty-docs", doc.getFileName());
        facultyDocumentRepository.delete(doc);
        auditLogService.logAction(requesterEmail, "DELETE_FACULTY_DOCUMENT", "FACULTY_DOC", String.valueOf(documentId), "Deleted document: " + doc.getTitle(), null);
    }

    public Map<String, Object> getFacultyDashboardStats(String facultyEmail) {
        User faculty = userRepository.findByEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + facultyEmail));

        List<Course> courses = courseRepository.findByTeacherId(faculty.getId());
        int totalCourses = courses.size();
        int totalMaterials = studyMaterialRepository.findByUploadedById(faculty.getId()).size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("facultyName", faculty.getName());
        stats.put("department", faculty.getDepartment());
        stats.put("designation", faculty.getDesignation() != null ? faculty.getDesignation() : "Assistant Professor");
        stats.put("totalAssignedCourses", totalCourses);
        stats.put("totalUploadedMaterials", totalMaterials);
        stats.put("todayClassesCount", totalCourses > 0 ? Math.min(totalCourses, 3) : 0);
        return stats;
    }

    private FacultyProfileResponse mapToProfileResponse(User u) {
        FacultyProfileResponse res = new FacultyProfileResponse();
        res.setId(u.getId());
        res.setName(u.getName());
        res.setEmail(u.getEmail());
        res.setEmployeeId(u.getEmployeeId());
        res.setDepartment(u.getDepartment());
        res.setPhone(u.getPhone());
        res.setDesignation(u.getDesignation() != null ? u.getDesignation() : "Faculty Member");
        res.setQualification(u.getQualification());
        res.setSpecialization(u.getSpecialization());
        res.setJoiningDate(u.getJoiningDate());
        res.setProfilePhoto(u.getProfilePhoto());
        res.setApprovalStatus(u.getApprovalStatus());
        res.setActive(u.isActive());
        res.setCreatedAt(u.getCreatedAt());

        List<Course> courses = courseRepository.findByTeacherId(u.getId());
        res.setAssignedCourses(courses.stream().map(Course::getTitle).collect(Collectors.toList()));
        res.setTotalAssignedCourses(courses.size());
        res.setTotalUploadedMaterials(studyMaterialRepository.findByUploadedById(u.getId()).size());
        return res;
    }

    private FacultyDocumentResponse mapToDocResponse(FacultyDocument d) {
        FacultyDocumentResponse res = new FacultyDocumentResponse();
        res.setId(d.getId());
        res.setFacultyId(d.getFaculty().getId());
        res.setFacultyName(d.getFaculty().getName());
        res.setDocumentType(d.getDocumentType());
        res.setTitle(d.getTitle());
        res.setFileName(d.getFileName());
        res.setOriginalFileName(d.getOriginalFileName());
        res.setContentType(d.getContentType());
        res.setFileSize(d.getFileSize());
        res.setCreatedAt(d.getCreatedAt());
        return res;
    }
}
