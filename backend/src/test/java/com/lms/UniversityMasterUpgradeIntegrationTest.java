package com.lms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.repository.*;
import com.lms.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UniversityMasterUpgradeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudyMaterialRepository studyMaterialRepository;

    @Autowired
    private StudyMaterialBookmarkRepository bookmarkRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AcademicCalendarRepository calendarRepository;

    @Autowired
    private DiscussionThreadRepository discussionThreadRepository;

    @Autowired
    private DiscussionReplyRepository discussionReplyRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private User teacher;
    private User student;
    private User admin;
    private Course course;
    private String teacherToken;
    private String studentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        admin = userRepository.save(new User("Admin " + uid, "admin." + uid + "@galgotias.edu", passwordEncoder.encode("admin123"), Role.ADMIN));

        teacher = new User("Dr. Sharma " + uid, "sharma." + uid + "@galgotias.edu", passwordEncoder.encode("pass123"), Role.TEACHER, "EMP" + uid, ApprovalStatus.APPROVED);
        teacher.setDepartment("Computer Science");
        teacher = userRepository.save(teacher);

        student = new User("Aarav Patel " + uid, "aarav." + uid + "@galgotias.edu", passwordEncoder.encode("pass123"), Role.STUDENT);
        student.setEnrollmentNumber("GU" + uid);
        student = userRepository.save(student);

        course = new Course();
        course.setTitle("Data Structures " + uid);
        course.setDescription("Advanced algorithms and complexity analysis");
        course.setTeacher(teacher);
        course.setActive(true);
        course = courseRepository.save(course);

        Enrollment enrollment = new Enrollment(student, course);
        enrollmentRepository.save(enrollment);

        adminToken = jwtUtils.generateToken(admin.getEmail(), admin.getRole().name());
        teacherToken = jwtUtils.generateToken(teacher.getEmail(), teacher.getRole().name());
        studentToken = jwtUtils.generateToken(student.getEmail(), student.getRole().name());
    }

    @Test
    void testStudyMaterialUploadSearchAndBookmark() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "lecture1.pdf", "application/pdf", "Dummy PDF Content".getBytes()
        );

        mockMvc.perform(multipart("/api/courses/" + course.getId() + "/materials")
                        .file(file)
                        .param("title", "Lecture 1: Trees and Graphs")
                        .param("description", "Tree traversal techniques")
                        .param("category", "LECTURE_NOTES")
                        .param("semester", "Semester 4")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Lecture 1: Trees and Graphs"))
                .andExpect(jsonPath("$.category").value("LECTURE_NOTES"));

        Long materialId = studyMaterialRepository.findByCourseId(course.getId()).get(0).getId();

        // Student bookmarks material
        mockMvc.perform(post("/api/materials/" + materialId + "/bookmark")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(true));

        // Retrieve student bookmarks
        mockMvc.perform(get("/api/materials/bookmarks")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(materialId))
                .andExpect(jsonPath("$[0].bookmarked").value(true));
    }

    @Test
    void testAcademicCalendarEvents() throws Exception {
        CalendarEventRequest req = new CalendarEventRequest();
        req.setTitle("Mid-Term Examinations 2026");
        req.setDescription("Centralized university examinations across all schools");
        req.setEventType("EXAM");
        req.setStartDate(LocalDateTime.now().plusDays(10));
        req.setEndDate(LocalDateTime.now().plusDays(15));
        req.setTargetRole("ALL");

        mockMvc.perform(post("/api/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Mid-Term Examinations 2026"))
                .andExpect(jsonPath("$.eventType").value("EXAM"));

        // Student retrieves events
        mockMvc.perform(get("/api/calendar/events")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    void testDiscussionForumLifecycle() throws Exception {
        DiscussionThreadRequest threadReq = new DiscussionThreadRequest();
        threadReq.setTitle("How to implement Red-Black Tree rotation in Java?");
        threadReq.setContent("I am stuck on balancing logic after left rotation.");

        mockMvc.perform(post("/api/discussions/courses/" + course.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(threadReq))
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("How to implement Red-Black Tree rotation in Java?"));

        Long threadId = discussionThreadRepository.findByCourseIdOrderByPinnedDescCreatedAtDesc(course.getId()).get(0).getId();

        // Faculty replies with solution
        DiscussionReplyRequest replyReq = new DiscussionReplyRequest();
        replyReq.setContent("Remember that in left rotation, the right child's left subtree becomes the left child's right subtree.");

        mockMvc.perform(post("/api/discussions/threads/" + threadId + "/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyReq))
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(replyReq.getContent()));

        Long replyId = discussionReplyRepository.findByThreadIdOrderByCreatedAtAsc(threadId).get(0).getId();

        // Student marks accepted solution
        mockMvc.perform(put("/api/discussions/replies/" + replyId + "/accept")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
    }

    @Test
    void testNotificationsLifecycle() throws Exception {
        notificationRepository.save(new Notification(
                student, "Assignment Published", "Assignment 1 is now live", "ASSIGNMENT", "/student/assignments.html"
        ));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));

        mockMvc.perform(put("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    void testAiQuizGeneration() throws Exception {
        mockMvc.perform(post("/api/quizzes/generate-ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "topic", "Binary Search Trees",
                                "difficulty", "MEDIUM",
                                "count", 3,
                                "questionType", "MCQ"
                        )))
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.questions.length()").value(3));
    }
}
