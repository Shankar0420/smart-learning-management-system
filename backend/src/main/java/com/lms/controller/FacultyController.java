package com.lms.controller;

import com.lms.dto.FacultyDocumentResponse;
import com.lms.dto.FacultyProfileResponse;
import com.lms.dto.MessageResponse;
import com.lms.entity.ApprovalStatus;
import com.lms.entity.FacultyDocument;
import com.lms.service.FacultyManagementService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FacultyController {

    private final FacultyManagementService facultyService;

    public FacultyController(FacultyManagementService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/admin/faculty")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FacultyProfileResponse>> getAllFaculty(
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(facultyService.getAllFaculty(department, search));
    }

    @GetMapping("/admin/faculty/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyProfileResponse> getFacultyById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getFacultyById(id));
    }

    @PutMapping("/admin/faculty/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyProfileResponse> updateFacultyProfile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(facultyService.updateFacultyProfile(id, payload, userDetails.getUsername()));
    }

    @PutMapping("/admin/faculty/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyProfileResponse> updateFacultyStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        Boolean active = payload.get("active") instanceof Boolean ? (Boolean) payload.get("active") : null;
        ApprovalStatus status = null;
        if (payload.containsKey("approvalStatus") && payload.get("approvalStatus") != null) {
            status = ApprovalStatus.valueOf(payload.get("approvalStatus").toString().toUpperCase());
        }
        return ResponseEntity.ok(facultyService.updateFacultyStatus(id, active, status, userDetails.getUsername()));
    }

    @GetMapping("/teacher/profile/stats")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getFacultyStats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(facultyService.getFacultyDashboardStats(userDetails.getUsername()));
    }

    @PostMapping(value = "/faculty/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<FacultyDocumentResponse> uploadFacultyDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("title") String title,
            @AuthenticationPrincipal UserDetails userDetails) {
        FacultyDocumentResponse res = facultyService.uploadFacultyDocument(file, documentType, title, userDetails.getUsername());
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @GetMapping("/faculty/documents")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<FacultyDocumentResponse>> getMyDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        // Find teacher ID
        com.lms.dto.FacultyProfileResponse profile = facultyService.getAllFaculty(null, userDetails.getUsername()).stream()
                .findFirst().orElse(null);
        if (profile == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(facultyService.getFacultyDocuments(profile.getId(), userDetails.getUsername()));
    }

    @GetMapping("/admin/faculty/{id}/documents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FacultyDocumentResponse>> getFacultyDocumentsForAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(facultyService.getFacultyDocuments(id, userDetails.getUsername()));
    }

    @GetMapping("/faculty/documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        FacultyDocument doc = facultyService.getDocumentEntity(id, userDetails.getUsername());
        Resource resource = facultyService.loadFacultyDocument(id, userDetails.getUsername());

        String originalFilename = doc.getOriginalFileName() != null ? doc.getOriginalFileName() : "document";
        String contentType = doc.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                .body(resource);
    }

    @DeleteMapping("/faculty/documents/{id}")
    public ResponseEntity<MessageResponse> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        facultyService.deleteFacultyDocument(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Faculty document deleted successfully"));
    }
}

