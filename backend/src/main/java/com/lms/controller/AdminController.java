package com.lms.controller;

import com.lms.dto.AdminDashboardResponse;
import com.lms.dto.CourseResponse;
import com.lms.dto.UserResponse;
import com.lms.entity.Role;
import com.lms.service.CourseService;
import com.lms.service.DashboardService;
import com.lms.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final CourseService courseService;

    public AdminController(DashboardService dashboardService,
                           UserService userService,
                           CourseService courseService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.courseService = courseService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/students")
    public ResponseEntity<List<UserResponse>> getStudents() {
        return ResponseEntity.ok(userService.getUsersByRole(Role.STUDENT));
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<UserResponse>> getTeachers() {
        return ResponseEntity.ok(userService.getUsersByRole(Role.TEACHER));
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setActiveStatus(id, true));
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setActiveStatus(id, false));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
}
