package com.lms.service;

import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.exception.BadRequestException;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final EnrollmentService enrollmentService;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             AssignmentSubmissionRepository submissionRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             GradeRepository gradeRepository,
                             EnrollmentService enrollmentService) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.enrollmentService = enrollmentService;
    }

    public List<AssignmentResponse> getAssignmentsByCourseId(Long courseId, String userEmail) {
        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(courseId, userEmail);
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AssignmentResponse getAssignmentById(Long id, String userEmail) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(assignment.getCourse().getId(), userEmail);
        return mapToResponse(assignment);
    }

    @Transactional
    public AssignmentResponse createAssignment(Long courseId, AssignmentRequest request, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can create assignments for this course");
        }

        if (request.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Assignment deadline must be in the future");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setCourse(course);
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxMarks(request.getMaxMarks());

        Assignment saved = assignmentRepository.save(assignment);
        return mapToResponse(saved);
    }

    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest request, String teacherEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !assignment.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can edit this assignment");
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxMarks(request.getMaxMarks());

        Assignment updated = assignmentRepository.save(assignment);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId, String teacherEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !assignment.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can delete this assignment");
        }

        // Clean dependent submissions
        submissionRepository.deleteAll(submissionRepository.findByAssignmentId(assignmentId));
        assignmentRepository.delete(assignment);
    }

    @Transactional
    public AssignmentSubmissionResponse submitAssignment(Long assignmentId, AssignmentSubmissionRequest request, String studentEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + studentEmail));

        if (student.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can submit assignments");
        }

        if (!enrollmentService.isStudentEnrolled(student.getId(), assignment.getCourse().getId())) {
            throw new ForbiddenException("You must be enrolled in the course to submit this assignment");
        }

        // Deadline validation: Reject submission if past deadline
        if (LocalDateTime.now().isAfter(assignment.getDeadline())) {
            throw new BadRequestException("Assignment deadline has passed. Submissions are no longer accepted.");
        }

        // Check if student has already submitted (update if allowed, or prevent duplicate)
        Optional<AssignmentSubmission> existingOpt = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId());
        AssignmentSubmission submission;
        if (existingOpt.isPresent()) {
            submission = existingOpt.get();
            submission.setAnswerText(request.getAnswerText());
        } else {
            submission = new AssignmentSubmission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
            submission.setAnswerText(request.getAnswerText());
            submission.setGraded(false);
        }

        AssignmentSubmission saved = submissionRepository.save(submission);
        return mapSubmissionToResponse(saved);
    }

    public List<AssignmentSubmissionResponse> getSubmissionsByAssignment(Long assignmentId, String teacherEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !assignment.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can view submissions for this assignment");
        }

        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapSubmissionToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentSubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, String teacherEmail) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        Assignment assignment = submission.getAssignment();
        if (user.getRole() != Role.ADMIN && !assignment.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can grade this submission");
        }

        if (request.getMarksObtained() > assignment.getMaxMarks()) {
            throw new BadRequestException("Marks obtained cannot exceed maximum marks (" + assignment.getMaxMarks() + ")");
        }

        submission.setMarksObtained(request.getMarksObtained());
        submission.setFeedback(request.getFeedback());
        submission.setGraded(true);
        submission.setGradedAt(LocalDateTime.now());

        AssignmentSubmission saved = submissionRepository.save(submission);

        // Update or create Grade entity for the student in this course
        Grade grade = new Grade();
        grade.setStudent(submission.getStudent());
        grade.setCourse(assignment.getCourse());
        grade.setMarks(request.getMarksObtained());
        grade.setTotalMarks(assignment.getMaxMarks());
        grade.setRemarks(assignment.getTitle() + ": " + (request.getFeedback() != null ? request.getFeedback() : "Graded"));
        gradeRepository.save(grade);

        return mapSubmissionToResponse(saved);
    }

    public AssignmentResponse mapToResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setCourseId(assignment.getCourse().getId());
        response.setDeadline(assignment.getDeadline());
        response.setMaxMarks(assignment.getMaxMarks());
        response.setCreatedAt(assignment.getCreatedAt());
        return response;
    }

    public AssignmentSubmissionResponse mapSubmissionToResponse(AssignmentSubmission submission) {
        AssignmentSubmissionResponse response = new AssignmentSubmissionResponse();
        response.setId(submission.getId());
        response.setAssignmentId(submission.getAssignment().getId());
        response.setAssignmentTitle(submission.getAssignment().getTitle());
        response.setStudentId(submission.getStudent().getId());
        response.setStudentName(submission.getStudent().getName());
        response.setAnswerText(submission.getAnswerText());
        response.setMarksObtained(submission.getMarksObtained());
        response.setMaxMarks(submission.getAssignment().getMaxMarks());
        response.setFeedback(submission.getFeedback());
        response.setGraded(submission.isGraded());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGradedAt(submission.getGradedAt());
        return response;
    }
}
