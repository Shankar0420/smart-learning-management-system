package com.lms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.*;
import com.lms.repository.QuizOptionRepository;
import com.lms.repository.QuizQuestionRepository;
import com.lms.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizAndAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizOptionRepository quizOptionRepository;

    private String teacherToken;
    private String student1Token;

    @BeforeEach
    void setUp() {
        teacherToken = "Bearer " + jwtUtils.generateToken("teacher@lms.com", "TEACHER");
        student1Token = "Bearer " + jwtUtils.generateToken("student1@lms.com", "STUDENT");
    }

    @Test
    void testQuizWorkflowAndServerSideScoreCalculation() throws Exception {
        // 1. Teacher creates Quiz for Course 1
        QuizRequest quizReq = new QuizRequest();
        quizReq.setTitle("Java Concurrency & Threads");
        quizReq.setDescription("Testing knowledge of multithreading in Java.");
        quizReq.setTotalMarks(20);

        String quizRes = mockMvc.perform(post("/api/courses/1/quizzes")
                        .header("Authorization", teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Java Concurrency & Threads"))
                .andReturn().getResponse().getContentAsString();

        Long quizId = objectMapper.readTree(quizRes).get("id").asLong();

        // 2. Teacher adds Question with options
        QuizQuestionRequest qReq = new QuizQuestionRequest();
        qReq.setQuestionText("Which interface must be implemented to create a thread using lambda?");
        qReq.setMarks(10);
        qReq.setOptions(List.of(
                new QuizOptionRequest("Runnable", true),
                new QuizOptionRequest("Callable", false),
                new QuizOptionRequest("Threadable", false),
                new QuizOptionRequest("Executable", false)
        ));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .header("Authorization", teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.marks").value(10));

        // 3. Student opens quiz: verify 'correct' answers are NOT exposed
        mockMvc.perform(get("/api/quizzes/" + quizId)
                        .header("Authorization", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].options[0].correct").doesNotExist());

        // Find the created options from database to construct submitted answers
        var questions = quizQuestionRepository.findByQuizId(quizId);
        Long questionId = questions.get(0).getId();
        var options = quizOptionRepository.findByQuestionId(questionId);
        Long correctOptionId = options.stream().filter(o -> o.isCorrect()).findFirst().get().getId();

        // 4. Student submits quiz: backend calculates score on server!
        QuizSubmitRequest submitReq = new QuizSubmitRequest(List.of(
                new QuizSubmitRequest.SelectedAnswer(questionId, correctOptionId)
        ));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/submit")
                        .header("Authorization", student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(10))
                .andExpect(jsonPath("$.totalMarks").value(10))
                .andExpect(jsonPath("$.percentage").value(100.0));

        // 5. Student views previous attempt
        mockMvc.perform(get("/api/quizzes/" + quizId + "/attempts")
                        .header("Authorization", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].score").value(10));
    }

    @Test
    void testAssignmentWorkflowAndDeadlineValidation() throws Exception {
        // 1. Teacher creates Assignment
        AssignmentRequest assignReq = new AssignmentRequest();
        assignReq.setTitle("Spring Data JPA Custom Queries");
        assignReq.setDescription("Implement repository methods using @Query and projections.");
        assignReq.setDeadline(LocalDateTime.now().plusDays(7));
        assignReq.setMaxMarks(50);

        String assignRes = mockMvc.perform(post("/api/courses/1/assignments")
                        .header("Authorization", teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Spring Data JPA Custom Queries"))
                .andReturn().getResponse().getContentAsString();

        Long assignmentId = objectMapper.readTree(assignRes).get("id").asLong();

        // 2. Student submits assignment before deadline
        AssignmentSubmissionRequest submitReq = new AssignmentSubmissionRequest(
                "Implemented findByTitleContainingIgnoreCase and custom DTO projection interfaces."
        );

        String submitRes = mockMvc.perform(post("/api/assignments/" + assignmentId + "/submit")
                        .header("Authorization", student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.graded").value(false))
                .andReturn().getResponse().getContentAsString();

        Long submissionId = objectMapper.readTree(submitRes).get("id").asLong();

        // 3. Teacher views submissions
        mockMvc.perform(get("/api/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // 4. Teacher grades submission
        GradeSubmissionRequest gradeReq = new GradeSubmissionRequest(48, "Excellent query optimizations!");

        mockMvc.perform(post("/api/assignments/submissions/" + submissionId + "/grade")
                        .header("Authorization", teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.graded").value(true))
                .andExpect(jsonPath("$.marksObtained").value(48))
                .andExpect(jsonPath("$.feedback").value("Excellent query optimizations!"));

        // 5. Student views grades via /api/grades
        mockMvc.perform(get("/api/grades")
                        .header("Authorization", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].marks").value(48.0));
    }

    @Test
    void testAssignmentExpiredDeadlineReturns400() throws Exception {
        // Teacher creates Assignment with past deadline (via BadRequest validation)
        AssignmentRequest assignReq = new AssignmentRequest();
        assignReq.setTitle("Expired Assignment");
        assignReq.setDescription("Should not be creatable with past deadline.");
        assignReq.setDeadline(LocalDateTime.now().minusDays(1));
        assignReq.setMaxMarks(50);

        mockMvc.perform(post("/api/courses/1/assignments")
                        .header("Authorization", teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Assignment deadline must be in the future"));
    }
}
