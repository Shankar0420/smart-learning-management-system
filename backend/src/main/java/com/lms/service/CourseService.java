package com.lms.service;

import com.lms.dto.CourseRequest;
import com.lms.dto.CourseResponse;
import com.lms.entity.Assignment;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.Module;
import com.lms.entity.Quiz;
import com.lms.entity.QuizQuestion;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final GradeRepository gradeRepository;
    private final LessonProgressRepository lessonProgressRepository;

    public CourseService(CourseRepository courseRepository,
                         UserRepository userRepository,
                         ModuleRepository moduleRepository,
                         LessonRepository lessonRepository,
                         EnrollmentRepository enrollmentRepository,
                         QuizRepository quizRepository,
                         QuizQuestionRepository quizQuestionRepository,
                         QuizOptionRepository quizOptionRepository,
                         QuizAttemptRepository quizAttemptRepository,
                         AssignmentRepository assignmentRepository,
                         AssignmentSubmissionRepository assignmentSubmissionRepository,
                         GradeRepository gradeRepository,
                         LessonProgressRepository lessonProgressRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizOptionRepository = quizOptionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.gradeRepository = gradeRepository;
        this.lessonProgressRepository = lessonProgressRepository;
    }

    public List<CourseResponse> getAllActiveCourses() {
        return courseRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return mapToResponse(course);
    }

    public Course getCourseEntity(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    public List<CourseResponse> getCoursesByTeacher(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));
        return courseRepository.findByTeacher(teacher).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));

        if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only teachers can create courses");
        }

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setTeacher(teacher);
        course.setActive(true);

        Course saved = courseRepository.save(course);
        return mapToResponse(saved);
    }

    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseRequest request, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() != Role.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to update this course");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());

        Course updated = courseRepository.save(course);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCourse(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() != Role.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to delete this course");
        }

        // Clean deletion of all dependent records to preserve relational integrity
        // 1. Grades
        gradeRepository.deleteAll(gradeRepository.findAll().stream()
                .filter(g -> g.getCourse().getId().equals(courseId))
                .collect(Collectors.toList()));

        // 2. Lesson progress & lessons & modules
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        for (Module m : modules) {
            List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(m);
            for (Lesson l : lessons) {
                lessonProgressRepository.deleteAll(lessonProgressRepository.findAll().stream()
                        .filter(lp -> lp.getLesson().getId().equals(l.getId()))
                        .collect(Collectors.toList()));
                lessonRepository.delete(l);
            }
            moduleRepository.delete(m);
        }

        // 3. Quizzes, questions, options, attempts
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        for (Quiz q : quizzes) {
            quizAttemptRepository.deleteAll(quizAttemptRepository.findAll().stream()
                    .filter(qa -> qa.getQuiz().getId().equals(q.getId()))
                    .collect(Collectors.toList()));

            List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(q.getId());
            for (QuizQuestion qq : questions) {
                quizOptionRepository.deleteAll(quizOptionRepository.findByQuestionId(qq.getId()));
                quizQuestionRepository.delete(qq);
            }
            quizRepository.delete(q);
        }

        // 4. Assignments & submissions
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        for (Assignment a : assignments) {
            assignmentSubmissionRepository.deleteAll(assignmentSubmissionRepository.findByAssignmentId(a.getId()));
            assignmentRepository.delete(a);
        }

        // 5. Enrollments
        enrollmentRepository.deleteAll(enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse().getId().equals(courseId))
                .collect(Collectors.toList()));

        // 6. Delete course
        courseRepository.delete(course);
    }

    public CourseResponse mapToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setTeacherId(course.getTeacher().getId());
        response.setTeacherName(course.getTeacher().getName());
        response.setActive(course.isActive());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());
        return response;
    }
}
