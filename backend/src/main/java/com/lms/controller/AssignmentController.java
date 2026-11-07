package com.lms.controller;

import com.lms.dto.*;
import com.lms.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(assignmentService.getAllAssignments(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> updateAssignment(@PathVariable Long id,
                                                               @Valid @RequestBody AssignmentRequest request,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        AssignmentResponse response = assignmentService.updateAssignment(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAssignment(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        assignmentService.deleteAssignment(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Assignment deleted successfully"));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssignmentSubmissionResponse> submitAssignment(@PathVariable Long id,
                                                                         @Valid @RequestBody AssignmentSubmissionRequest request,
                                                                         @AuthenticationPrincipal UserDetails userDetails) {
        AssignmentSubmissionResponse response = assignmentService.submitAssignment(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/submit-file", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssignmentSubmissionResponse> submitAssignmentWithFile(
            @PathVariable Long id,
            @RequestParam(value = "answerText", required = false) String answerText,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        AssignmentSubmissionResponse response = assignmentService.submitAssignmentWithFile(id, answerText, file, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/my-submission")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AssignmentSubmissionResponse> getMySubmission(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        AssignmentSubmissionResponse response = assignmentService.getMySubmission(id, userDetails.getUsername());
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attachment")
    public ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        com.lms.entity.Assignment assignment = assignmentService.getAssignmentEntity(id);
        org.springframework.core.io.Resource resource = assignmentService.loadAssignmentAttachment(id, userDetails.getUsername());
        String filename = assignment.getAttachmentOriginalName() != null ? assignment.getAttachmentOriginalName() : "attachment";
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/submissions/{id}/file")
    public ResponseEntity<org.springframework.core.io.Resource> downloadSubmissionFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        com.lms.entity.AssignmentSubmission sub = assignmentService.getSubmissionEntity(id);
        org.springframework.core.io.Resource resource = assignmentService.loadSubmissionFile(id, userDetails.getUsername());
        String filename = sub.getSubmissionOriginalName() != null ? sub.getSubmissionOriginalName() : "submission";
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<AssignmentSubmissionResponse>> getSubmissions(@PathVariable Long id,
                                                                             @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(assignmentService.getSubmissionsByAssignment(id, userDetails.getUsername()));
    }

    @PostMapping("/submissions/{id}/grade")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AssignmentSubmissionResponse> gradeSubmission(@PathVariable Long id,
                                                                        @Valid @RequestBody GradeSubmissionRequest request,
                                                                        @AuthenticationPrincipal UserDetails userDetails) {
        AssignmentSubmissionResponse response = assignmentService.gradeSubmission(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
