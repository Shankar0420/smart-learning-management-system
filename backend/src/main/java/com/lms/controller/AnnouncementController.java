package com.lms.controller;

import com.lms.dto.AnnouncementRequest;
import com.lms.dto.AnnouncementResponse;
import com.lms.dto.MessageResponse;
import com.lms.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AnnouncementResponse res = announcementService.createAnnouncement(request, userDetails.getUsername());
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getMyAnnouncements(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(announcementService.getAnnouncementsForUser(userDetails.getUsername()));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AnnouncementResponse>> getCourseAnnouncements(@PathVariable Long courseId) {
        return ResponseEntity.ok(announcementService.getCourseAnnouncements(courseId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        announcementService.deleteAnnouncement(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Announcement deleted successfully"));
    }
}

