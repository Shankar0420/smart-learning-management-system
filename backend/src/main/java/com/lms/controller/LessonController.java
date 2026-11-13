package com.lms.controller;

import com.lms.dto.LessonRequest;
import com.lms.dto.LessonResponse;
import com.lms.dto.MessageResponse;
import com.lms.service.LessonProgressService;
import com.lms.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;

    public LessonController(LessonService lessonService, LessonProgressService lessonProgressService) {
        this.lessonService = lessonService;
        this.lessonProgressService = lessonProgressService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(lessonService.getLessonById(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable Long id,
                                                       @Valid @RequestBody LessonRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        LessonResponse response = lessonService.updateLesson(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteLesson(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        lessonService.deleteLesson(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Lesson deleted successfully"));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MessageResponse> completeLesson(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        MessageResponse response = lessonProgressService.completeLesson(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
