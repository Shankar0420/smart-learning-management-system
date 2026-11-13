package com.lms.controller;

import com.lms.dto.MessageResponse;
import com.lms.dto.StudyMaterialResponse;
import com.lms.entity.StudyMaterial;
import com.lms.service.StudyMaterialService;
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
public class StudyMaterialController {

    private final StudyMaterialService studyMaterialService;

    public StudyMaterialController(StudyMaterialService studyMaterialService) {
        this.studyMaterialService = studyMaterialService;
    }

    @PostMapping(value = "/courses/{courseId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<StudyMaterialResponse> uploadMaterial(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false, defaultValue = "LECTURE_NOTES") String category,
            @RequestParam(value = "semester", required = false) String semester,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "subject", required = false) String subject,
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyMaterialResponse response = studyMaterialService.uploadMaterial(
                courseId, file, title, description, category, semester, academicYear, department, subject, userDetails.getUsername()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/materials/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<StudyMaterialResponse> updateMaterial(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        String title = (String) payload.get("title");
        String description = (String) payload.get("description");
        String category = (String) payload.get("category");
        String semester = (String) payload.get("semester");
        Boolean published = payload.get("published") instanceof Boolean ? (Boolean) payload.get("published") : null;

        StudyMaterialResponse response = studyMaterialService.updateMaterial(
                id, title, description, category, semester, published, userDetails.getUsername()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/materials/{id}/replace-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<StudyMaterialResponse> replaceMaterialFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyMaterialResponse response = studyMaterialService.replaceMaterialFile(id, file, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/materials/{id}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean bookmarked = studyMaterialService.toggleBookmark(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked, "message", bookmarked ? "Material bookmarked" : "Bookmark removed"));
    }

    @GetMapping("/materials/bookmarks")
    public ResponseEntity<List<StudyMaterialResponse>> getBookmarkedMaterials(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyMaterialService.getBookmarkedMaterials(userDetails.getUsername()));
    }

    @GetMapping("/materials/search")
    public ResponseEntity<List<StudyMaterialResponse>> searchMaterials(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "semester", required = false) String semester,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyMaterialService.searchMaterials(userDetails.getUsername(), keyword, category, semester, courseId));
    }

    @GetMapping("/courses/{courseId}/materials")
    public ResponseEntity<List<StudyMaterialResponse>> getMaterialsByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyMaterialService.getMaterialsByCourse(courseId, userDetails.getUsername()));
    }

    @GetMapping("/teacher/materials")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<StudyMaterialResponse>> getTeacherMaterials(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyMaterialService.getMaterialsByTeacher(userDetails.getUsername()));
    }

    @GetMapping("/materials/{id}/preview")
    public ResponseEntity<Resource> previewMaterial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyMaterial material = studyMaterialService.getMaterialEntity(id, userDetails.getUsername());
        Resource resource = studyMaterialService.previewMaterialFile(id, userDetails.getUsername());

        String originalFilename = material.getOriginalFileName() != null ? material.getOriginalFileName() : "material";
        String contentType = material.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + originalFilename + "\"")
                .body(resource);
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Resource> downloadMaterial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyMaterial material = studyMaterialService.getMaterialEntity(id, userDetails.getUsername());
        Resource resource = studyMaterialService.loadMaterialFile(id, userDetails.getUsername());

        String originalFilename = material.getOriginalFileName() != null ? material.getOriginalFileName() : "material";
        String contentType = material.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                .body(resource);
    }

    @DeleteMapping("/materials/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteMaterial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        studyMaterialService.deleteMaterial(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Study material deleted successfully"));
    }
}
