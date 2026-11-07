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
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyMaterialResponse response = studyMaterialService.uploadMaterial(courseId, file, title, description, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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

