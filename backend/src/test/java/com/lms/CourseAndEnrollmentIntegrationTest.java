package com.lms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.CourseRequest;
import com.lms.dto.LessonRequest;
import com.lms.dto.ModuleRequest;
import com.lms.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseAndEnrollmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private com.lms.repository.EnrollmentRepository enrollmentRepository;

    @Autowired
    private com.lms.repository.UserRepository userRepository;

    private String teacherToken;
    private String student1Token;
    private String student2Token;

    @BeforeEach
    void setUp() {
        teacherToken = "Bearer " + jwtUtils.generateToken("teacher@lms.com", "TEACHER");
        student1Token = "Bearer " + jwtUtils.generateToken("student1@lms.com", "STUDENT");
        student2Token = "Bearer " + jwtUtils.generateToken("student2@lms.com", "STUDENT");

        userRepository.findByEmail("student2@lms.com").ifPresent(s -> {
            enrollmentRepository.findByStudentIdAndCourseId(s.getId(), 2L).ifPresent(enrollmentRepository::delete);
            enrollmentRepository.findByStudentIdAndCourseId(s.getId(), 3L).ifPresent(enrollmentRepository::delete);
        });
    }

    @Test
    void testPublicCanViewCoursesWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].title").isNotEmpty());
    }

    @Test
    void testStudentCannotCreateCourseReturns403() throws Exception {
        CourseRequest request = new CourseRequest();
        request.setTitle("Student Attempt Course");
        request.setDescription("This creation attempt should fail with 403 Forbidden.");

        mockMvc.perform(post("/api/courses")
                        .header("Authorization", student1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTeacherCanCreateCourseAndAddModuleAndLesson() throws Exception {
        CourseRequest courseReq = new CourseRequest();
        courseReq.setTitle("Full Stack Cloud Engineering");
        courseReq.setDescription("Master scalable microservices and cloud deployments.");

        String courseResponse = mockMvc.perform(post("/api/courses")
                        .header("Authorization", teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Full Stack Cloud Engineering"))
                .andReturn().getResponse().getContentAsString();

        Long courseId = objectMapper.readTree(courseResponse).get("id").asLong();

        // Teacher creates Module
        ModuleRequest moduleReq = new ModuleRequest();
        moduleReq.setTitle("Introduction to Microservices");
        moduleReq.setDescription("Architecture patterns and design.");
        moduleReq.setOrderIndex(1);

        String moduleResponse = mockMvc.perform(post("/api/courses/" + courseId + "/modules")
                        .header("Authorization", teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Introduction to Microservices"))
                .andReturn().getResponse().getContentAsString();

        Long moduleId = objectMapper.readTree(moduleResponse).get("id").asLong();

        // Teacher creates Lesson
        LessonRequest lessonReq = new LessonRequest();
        lessonReq.setTitle("Service Discovery and API Gateways");
        lessonReq.setContent("Detailed guide on configuring Spring Cloud Gateway and Eureka.");
        lessonReq.setOrderIndex(1);

        mockMvc.perform(post("/api/modules/" + moduleId + "/lessons")
                        .header("Authorization", teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Service Discovery and API Gateways"));
    }

    @Test
    void testStudentEnrollmentAndDuplicatePrevention() throws Exception {
        // student2 enrolls in course 2
        mockMvc.perform(post("/api/courses/2/enroll")
                        .header("Authorization", student2Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Duplicate enrollment must return 409 Conflict
        mockMvc.perform(post("/api/courses/2/enroll")
                        .header("Authorization", student2Token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Student is already enrolled in this course"));
    }

    @Test
    void testUnenrolledStudentCannotAccessProtectedContent() throws Exception {
        // student2 is NOT enrolled in course 3
        mockMvc.perform(get("/api/courses/3/modules")
                        .header("Authorization", student2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You must be enrolled in this course to access its content"));
    }

    @Test
    void testEnrolledStudentCanAccessContent() throws Exception {
        // student1 is enrolled in course 1
        mockMvc.perform(get("/api/courses/1/modules")
                        .header("Authorization", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }
}
