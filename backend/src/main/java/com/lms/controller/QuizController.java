package com.lms.controller;

import com.lms.dto.*;
import com.lms.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.getQuizById(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> updateQuiz(@PathVariable Long id,
                                                   @Valid @RequestBody QuizRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        QuizResponse response = quizService.updateQuiz(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteQuiz(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        quizService.deleteQuiz(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Quiz deleted successfully"));
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuizQuestionResponse> addQuestion(@PathVariable Long id,
                                                            @Valid @RequestBody QuizQuestionRequest request,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        QuizQuestionResponse response = quizService.addQuestion(id, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizAttemptResponse> submitQuiz(@PathVariable Long id,
                                                          @Valid @RequestBody QuizSubmitRequest request,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        QuizAttemptResponse response = quizService.submitQuiz(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/attempts")
    public ResponseEntity<List<QuizAttemptResponse>> getAttempts(@PathVariable Long id,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.getQuizAttempts(id, userDetails.getUsername()));
    }
}
