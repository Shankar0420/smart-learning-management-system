package com.lms.controller;

import com.lms.dto.AiChatRequest;
import com.lms.dto.AiChatResponse;
import com.lms.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("isAuthenticated()")
public class AiController {

    private final AiAssistantService aiAssistantService;

    public AiController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chatWithBuddy(
            @Valid @RequestBody AiChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AiChatResponse response = aiAssistantService.processAcademicQuery(request, userDetails != null ? userDetails.getUsername() : "anonymous");
        return ResponseEntity.ok(response);
    }
}

