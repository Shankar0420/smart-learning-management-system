package com.lms;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Phase4ProfessionalUiAndHealthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    @Test
    public void testPublicHealthEndpointReturnsUpAndGalgotiasBranding() throws Exception {
        mockMvc.perform(get("/api/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.app").value("Galgotias LMS"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    public void testAdminDashboardContainsActiveAndInactiveAccountsMetrics() throws Exception {
        String adminToken = loginAndGetToken("admin@lms.com", "admin123");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").isNumber())
                .andExpect(jsonPath("$.totalTeachers").isNumber())
                .andExpect(jsonPath("$.totalCourses").isNumber())
                .andExpect(jsonPath("$.activeAccounts").isNumber())
                .andExpect(jsonPath("$.inactiveAccounts").isNumber())
                .andExpect(jsonPath("$.activeAccounts", greaterThanOrEqualTo(1)));
    }

    @Test
    public void testTeacherDashboardContainsMaterialsQuizzesAndAssignmentsMetrics() throws Exception {
        String teacherToken = loginAndGetToken("teacher@lms.com", "teacher123");

        mockMvc.perform(get("/api/teacher/dashboard")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourses").isNumber())
                .andExpect(jsonPath("$.totalStudents").isNumber())
                .andExpect(jsonPath("$.totalMaterials").isNumber())
                .andExpect(jsonPath("$.totalQuizzes").isNumber())
                .andExpect(jsonPath("$.totalAssignments").isNumber())
                .andExpect(jsonPath("$.recentMaterials").isArray());
    }

    @Test
    public void testStudentDashboardContainsAllRequiredMetricsAndProfessionalId() throws Exception {
        String studentToken = loginAndGetToken("student1@lms.com", "student123");

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionalId").isNotEmpty())
                .andExpect(jsonPath("$.totalEnrolledCourses").isNumber())
                .andExpect(jsonPath("$.availableMaterialsCount").isNumber())
                .andExpect(jsonPath("$.publishedQuizzesCount").isNumber())
                .andExpect(jsonPath("$.totalAssignmentsCount").isNumber())
                .andExpect(jsonPath("$.upcomingAssignments").isArray())
                .andExpect(jsonPath("$.recentQuizScores").isArray())
                .andExpect(jsonPath("$.recentMaterials").isArray());
    }

    @Test
    public void testCorsAllowsExactProductionNetlifyOrigin() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "https://smart-learning-management-system.netlify.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://smart-learning-management-system.netlify.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    public void testCorsRejectsUnrelatedNetlifySubdomain() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "https://unrelated-domain.netlify.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    public void testCorsRejectsUnauthorizedExternalOrigin() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "https://evil-attacker.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    public void testErrorHandlingReturnsSanitizedMessageWithoutExposingInternalTraces() throws Exception {
        LoginRequest badReq = new LoginRequest();
        badReq.setEmail("nonexistent@lms.com");
        badReq.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    public void testResourceNotFoundSanitizedErrorResponse() throws Exception {
        mockMvc.perform(get("/api/courses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found with id: 999999"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    public void testProdProfileConfigurationFileContainsExpectedProperties() throws Exception {
        java.util.Properties prodProps = new java.util.Properties();
        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("application-prod.properties")) {
            org.junit.jupiter.api.Assertions.assertNotNull(is, "application-prod.properties must be on classpath");
            prodProps.load(is);
        }
        org.junit.jupiter.api.Assertions.assertEquals("${HIBERNATE_DDL_AUTO:validate}", prodProps.getProperty("spring.jpa.hibernate.ddl-auto"));
        org.junit.jupiter.api.Assertions.assertEquals("never", prodProps.getProperty("server.error.include-message"));
        org.junit.jupiter.api.Assertions.assertEquals("never", prodProps.getProperty("server.error.include-stacktrace"));
        org.junit.jupiter.api.Assertions.assertEquals("${CORS_ALLOWED_ORIGINS:https://smart-learning-management-system.netlify.app}", prodProps.getProperty("app.cors.allowed-origins"));
    }
}
