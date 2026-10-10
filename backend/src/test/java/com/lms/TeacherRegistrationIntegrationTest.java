package com.lms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.LoginRequest;
import com.lms.dto.TeacherRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TeacherRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAdminToken() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@lms.com");
        login.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    @Test
    void testTeacherRegistrationWorkflow_Register_Pending_Approve_Login_Access() throws Exception {
        long timestamp = System.currentTimeMillis();
        String teacherEmail = "prof." + timestamp + "@galgotias.edu";
        String empId = "EMP-" + timestamp;

        // 1. Teacher registers
        TeacherRegisterRequest req = new TeacherRegisterRequest();
        req.setName("Prof. Alok Nath");
        req.setEmail(teacherEmail);
        req.setEmployeeId(empId);
        req.setPassword("teachPass123");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(teacherEmail))
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.employeeId").value(empId))
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        JsonNode regNode = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        long teacherId = regNode.get("id").asLong();

        // 2. Pending teacher tries to log in -> MUST FAIL with 401 pending approval
        LoginRequest pendingLogin = new LoginRequest();
        pendingLogin.setEmail(teacherEmail);
        pendingLogin.setPassword("teachPass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pendingLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("pending approval")));

        // 3. Admin views pending teachers
        String adminToken = getAdminToken();

        mockMvc.perform(get("/api/admin/teachers/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(teacherEmail)));

        // 4. Admin approves the teacher
        mockMvc.perform(put("/api/admin/teachers/" + teacherId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teacherId))
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.active").value(true));

        // 5. Teacher logs in successfully after approval
        MvcResult approvedLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pendingLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andReturn();

        JsonNode approvedLoginNode = objectMapper.readTree(approvedLoginResult.getResponse().getContentAsString());
        String teacherToken = approvedLoginNode.get("token").asText();

        // 6. Teacher can now access Teacher Dashboard API
        mockMvc.perform(get("/api/teacher/dashboard")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourses").isNumber());
    }

    @Test
    void testTeacherRegistration_RejectionWorkflow() throws Exception {
        long timestamp = System.currentTimeMillis();
        String teacherEmail = "reject." + timestamp + "@galgotias.edu";
        String empId = "EMP-REJ-" + timestamp;

        TeacherRegisterRequest req = new TeacherRegisterRequest();
        req.setName("Prof. Rejected Candidate");
        req.setEmail(teacherEmail);
        req.setEmployeeId(empId);
        req.setPassword("pass12345");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode regNode = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        long teacherId = regNode.get("id").asLong();

        // Admin rejects the teacher
        String adminToken = getAdminToken();
        mockMvc.perform(put("/api/admin/teachers/" + teacherId + "/reject")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"))
                .andExpect(jsonPath("$.active").value(false));

        // Teacher tries to log in -> rejected message
        LoginRequest login = new LoginRequest();
        login.setEmail(teacherEmail);
        login.setPassword("pass12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("rejected")));
    }

    @Test
    void testDuplicateEmailReturns409() throws Exception {
        long timestamp = System.currentTimeMillis();
        TeacherRegisterRequest req1 = new TeacherRegisterRequest();
        req1.setName("Teacher One");
        req1.setEmail("dup." + timestamp + "@galgotias.edu");
        req1.setEmployeeId("EMP1-" + timestamp);
        req1.setPassword("pass12345");

        mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Same email, different empId
        TeacherRegisterRequest req2 = new TeacherRegisterRequest();
        req2.setName("Teacher Two");
        req2.setEmail("dup." + timestamp + "@galgotias.edu");
        req2.setEmployeeId("EMP2-" + timestamp);
        req2.setPassword("pass12345");

        mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Email is already registered")));
    }

    @Test
    void testDuplicateEmployeeIdReturns409() throws Exception {
        long timestamp = System.currentTimeMillis();
        TeacherRegisterRequest req1 = new TeacherRegisterRequest();
        req1.setName("Teacher Alpha");
        req1.setEmail("alpha." + timestamp + "@galgotias.edu");
        req1.setEmployeeId("EMP-SAME-" + timestamp);
        req1.setPassword("pass12345");

        mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Different email, same empId
        TeacherRegisterRequest req2 = new TeacherRegisterRequest();
        req2.setName("Teacher Beta");
        req2.setEmail("beta." + timestamp + "@galgotias.edu");
        req2.setEmployeeId("EMP-SAME-" + timestamp);
        req2.setPassword("pass12345");

        mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Employee ID is already registered")));
    }

    @Test
    void testNonAdminCannotApproveTeacher() throws Exception {
        mockMvc.perform(put("/api/admin/teachers/1/approve"))
                .andExpect(status().isUnauthorized());
    }
}
