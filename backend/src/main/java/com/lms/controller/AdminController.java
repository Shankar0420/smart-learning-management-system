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

import com.lms.dto.AdminRegisterRequest;
import com.lms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final CourseService courseService;
    private final AuthService authService;

    public AdminController(DashboardService dashboardService,
                           UserService userService,
                           CourseService courseService,
                           AuthService authService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.courseService = courseService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/students")
    public ResponseEntity<List<UserResponse>> getStudents() {
        return ResponseEntity.ok(userService.getUsersByRole(Role.STUDENT));
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<UserResponse>> getTeachers() {
        return ResponseEntity.ok(userService.getUsersByRole(Role.TEACHER));
    }

    @GetMapping("/teachers/pending")
    public ResponseEntity<List<UserResponse>> getPendingTeachers() {
        return ResponseEntity.ok(userService.getPendingTeachers());
    }

    @PutMapping("/teachers/{id}/approve")
    public ResponseEntity<UserResponse> approveTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(userService.approveTeacher(id));
    }

    @PutMapping("/teachers/{id}/reject")
    public ResponseEntity<UserResponse> rejectTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(userService.rejectTeacher(id));
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setActiveStatus(id, true));
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setActiveStatus(id, false));
    }

    @GetMapping("/administrators")
    public ResponseEntity<List<UserResponse>> getAdministrators() {
        return ResponseEntity.ok(userService.getUsersByRole(Role.ADMIN));
    }

    @PostMapping("/administrators")
    public ResponseEntity<UserResponse> createAdministrator(@Valid @RequestBody AdminRegisterRequest request) {
        UserResponse response = authService.createAdminByAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

}
