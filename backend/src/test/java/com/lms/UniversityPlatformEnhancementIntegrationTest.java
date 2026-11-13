package com.lms;

import com.lms.dto.AiChatRequest;
import com.lms.dto.AnnouncementRequest;
import com.lms.dto.AttendanceSessionRequest;
import com.lms.entity.AttendanceStatus;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import com.lms.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UniversityPlatformEnhancementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        User admin = userRepository.save(new User("Test Admin", "admin." + uid + "@lms.com", passwordEncoder.encode("admin123"), Role.ADMIN));
        User teacher = userRepository.save(new User("Test Teacher", "teacher." + uid + "@lms.com", passwordEncoder.encode("teacher123"), Role.TEACHER));
        User student = userRepository.save(new User("Test Student", "student." + uid + "@lms.com", passwordEncoder.encode("student123"), Role.STUDENT));

        adminToken = "Bearer " + jwtUtils.generateToken(admin.getEmail(), admin.getRole().name());
        teacherToken = "Bearer " + jwtUtils.generateToken(teacher.getEmail(), teacher.getRole().name());
        studentToken = "Bearer " + jwtUtils.generateToken(student.getEmail(), student.getRole().name());
    }

    @Test
    void testAiChat_AuthenticatedUser_ReturnsAcademicReply() throws Exception {
        String payload = "{\"prompt\": \"What is Spring Security?\", \"context\": \"EXPLAIN_CONCEPT\"}";

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").isNotEmpty())
                .andExpect(jsonPath("$.model").isNotEmpty());
    }

    @Test
    void testAnnouncements_AdminCanCreateAndStudentCanView() throws Exception {
        String payload = "{\"title\": \"Semester End Examination Notice\", \"content\": \"Final examinations will begin next week.\", \"priority\": \"HIGH\", \"targetRole\": \"ALL\"}";

        mockMvc.perform(post("/api/announcements")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Semester End Examination Notice"));

        mockMvc.perform(get("/api/announcements")
                        .header("Authorization", studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testAuditLogs_AdminCanRetrieveRecentLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testStudentAttendanceSummary_ReturnsValidSummary() throws Exception {
        mockMvc.perform(get("/api/attendance/my-summary")
                        .header("Authorization", studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallPercentage").isNumber());
    }
}
