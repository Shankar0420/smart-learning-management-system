package com.lms.controller;

import com.lms.dto.CourseResponse;
import com.lms.dto.StudentDashboardResponse;
import com.lms.service.DashboardService;
import com.lms.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final DashboardService dashboardService;
    private final EnrollmentService enrollmentService;

    public StudentController(DashboardService dashboardService, EnrollmentService enrollmentService) {
        this.dashboardService = dashboardService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(userDetails.getUsername()));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getEnrolledCourses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(enrollmentService.getEnrolledCourses(userDetails.getUsername()));
    }
}
