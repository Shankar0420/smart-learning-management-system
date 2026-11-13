package com.lms.controller;

import com.lms.dto.LessonRequest;
import com.lms.dto.LessonResponse;
import com.lms.dto.MessageResponse;
import com.lms.dto.ModuleRequest;
import com.lms.dto.ModuleResponse;
import com.lms.service.LessonService;
import com.lms.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;
    private final LessonService lessonService;

    public ModuleController(ModuleService moduleService, LessonService lessonService) {
        this.moduleService = moduleService;
        this.lessonService = lessonService;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> updateModule(@PathVariable Long id,
                                                       @Valid @RequestBody ModuleRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        ModuleResponse response = moduleService.updateModule(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteModule(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        moduleService.deleteModule(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Module deleted successfully"));
    }

    @GetMapping("/{id}/lessons")
    public ResponseEntity<List<LessonResponse>> getLessons(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(lessonService.getLessonsByModuleId(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/lessons")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> createLesson(@PathVariable Long id,
                                                       @Valid @RequestBody LessonRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        LessonResponse response = lessonService.createLesson(id, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
