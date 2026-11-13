package com.lms.controller;

import com.lms.dto.AttendanceSessionRequest;
import com.lms.dto.AttendanceSessionResponse;
import com.lms.dto.StudentAttendanceSummaryResponse;
import com.lms.entity.AttendanceStatus;
import com.lms.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/courses/{courseId}/sessions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceSessionResponse> createSession(
            @PathVariable Long courseId,
            @Valid @RequestBody AttendanceSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AttendanceSessionResponse res = attendanceService.createSession(courseId, request, userDetails.getUsername());
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @GetMapping("/courses/{courseId}/sessions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<AttendanceSessionResponse>> getCourseSessions(@PathVariable Long courseId) {
        return ResponseEntity.ok(attendanceService.getCourseSessions(courseId));
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceSessionResponse> getSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.getSessionById(sessionId));
    }

    @PutMapping("/sessions/{sessionId}/records/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceSessionResponse> updateStudentRecord(
            @PathVariable Long sessionId,
            @PathVariable Long studentId,
            @RequestParam("status") AttendanceStatus status,
            @RequestParam(value = "remarks", required = false) String remarks,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.updateRecord(sessionId, studentId, status, remarks, userDetails.getUsername()));
    }

    @GetMapping("/my-summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentAttendanceSummaryResponse> getMyAttendanceSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.getStudentAttendanceSummary(userDetails.getUsername()));
    }
}

