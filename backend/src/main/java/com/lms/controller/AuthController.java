package com.lms.controller;

import com.lms.dto.AuthResponse;
import com.lms.dto.LoginRequest;
import com.lms.dto.MessageResponse;
import com.lms.dto.RegisterRequest;
import com.lms.dto.TeacherRegisterRequest;
import com.lms.dto.AdminRegisterRequest;
import com.lms.dto.UserResponse;
import com.lms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final com.lms.service.PasswordResetService passwordResetService;

    public AuthController(AuthService authService, com.lms.service.PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register-teacher")
    public ResponseEntity<UserResponse> registerTeacher(@Valid @RequestBody TeacherRegisterRequest request) {
        UserResponse response = authService.registerTeacher(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<UserResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        UserResponse response = authService.registerAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            authService.logout(userDetails.getUsername());
        }
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody com.lms.dto.ForgotPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.initiatePasswordReset(request.getEmail()));
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<java.util.Map<String, Boolean>> verifyResetToken(@RequestParam("token") String token) {
        boolean valid = passwordResetService.verifyToken(token);
        return ResponseEntity.ok(java.util.Collections.singletonMap("valid", valid));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody com.lms.dto.ResetPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }
}
