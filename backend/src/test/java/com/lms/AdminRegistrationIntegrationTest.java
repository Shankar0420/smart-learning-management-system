package com.lms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.AdminRegisterRequest;
import com.lms.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminRegistrationIntegrationTest {

    private static final String VALID_KEY = "ADMIN_SECRET_KEY_2026_GALGOTIAS";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getSystemAdminToken() throws Exception {
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
    void testAdminRegistrationWorkflow_Register_Login_AccessAdminDashboard() throws Exception {
        long timestamp = System.currentTimeMillis();
        String adminEmail = "reg.admin." + timestamp + "@lms.com";
        String adminPassword = "SecureAdminPass123";

        // 1. Register as Administrator with valid registration key
        AdminRegisterRequest req = new AdminRegisterRequest();
        req.setName("Associate Dean Registrar");
        req.setEmail(adminEmail);
        req.setPassword(adminPassword);
        req.setConfirmPassword(adminPassword);
        req.setRegistrationKey(VALID_KEY);

        MvcResult regResult = mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Associate Dean Registrar"))
                .andExpect(jsonPath("$.email").value(adminEmail))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        // 2. Newly registered admin logs in
        LoginRequest login = new LoginRequest();
        login.setEmail(adminEmail);
        login.setPassword(adminPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value(adminEmail))
                .andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String adminJwt = loginNode.get("token").asText();

        // 3. Newly registered admin accesses protected admin dashboard API
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").isNumber())
                .andExpect(jsonPath("$.totalCourses").isNumber());
    }

    @Test
    void testAdminRegistration_InvalidKeyReturns403Forbidden() throws Exception {
        long timestamp = System.currentTimeMillis();
        AdminRegisterRequest req = new AdminRegisterRequest();
        req.setName("Intruder Admin");
        req.setEmail("intruder." + timestamp + "@lms.com");
        req.setPassword("Password123");
        req.setConfirmPassword("Password123");
        req.setRegistrationKey("WRONG_OR_INVALID_KEY");

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", containsString("Invalid administrator registration key")));
    }

    @Test
    void testAdminRegistration_MissingKeyReturns400BadRequest() throws Exception {
        long timestamp = System.currentTimeMillis();
        AdminRegisterRequest req = new AdminRegisterRequest();
        req.setName("Missing Key Admin");
        req.setEmail("nokey." + timestamp + "@lms.com");
        req.setPassword("Password123");
        req.setConfirmPassword("Password123");
        req.setRegistrationKey(""); // Blank key

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testAdminRegistration_MismatchedPasswordsReturns400BadRequest() throws Exception {
        long timestamp = System.currentTimeMillis();
        AdminRegisterRequest req = new AdminRegisterRequest();
        req.setName("Mismatch Admin");
        req.setEmail("mismatch." + timestamp + "@lms.com");
        req.setPassword("Password123");
        req.setConfirmPassword("DifferentPassword123");
        req.setRegistrationKey(VALID_KEY);

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Passwords do not match")));
    }

    @Test
    void testAdminRegistration_DuplicateEmailReturns409Conflict() throws Exception {
        long timestamp = System.currentTimeMillis();
        String email = "dup.admin." + timestamp + "@lms.com";

        AdminRegisterRequest req1 = new AdminRegisterRequest();
        req1.setName("First Admin");
        req1.setEmail(email);
        req1.setPassword("Password123");
        req1.setConfirmPassword("Password123");
        req1.setRegistrationKey(VALID_KEY);

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Attempt registering with the same email
        AdminRegisterRequest req2 = new AdminRegisterRequest();
        req2.setName("Second Admin");
        req2.setEmail(email);
        req2.setPassword("Password456");
        req2.setConfirmPassword("Password456");
        req2.setRegistrationKey(VALID_KEY);

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Email is already registered")));
    }

    @Test
    void testAdminRegistration_InvalidEmailReturns400BadRequest() throws Exception {
        AdminRegisterRequest req = new AdminRegisterRequest();
        req.setName("Bad Email Admin");
        req.setEmail("not-a-valid-email");
        req.setPassword("Password123");
        req.setConfirmPassword("Password123");
        req.setRegistrationKey(VALID_KEY);

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testAdminRegistration_ShortPasswordReturns400BadRequest() throws Exception {
        AdminRegisterRequest req = new AdminRegisterRequest();
        req.setName("Short Pass Admin");
        req.setEmail("shortpass." + System.currentTimeMillis() + "@lms.com");
        req.setPassword("12345"); // < 6 chars
        req.setConfirmPassword("12345");
        req.setRegistrationKey(VALID_KEY);

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testAuthenticatedAdminCanCreateAnotherAdmin() throws Exception {
        String adminToken = getSystemAdminToken();
        long timestamp = System.currentTimeMillis();
        String newAdminEmail = "dean." + timestamp + "@lms.com";

        AdminRegisterRequest req = new AdminRegisterRequest();
        req.setName("Dr. Academic Dean");
        req.setEmail(newAdminEmail);
        req.setPassword("DeanPassword123");
        req.setConfirmPassword("DeanPassword123");
        req.setRegistrationKey(VALID_KEY);

        mockMvc.perform(post("/api/admin/administrators")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dr. Academic Dean"))
                .andExpect(jsonPath("$.email").value(newAdminEmail))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Verify newly created admin appears in /api/admin/administrators
        mockMvc.perform(get("/api/admin/administrators")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(newAdminEmail)));
    }
}
