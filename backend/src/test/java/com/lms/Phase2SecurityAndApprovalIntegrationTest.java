package com.lms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.dto.TeacherRegisterRequest;
import com.lms.entity.Role;
import com.lms.repository.UserRepository;
import com.lms.service.ProfessionalIdService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Phase2SecurityAndApprovalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessionalIdService professionalIdService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String getAdminToken() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@lms.com");
        login.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    @Test
    void testStudentRegistration_ApprovalWorkflow_And_ProfessionalIdGeneration() throws Exception {
        String adminToken = getAdminToken();
        long ts = System.currentTimeMillis();
        String studentEmail = "stu.test." + ts + "@lms.com";

        // 1. Student registers
        RegisterRequest regReq = new RegisterRequest();
        regReq.setName("Rohit Sharma");
        regReq.setEmail(studentEmail);
        regReq.setPassword("pass123456");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(studentEmail))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andReturn();

        JsonNode regNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        long studentId = regNode.get("id").asLong();

        // 2. Pending student CANNOT log in
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(studentEmail);
        loginReq.setPassword("pass123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("pending approval")));

        // 3. Admin sees student in pending students list
        mockMvc.perform(get("/api/admin/students/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(studentEmail)));

        // 4. Admin approves the student -> Student ID must be generated with format GUL-STU-2026-XXX
        MvcResult approveResult = mockMvc.perform(put("/api/admin/students/" + studentId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId))
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.professionalId", startsWith("GUL-STU-2026-")))
                .andExpect(jsonPath("$.enrollmentNumber", notNullValue()))
                .andReturn();

        JsonNode approveNode = objectMapper.readTree(approveResult.getResponse().getContentAsString());
        String generatedStudentId = approveNode.get("professionalId").asText();

        // 5. Approved student logs in successfully
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.professionalId").value(generatedStudentId))
                .andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String studentToken = loginNode.get("token").asText();

        // 6. Access student dashboard API
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        // 7. Search user by professional ID
        mockMvc.perform(get("/api/admin/users")
                        .param("q", generatedStudentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professionalId").value(generatedStudentId));
    }

    @Test
    void testTeacherRegistration_ApprovalWorkflow_And_ProfessionalIdGeneration() throws Exception {
        String adminToken = getAdminToken();
        long ts = System.currentTimeMillis();
        String teacherEmail = "prof.phase2." + ts + "@lms.com";

        TeacherRegisterRequest req = new TeacherRegisterRequest();
        req.setName("Dr. Anita Roy");
        req.setEmail(teacherEmail);
        req.setEmployeeId("EMP-" + ts);
        req.setPassword("teachPass123");

        MvcResult regRes = mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"))
                .andReturn();

        long teacherId = objectMapper.readTree(regRes.getResponse().getContentAsString()).get("id").asLong();

        // Admin approves teacher
        mockMvc.perform(put("/api/admin/teachers/" + teacherId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.professionalId", startsWith("GUL-TEA-2026-")));
    }

    @Test
    void testStudentRejectionWorkflow_CannotLogin() throws Exception {
        String adminToken = getAdminToken();
        long ts = System.currentTimeMillis();
        String email = "rejected." + ts + "@lms.com";

        RegisterRequest regReq = new RegisterRequest();
        regReq.setName("Rejected Applicant");
        regReq.setEmail(email);
        regReq.setPassword("pass123456");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long userId = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("id").asLong();

        // Admin rejects student
        mockMvc.perform(put("/api/admin/students/" + userId + "/reject")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"))
                .andExpect(jsonPath("$.active").value(false));

        // Rejected student login attempt fails
        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("pass123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("rejected")));
    }

    @Test
    void testPrimaryAdminProtection_CannotBeDeactivated() throws Exception {
        String adminToken = getAdminToken();

        // Attempt to deactivate primary admin id=1
        mockMvc.perform(put("/api/admin/users/1/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Primary System Administrator account cannot be deactivated")));
    }

    @Test
    void testImmediateTokenRevocation_OnAccountDeactivation() throws Exception {
        String adminToken = getAdminToken();
        long ts = System.currentTimeMillis();
        String studentEmail = "active.deact." + ts + "@lms.com";

        // Register and approve student
        RegisterRequest regReq = new RegisterRequest();
        regReq.setName("Revocation Test");
        regReq.setEmail(studentEmail);
        regReq.setPassword("pass123456");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long studentId = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/admin/students/" + studentId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Student logs in and obtains token
        LoginRequest login = new LoginRequest();
        login.setEmail(studentEmail);
        login.setPassword("pass123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String issuedToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Token works
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + issuedToken))
                .andExpect(status().isOk());

        // Admin deactivates student
        mockMvc.perform(put("/api/admin/users/" + studentId + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // IMMEDIATE REVOCATION: Old token MUST fail immediately with 401 Unauthorized
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + issuedToken))
                .andExpect(status().isUnauthorized());

        // Even after admin reactivates student, previously revoked token MUST REMAIN INVALID
        mockMvc.perform(put("/api/admin/users/" + studentId + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + issuedToken))
                .andExpect(status().isUnauthorized());

        // Fresh login after reactivation succeeds and works with a new token
        MvcResult freshLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String freshToken = objectMapper.readTree(freshLoginResult.getResponse().getContentAsString()).get("token").asText();
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + freshToken))
                .andExpect(status().isOk());
    }

    @Test
    void testConcurrentIdGeneration_NeverProducesDuplicates() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                latch.await();
                TransactionTemplate tt = new TransactionTemplate(transactionManager);
                return tt.execute(status -> professionalIdService.generateNextId(Role.STUDENT));
            }));
        }

        latch.countDown(); // Trigger all threads simultaneously

        Set<String> generatedIds = Collections.synchronizedSet(new HashSet<>());
        for (Future<String> future : futures) {
            String id = future.get();
            assertNotNull(id);
            assertTrue(id.startsWith("GUL-STU-2026-"));
            boolean added = generatedIds.add(id);
            assertTrue(added, "Duplicate ID detected in concurrent generation: " + id);
        }

        assertEquals(threads, generatedIds.size(), "All concurrent IDs must be unique");
        executor.shutdown();
    }

    @Test
    void testGetPendingStudentsAndSearchByProfessionalId() throws Exception {
        String adminToken = getAdminToken();

        // 1. Register a student
        String email = "search_test_" + System.currentTimeMillis() + "@galgotiasuniversity.edu.in";
        RegisterRequest req = new RegisterRequest();
        req.setName("Searchable Student");
        req.setEmail(email);
        req.setPassword("Password123!");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode regNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        long studentId = regNode.get("id").asLong();

        // 2. Verify student appears in /api/admin/students/pending
        mockMvc.perform(get("/api/admin/students/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) studentId)));

        // 3. Approve student to receive GUL-STU-2026-XXX
        MvcResult approveResult = mockMvc.perform(put("/api/admin/students/" + studentId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionalId").value(startsWith("GUL-STU-2026-")))
                .andReturn();

        JsonNode approveNode = objectMapper.readTree(approveResult.getResponse().getContentAsString());
        String generatedProfId = approveNode.get("professionalId").asText();

        // 4. Search by professional ID via /api/admin/users?q=
        mockMvc.perform(get("/api/admin/users")
                        .param("q", generatedProfId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value(email))
                .andExpect(jsonPath("$[0].professionalId").value(generatedProfId));

        // 5. Search by professional ID via /api/admin/users/search?q=
        mockMvc.perform(get("/api/admin/users/search")
                        .param("q", generatedProfId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value((int) studentId));
    }
}

