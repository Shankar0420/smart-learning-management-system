package com.lms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.*;
import com.lms.entity.Role;
import com.lms.repository.PasswordResetTokenRepository;
import com.lms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Phase3CoreFeaturesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    @Test
    void testStudyMaterialUploadDownloadAndDelete() throws Exception {
        String teacherToken = loginAndGetToken("teacher@lms.com", "teacher123");
        String studentToken = loginAndGetToken("student1@lms.com", "student123");

        // 1. Teacher uploads PDF study material to course 1
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "lecture1_intro.pdf",
                "application/pdf",
                "Sample PDF content for Galgotias LMS Lecture 1".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/courses/1/materials")
                        .file(pdfFile)
                        .param("title", "Lecture 1: Introduction to Java")
                        .param("description", "Slides covering Java Virtual Machine and byte code")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Lecture 1: Introduction to Java"))
                .andExpect(jsonPath("$.fileType").value("PDF"))
                .andReturn();

        JsonNode uploadNode = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        long materialId = uploadNode.get("id").asLong();

        // 2. Student views materials for enrolled course
        mockMvc.perform(get("/api/courses/1/materials")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) materialId)));

        // 3. Student downloads the study material
        mockMvc.perform(get("/api/materials/" + materialId + "/download")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("lecture1_intro.pdf")))
                .andExpect(content().bytes("Sample PDF content for Galgotias LMS Lecture 1".getBytes()));

        // 4. Teacher deletes the study material
        mockMvc.perform(delete("/api/materials/" + materialId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // 5. Subsequent download fails with 404
        mockMvc.perform(get("/api/materials/" + materialId + "/download")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testQuizCreationTrueFalseAndMCQWithPublishAndAttempt() throws Exception {
        String teacherToken = loginAndGetToken("teacher@lms.com", "teacher123");
        String studentToken = loginAndGetToken("student1@lms.com", "student123");

        // 1. Teacher creates a Quiz in Course 1
        QuizRequest quizReq = new QuizRequest();
        quizReq.setTitle("Module 1 Assessment: Core Java");
        quizReq.setDescription("Multiple Choice and True/False assessment");

        MvcResult quizRes = mockMvc.perform(post("/api/courses/1/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizReq))
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        long quizId = objectMapper.readTree(quizRes.getResponse().getContentAsString()).get("id").asLong();

        // 2. Add a Multiple Choice Question (4 options)
        QuizQuestionRequest mcqReq = new QuizQuestionRequest();
        mcqReq.setQuestionText("Which keyword in Java prevents a method from being overridden?");
        mcqReq.setMarks(2);
        mcqReq.setOptions(Arrays.asList(
                new QuizOptionRequest("static", false),
                new QuizOptionRequest("final", true),
                new QuizOptionRequest("abstract", false),
                new QuizOptionRequest("synchronized", false)
        ));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mcqReq))
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated());

        // 3. Add a True / False Question (2 options)
        QuizQuestionRequest tfReq = new QuizQuestionRequest();
        tfReq.setQuestionText("In Java, Strings are immutable objects.");
        tfReq.setMarks(1);
        tfReq.setOptions(Arrays.asList(
                new QuizOptionRequest("True", true),
                new QuizOptionRequest("False", false)
        ));

        MvcResult tfResult = mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tfReq))
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated())
                .andReturn();

        long tfQuestionId = objectMapper.readTree(tfResult.getResponse().getContentAsString()).get("id").asLong();

        // 4. Fetch quiz as student: correct answers should NOT be visible
        MvcResult studentView = mockMvc.perform(get("/api/quizzes/" + quizId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions", hasSize(2)))
                .andReturn();

        String body = studentView.getResponse().getContentAsString();
        assertFalse(body.contains("\"correct\":true"), "Student must NOT see correct answers in quiz payload");

        // 5. Student submits answers
        // Look up option IDs from teacher view
        MvcResult teacherView = mockMvc.perform(get("/api/quizzes/" + quizId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode qTree = objectMapper.readTree(teacherView.getResponse().getContentAsString());
        long correctMcqOptionId = -1;
        long correctTfOptionId = -1;
        long mcqQId = qTree.get("questions").get(0).get("id").asLong();

        for (JsonNode opt : qTree.get("questions").get(0).get("options")) {
            if (opt.get("correct").asBoolean()) correctMcqOptionId = opt.get("id").asLong();
        }
        for (JsonNode opt : qTree.get("questions").get(1).get("options")) {
            if (opt.get("correct").asBoolean()) correctTfOptionId = opt.get("id").asLong();
        }

        QuizSubmitRequest submitReq = new QuizSubmitRequest();
        submitReq.setAnswers(Arrays.asList(
                new QuizSubmitRequest.SelectedAnswer(mcqQId, correctMcqOptionId),
                new QuizSubmitRequest.SelectedAnswer(tfQuestionId, correctTfOptionId)
        ));

        MvcResult attemptResult = mockMvc.perform(post("/api/quizzes/" + quizId + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq))
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(3))
                .andExpect(jsonPath("$.totalMarks").value(3))
                .andReturn();

        // 6. Delete question as teacher
        mockMvc.perform(delete("/api/quizzes/questions/" + tfQuestionId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // 7. Toggle publish status
        mockMvc.perform(put("/api/quizzes/" + quizId + "/publish")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // 8. Delete quiz
        mockMvc.perform(delete("/api/quizzes/" + quizId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());
    }

    @Test
    void testAssignmentWithSupportingDocAndStudentFileUpload() throws Exception {
        String teacherToken = loginAndGetToken("teacher@lms.com", "teacher123");
        String studentToken = loginAndGetToken("student1@lms.com", "student123");

        // 1. Teacher creates assignment with attachment
        MockMultipartFile assignmentDoc = new MockMultipartFile(
                "file",
                "lab_specs.pdf",
                "application/pdf",
                "Specifications for Lab 1".getBytes()
        );

        LocalDateTime deadline = LocalDateTime.now().plusDays(5);
        MvcResult createResult = mockMvc.perform(multipart("/api/courses/1/assignments/upload")
                        .file(assignmentDoc)
                        .param("title", "Lab Assignment: Multithreading")
                        .param("description", "Implement Producer-Consumer problem using BlockingQueue")
                        .param("deadline", deadline.toString())
                        .param("maxMarks", "100")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasAttachment").value(true))
                .andExpect(jsonPath("$.attachmentOriginalName").value("lab_specs.pdf"))
                .andReturn();

        long assignmentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Student downloads assignment supporting document
        mockMvc.perform(get("/api/assignments/" + assignmentId + "/attachment")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("lab_specs.pdf")))
                .andExpect(content().bytes("Specifications for Lab 1".getBytes()));

        // 3. Student submits assignment with uploaded solution file
        MockMultipartFile submissionFile = new MockMultipartFile(
                "file",
                "student_solution.zip",
                "application/zip",
                "Simulated Java Source Code Zip File".getBytes()
        );

        MvcResult subResult = mockMvc.perform(multipart("/api/assignments/" + assignmentId + "/submit-file")
                        .file(submissionFile)
                        .param("answerText", "Attached my multithreading solution code.")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSubmissionFile").value(true))
                .andExpect(jsonPath("$.submissionOriginalName").value("student_solution.zip"))
                .andReturn();

        long submissionId = objectMapper.readTree(subResult.getResponse().getContentAsString()).get("id").asLong();

        // 4. Teacher downloads student submission file
        mockMvc.perform(get("/api/assignments/submissions/" + submissionId + "/file")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("student_solution.zip")))
                .andExpect(content().bytes("Simulated Java Source Code Zip File".getBytes()));

        // 5. Clean up assignment
        mockMvc.perform(delete("/api/assignments/" + assignmentId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());
    }

    @Test
    void testPasswordResetFlowWithTokenInvalidation() throws Exception {
        String email = "student2@lms.com";

        // 1. Request forgot password
        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest(email);
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());

        // 2. Retrieve generated token from test repository
        var user = userRepository.findByEmail(email).orElseThrow();
        var tokens = passwordResetTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()) && !t.isUsed())
                .toList();

        assertFalse(tokens.isEmpty(), "Password reset token should be generated");
        String resetToken = tokens.get(0).getToken();

        // 3. Verify token endpoint returns valid=true
        mockMvc.perform(get("/api/auth/verify-reset-token")
                        .param("token", resetToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        // 4. Reset password
        String newPassword = "NewSecurePassword123!";
        ResetPasswordRequest resetReq = new ResetPasswordRequest(resetToken, newPassword);
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("successfully reset")));

        // 5. Trying to use token again fails
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isBadRequest());

        // 6. Login with old password fails
        LoginRequest oldLogin = new LoginRequest();
        oldLogin.setEmail(email);
        oldLogin.setPassword("student123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldLogin)))
                .andExpect(status().isUnauthorized());

        // 7. Login with new password succeeds
        LoginRequest newLogin = new LoginRequest();
        newLogin.setEmail(email);
        newLogin.setPassword(newPassword);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testStudentDashboardEnhancedMetrics() throws Exception {
        String studentToken = loginAndGetToken("student1@lms.com", "student123");

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionalId").value("GUL-STU-2026-001"))
                .andExpect(jsonPath("$.totalEnrolledCourses").isNumber())
                .andExpect(jsonPath("$.availableMaterialsCount").isNumber())
                .andExpect(jsonPath("$.publishedQuizzesCount").isNumber())
                .andExpect(jsonPath("$.totalAssignmentsCount").isNumber())
                .andExpect(jsonPath("$.upcomingAssignments").isArray())
                .andExpect(jsonPath("$.recentQuizScores").isArray())
                .andExpect(jsonPath("$.recentMaterials").isArray());
    }
}
