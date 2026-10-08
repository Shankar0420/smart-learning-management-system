package com.lms.controller;

import com.lms.dto.CourseResponse;
import com.lms.dto.TeacherDashboardResponse;
import com.lms.service.CourseService;
import com.lms.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public class TeacherController {

    private final DashboardService dashboardService;
    private final CourseService courseService;

    public TeacherController(DashboardService dashboardService, CourseService courseService) {
        this.dashboardService = dashboardService;
        this.courseService = courseService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<TeacherDashboardResponse> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.getTeacherDashboard(userDetails.getUsername()));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getMyCourses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(courseService.getCoursesByTeacher(userDetails.getUsername()));
    }
}
