package com.lms.service;

import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.exception.BadRequestException;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;

    public QuizService(QuizRepository quizRepository,
                       QuizQuestionRepository quizQuestionRepository,
                       QuizOptionRepository quizOptionRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       CourseRepository courseRepository,
                       UserRepository userRepository,
                       EnrollmentService enrollmentService) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizOptionRepository = quizOptionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
    }

    public List<QuizResponse> getQuizzesByCourseId(Long courseId, String userEmail) {
        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(courseId, userEmail);
        return quizRepository.findByCourseId(courseId).stream()
                .map(q -> mapToResponse(q, false))
                .collect(Collectors.toList());
    }

    public QuizResponse getQuizById(Long quizId, String userEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(quiz.getCourse().getId(), userEmail);

        // Hide correct option answers if current user is a student
        boolean showCorrect = user.getRole() == Role.TEACHER || user.getRole() == Role.ADMIN;
        return mapToResponse(quiz, showCorrect);
    }

    @Transactional
    public QuizResponse createQuiz(Long courseId, QuizRequest request, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can create quizzes for this course");
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setCourse(course);
        quiz.setTotalMarks(request.getTotalMarks());
        quiz.setActive(true);

        Quiz saved = quizRepository.save(quiz);
        return mapToResponse(saved, true);
    }

    @Transactional
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, String teacherEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !quiz.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can update this quiz");
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        if (request.getTotalMarks() > 0) {
            quiz.setTotalMarks(request.getTotalMarks());
        }

        Quiz updated = quizRepository.save(quiz);
        return mapToResponse(updated, true);
    }

    @Transactional
    public void deleteQuiz(Long quizId, String teacherEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !quiz.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can delete this quiz");
        }

        // Clean attempts
        quizAttemptRepository.deleteAll(quizAttemptRepository.findAll().stream()
                .filter(qa -> qa.getQuiz().getId().equals(quizId))
                .collect(Collectors.toList()));

        // Clean questions and options
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        for (QuizQuestion q : questions) {
            quizOptionRepository.deleteAll(quizOptionRepository.findByQuestionId(q.getId()));
            quizQuestionRepository.delete(q);
        }

        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizQuestionResponse addQuestion(Long quizId, QuizQuestionRequest request, String teacherEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + teacherEmail));

        if (user.getRole() != Role.ADMIN && !quiz.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the course teacher can add questions to this quiz");
        }

        if (request.getOptions() == null || request.getOptions().size() < 2) {
            throw new BadRequestException("A question must have at least two options");
        }

        boolean hasCorrect = request.getOptions().stream().anyMatch(QuizOptionRequest::isCorrect);
        if (!hasCorrect) {
            throw new BadRequestException("At least one option must be marked as correct");
        }

        QuizQuestion question = new QuizQuestion();
        question.setQuestionText(request.getQuestionText());
        question.setMarks(request.getMarks());
        question.setQuiz(quiz);

        QuizQuestion savedQuestion = quizQuestionRepository.save(question);

        List<QuizOptionResponse> optionResponses = new ArrayList<>();
        for (QuizOptionRequest optReq : request.getOptions()) {
            QuizOption option = new QuizOption();
            option.setOptionText(optReq.getOptionText());
            option.setCorrect(optReq.isCorrect());
            option.setQuestion(savedQuestion);
            QuizOption savedOption = quizOptionRepository.save(option);
            optionResponses.add(new QuizOptionResponse(savedOption.getId(), savedOption.getOptionText(), savedOption.isCorrect()));
        }

        // Automatically update total marks for quiz
        int totalMarks = quizQuestionRepository.findByQuizId(quizId).stream()
                .mapToInt(QuizQuestion::getMarks)
                .sum();
        quiz.setTotalMarks(totalMarks);
        quizRepository.save(quiz);

        return new QuizQuestionResponse(savedQuestion.getId(), savedQuestion.getQuestionText(), savedQuestion.getMarks(), optionResponses);
    }

    @Transactional
    public QuizAttemptResponse submitQuiz(Long quizId, QuizSubmitRequest request, String studentEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + studentEmail));

        if (student.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can submit quiz attempts");
        }

        if (!enrollmentService.isStudentEnrolled(student.getId(), quiz.getCourse().getId())) {
            throw new ForbiddenException("You must be enrolled in the course to submit this quiz");
        }

        // Server-side Score Calculation: NEVER trust frontend score
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        int calculatedScore = 0;
        int totalMarks = quiz.getTotalMarks();

        if (totalMarks == 0) {
            totalMarks = questions.stream().mapToInt(QuizQuestion::getMarks).sum();
        }

        // Map question ID to submitted selected option ID
        Map<Long, Long> submittedAnswers = new HashMap<>();
        if (request.getAnswers() != null) {
            for (QuizSubmitRequest.SelectedAnswer ans : request.getAnswers()) {
                submittedAnswers.put(ans.getQuestionId(), ans.getSelectedOptionId());
            }
        }

        // Compare each question with MariaDB stored options
        for (QuizQuestion question : questions) {
            Long selectedOptionId = submittedAnswers.get(question.getId());
            if (selectedOptionId != null) {
                Optional<QuizOption> optionOpt = quizOptionRepository.findById(selectedOptionId);
                if (optionOpt.isPresent()) {
                    QuizOption option = optionOpt.get();
                    // Verify option belongs to this question and is correct
                    if (option.getQuestion().getId().equals(question.getId()) && option.isCorrect()) {
                        calculatedScore += question.getMarks();
                    }
                }
            }
        }

        // Save QuizAttempt in MariaDB
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setScore(calculatedScore);
        attempt.setTotalMarks(totalMarks);

        QuizAttempt saved = quizAttemptRepository.save(attempt);

        double percentage = totalMarks > 0 ? (double) calculatedScore / totalMarks * 100.0 : 0.0;

        return new QuizAttemptResponse(
                saved.getId(),
                quiz.getId(),
                quiz.getTitle(),
                student.getId(),
                student.getName(),
                calculatedScore,
                totalMarks,
                Math.round(percentage * 10.0) / 10.0,
                saved.getAttemptedAt()
        );
    }

    public List<QuizAttemptResponse> getQuizAttempts(Long quizId, String userEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<QuizAttempt> attempts;
        if (user.getRole() == Role.STUDENT) {
            attempts = quizAttemptRepository.findByQuizIdAndStudentId(quizId, user.getId());
        } else {
            // Teacher or admin can view all attempts for this quiz
            attempts = quizAttemptRepository.findAll().stream()
                    .filter(a -> a.getQuiz().getId().equals(quizId))
                    .collect(Collectors.toList());
        }

        return attempts.stream()
                .map(this::mapAttemptToResponse)
                .collect(Collectors.toList());
    }

    public QuizResponse mapToResponse(Quiz quiz, boolean showCorrect) {
        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setCourseId(quiz.getCourse().getId());
        response.setTotalMarks(quiz.getTotalMarks());
        response.setActive(quiz.isActive());
        response.setCreatedAt(quiz.getCreatedAt());

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quiz.getId());
        List<QuizQuestionResponse> questionResponses = questions.stream().map(q -> {
            List<QuizOption> options = quizOptionRepository.findByQuestionId(q.getId());
            List<QuizOptionResponse> optionResponses = options.stream().map(opt -> {
                // If showCorrect is false (student taking quiz), do NOT expose isCorrect
                Boolean isCorrect = showCorrect ? opt.isCorrect() : null;
                return new QuizOptionResponse(opt.getId(), opt.getOptionText(), isCorrect);
            }).collect(Collectors.toList());

            return new QuizQuestionResponse(q.getId(), q.getQuestionText(), q.getMarks(), optionResponses);
        }).collect(Collectors.toList());

        response.setQuestions(questionResponses);
        return response;
    }

    public QuizAttemptResponse mapAttemptToResponse(QuizAttempt attempt) {
        double percentage = attempt.getTotalMarks() > 0
                ? (double) attempt.getScore() / attempt.getTotalMarks() * 100.0
                : 0.0;
        return new QuizAttemptResponse(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getQuiz().getTitle(),
                attempt.getStudent().getId(),
                attempt.getStudent().getName(),
                attempt.getScore(),
                attempt.getTotalMarks(),
                Math.round(percentage * 10.0) / 10.0,
                attempt.getAttemptedAt()
        );
    }
}
