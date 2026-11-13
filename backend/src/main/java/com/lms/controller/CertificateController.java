package com.lms.controller;

import com.lms.dto.CertificateResponse;
import com.lms.service.CertificateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/my-certificates")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(certificateService.getStudentCertificates(userDetails.getUsername()));
    }

    @PostMapping("/courses/{courseId}/generate")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CertificateResponse> generateCertificate(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CertificateResponse response = certificateService.generateCertificate(courseId, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/verify/{code}")
    public ResponseEntity<CertificateResponse> verifyCertificate(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verifyCertificate(code));
    }
}

