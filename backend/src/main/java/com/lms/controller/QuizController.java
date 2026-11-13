package com.lms.controller;

import com.lms.dto.*;
import com.lms.service.AiAssistantService;
import com.lms.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final AiAssistantService aiAssistantService;

    public QuizController(QuizService quizService, AiAssistantService aiAssistantService) {
        this.quizService = quizService;
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/generate-ai")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateQuizWithAi(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal UserDetails userDetails) {
        String topic = (String) req.get("topic");
        String subject = (String) req.get("subject");
        String difficulty = (String) req.get("difficulty");
        String questionType = (String) req.get("questionType");
        int count = req.containsKey("count") && req.get("count") instanceof Number ? ((Number) req.get("count")).intValue() : 5;

        List<Map<String, Object>> questions = aiAssistantService.generateQuizQuestions(topic, subject, difficulty, count, questionType);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "topic", topic != null ? topic : "General",
                "questions", questions,
                "message", "AI generated " + questions.size() + " review questions successfully"
        ));
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

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> togglePublish(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.togglePublishQuiz(id, userDetails.getUsername()));
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

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteQuestion(@PathVariable Long questionId,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        quizService.deleteQuestion(questionId, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Question deleted successfully"));
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
