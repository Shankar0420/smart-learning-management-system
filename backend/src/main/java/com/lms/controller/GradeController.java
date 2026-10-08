package com.lms.controller;

import com.lms.dto.GradeResponse;
import com.lms.service.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public ResponseEntity<List<GradeResponse>> getGrades(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gradeService.getGradesForUser(userDetails.getUsername()));
    }
}
