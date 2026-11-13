package com.lms.controller;

import com.lms.dto.CalendarEventRequest;
import com.lms.dto.CalendarEventResponse;
import com.lms.dto.MessageResponse;
import com.lms.service.AcademicCalendarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class AcademicCalendarController {

    private final AcademicCalendarService calendarService;

    public AcademicCalendarController(AcademicCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEventResponse>> getEvents(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(calendarService.getEventsForUser(userDetails.getUsername()));
    }

    @PostMapping("/events")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<CalendarEventResponse> createEvent(
            @Valid @RequestBody CalendarEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CalendarEventResponse response = calendarService.createEvent(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        calendarService.deleteEvent(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Calendar event deleted successfully"));
    }
}

