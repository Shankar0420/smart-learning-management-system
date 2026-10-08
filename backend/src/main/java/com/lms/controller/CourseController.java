package com.lms.controller;

import com.lms.dto.*;
import com.lms.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ModuleService moduleService;
    private final QuizService quizService;
    private final AssignmentService assignmentService;
    private final LessonProgressService lessonProgressService;

    public CourseController(CourseService courseService,
                            EnrollmentService enrollmentService,
                            ModuleService moduleService,
                            QuizService quizService,
                            AssignmentService assignmentService,
                            LessonProgressService lessonProgressService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.moduleService = moduleService;
        this.quizService = quizService;
        this.assignmentService = assignmentService;
        this.lessonProgressService = lessonProgressService;
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCourses() {
        return ResponseEntity.ok(courseService.getAllActiveCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        CourseResponse response = courseService.createCourse(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id,
                                                       @Valid @RequestBody CourseRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        CourseResponse response = courseService.updateCourse(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        courseService.deleteCourse(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Course deleted successfully"));
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        EnrollmentResponse response = enrollmentService.enroll(id, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/modules")
    public ResponseEntity<List<ModuleResponse>> getModules(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(moduleService.getModulesByCourseId(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/modules")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> createModule(@PathVariable Long id,
                                                       @Valid @RequestBody ModuleRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        ModuleResponse response = moduleService.createModule(id, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/quizzes")
    public ResponseEntity<List<QuizResponse>> getQuizzes(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.getQuizzesByCourseId(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/quizzes")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> createQuiz(@PathVariable Long id,
                                                   @Valid @RequestBody QuizRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        QuizResponse response = quizService.createQuiz(id, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignments(@PathVariable Long id,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourseId(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> createAssignment(@PathVariable Long id,
                                                               @Valid @RequestBody AssignmentRequest request,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        AssignmentResponse response = assignmentService.createAssignment(id, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(@PathVariable Long id,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(lessonProgressService.getCourseProgress(id, userDetails.getUsername()));
    }
}
