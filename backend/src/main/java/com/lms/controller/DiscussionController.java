package com.lms.controller;

import com.lms.dto.DiscussionReplyRequest;
import com.lms.dto.DiscussionReplyResponse;
import com.lms.dto.DiscussionThreadRequest;
import com.lms.dto.DiscussionThreadResponse;
import com.lms.dto.MessageResponse;
import com.lms.service.DiscussionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    private final DiscussionService discussionService;

    public DiscussionController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<DiscussionThreadResponse>> getCourseThreads(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(discussionService.getCourseThreads(courseId, userDetails.getUsername()));
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<DiscussionThreadResponse> getThreadDetails(
            @PathVariable Long threadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(discussionService.getThreadDetails(threadId, userDetails.getUsername()));
    }

    @PostMapping("/courses/{courseId}")
    public ResponseEntity<DiscussionThreadResponse> createThread(
            @PathVariable Long courseId,
            @Valid @RequestBody DiscussionThreadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        DiscussionThreadResponse response = discussionService.createThread(courseId, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/threads/{threadId}/replies")
    public ResponseEntity<DiscussionReplyResponse> addReply(
            @PathVariable Long threadId,
            @Valid @RequestBody DiscussionReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        DiscussionReplyResponse response = discussionService.addReply(threadId, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/threads/{threadId}/pin")
    public ResponseEntity<MessageResponse> togglePin(
            @PathVariable Long threadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        discussionService.togglePin(threadId, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Thread pin status updated"));
    }

    @PutMapping("/replies/{replyId}/accept")
    public ResponseEntity<MessageResponse> markAcceptedSolution(
            @PathVariable Long replyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        discussionService.markAcceptedSolution(replyId, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Reply acceptance status updated"));
    }

    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<MessageResponse> deleteThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        discussionService.deleteThread(threadId, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Discussion thread deleted successfully"));
    }
}

