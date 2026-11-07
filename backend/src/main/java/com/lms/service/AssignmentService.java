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
    private final FileStorageService fileStorageService;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             AssignmentSubmissionRepository submissionRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             GradeRepository gradeRepository,
                             EnrollmentService enrollmentService,
                             FileStorageService fileStorageService) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.enrollmentService = enrollmentService;
        this.fileStorageService = fileStorageService;
    }

    public List<AssignmentResponse> getAllAssignments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        if (user.getRole() == Role.ADMIN) {
            return assignmentRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
        } else if (user.getRole() == Role.TEACHER) {
            List<Course> courses = courseRepository.findByTeacher(user);
            return courses.stream()
                    .flatMap(c -> assignmentRepository.findByCourseId(c.getId()).stream())
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else {
            List<CourseResponse> enrolled = enrollmentService.getEnrolledCourses(userEmail);
            return enrolled.stream()
                    .flatMap(c -> assignmentRepository.findByCourseId(c.getId()).stream())
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
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

        // Clean dependent submissions and files
        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        for (AssignmentSubmission sub : submissions) {
            if (sub.getSubmissionFileName() != null) {
                fileStorageService.deleteFile("submissions", sub.getSubmissionFileName());
            }
        }
        submissionRepository.deleteAll(submissions);

        if (assignment.getAttachmentFileName() != null) {
            fileStorageService.deleteFile("assignments", assignment.getAttachmentFileName());
        }
        assignmentRepository.delete(assignment);
    }

    @Transactional
    public AssignmentResponse createAssignmentWithAttachment(Long courseId,
                                                             String title,
                                                             String description,
                                                             LocalDateTime deadline,
                                                             int maxMarks,
                                                             org.springframework.web.multipart.MultipartFile attachment,
                                                             String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can create assignments for this course");
        }

        if (deadline.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Assignment deadline must be in the future");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setCourse(course);
        assignment.setDeadline(deadline);
        assignment.setMaxMarks(maxMarks);

        if (attachment != null && !attachment.isEmpty()) {
            String storedFileName = fileStorageService.storeAssignmentAttachment(attachment);
            assignment.setAttachmentFileName(storedFileName);
            assignment.setAttachmentOriginalName(attachment.getOriginalFilename());
            assignment.setAttachmentType(fileStorageService.getFileExtension(attachment.getOriginalFilename()).toUpperCase());
            assignment.setAttachmentSize(attachment.getSize());
            assignment.setAttachmentPath("assignments/" + storedFileName);
        }

        Assignment saved = assignmentRepository.save(assignment);
        return mapToResponse(saved);
    }

    @Transactional
    public AssignmentSubmissionResponse submitAssignmentWithFile(Long assignmentId,
                                                                 String answerText,
                                                                 org.springframework.web.multipart.MultipartFile file,
                                                                 String studentEmail) {
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

        if (LocalDateTime.now().isAfter(assignment.getDeadline())) {
            throw new BadRequestException("Assignment deadline has passed. Submissions are no longer accepted.");
        }

        if ((answerText == null || answerText.trim().isEmpty()) && (file == null || file.isEmpty())) {
            throw new BadRequestException("Please provide written answer text or upload a submission file");
        }

        Optional<AssignmentSubmission> existingOpt = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId());
        AssignmentSubmission submission;
        if (existingOpt.isPresent()) {
            submission = existingOpt.get();
            if (answerText != null) submission.setAnswerText(answerText);
        } else {
            submission = new AssignmentSubmission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
            submission.setAnswerText(answerText != null ? answerText : "");
            submission.setGraded(false);
        }

        if (file != null && !file.isEmpty()) {
            // Delete old file if replacing
            if (submission.getSubmissionFileName() != null) {
                fileStorageService.deleteFile("submissions", submission.getSubmissionFileName());
            }
            String storedFileName = fileStorageService.storeSubmissionFile(file);
            submission.setSubmissionFileName(storedFileName);
            submission.setSubmissionOriginalName(file.getOriginalFilename());
            submission.setSubmissionFileType(fileStorageService.getFileExtension(file.getOriginalFilename()).toUpperCase());
            submission.setSubmissionFileSize(file.getSize());
            submission.setSubmissionFilePath("submissions/" + storedFileName);
        }

        AssignmentSubmission saved = submissionRepository.save(submission);
        return mapSubmissionToResponse(saved);
    }

    public org.springframework.core.io.Resource loadAssignmentAttachment(Long assignmentId, String userEmail) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(assignment.getCourse().getId(), userEmail);

        if (assignment.getAttachmentFileName() == null) {
            throw new ResourceNotFoundException("This assignment does not contain an attached file");
        }

        return fileStorageService.loadFileAsResource("assignments", assignment.getAttachmentFileName());
    }

    public Assignment getAssignmentEntity(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
    }

    public AssignmentSubmission getSubmissionEntity(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
    }

    public org.springframework.core.io.Resource loadSubmissionFile(Long submissionId, String userEmail) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        boolean isStudentOwner = submission.getStudent().getId().equals(user.getId());
        boolean isCourseTeacher = submission.getAssignment().getCourse().getTeacher().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isStudentOwner && !isCourseTeacher && !isAdmin) {
            throw new ForbiddenException("You are not authorized to download this submission file");
        }

        if (submission.getSubmissionFileName() == null) {
            throw new ResourceNotFoundException("No file was uploaded with this submission");
        }

        return fileStorageService.loadFileAsResource("submissions", submission.getSubmissionFileName());
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

    public AssignmentSubmissionResponse getMySubmission(Long assignmentId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + studentEmail));
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId())
                .map(this::mapSubmissionToResponse)
                .orElse(null);
    }

    public AssignmentResponse mapToResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setCourseId(assignment.getCourse().getId());
        response.setDeadline(assignment.getDeadline());
        response.setMaxMarks(assignment.getMaxMarks());
        response.setAttachmentOriginalName(assignment.getAttachmentOriginalName());
        response.setAttachmentType(assignment.getAttachmentType());
        response.setAttachmentSize(assignment.getAttachmentSize());
        response.setHasAttachment(assignment.getAttachmentFileName() != null);
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
        response.setSubmissionOriginalName(submission.getSubmissionOriginalName());
        response.setSubmissionFileType(submission.getSubmissionFileType());
        response.setSubmissionFileSize(submission.getSubmissionFileSize());
        response.setHasSubmissionFile(submission.getSubmissionFileName() != null);
        response.setMarksObtained(submission.getMarksObtained());
        response.setMaxMarks(submission.getAssignment().getMaxMarks());
        response.setFeedback(submission.getFeedback());
        response.setGraded(submission.isGraded());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGradedAt(submission.getGradedAt());
        return response;
    }
}
