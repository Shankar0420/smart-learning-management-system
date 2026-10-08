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
