package com.lms.config;

import com.lms.entity.*;
import com.lms.entity.Module;
import com.lms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final AssignmentRepository assignmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CourseRepository courseRepository,
                           ModuleRepository moduleRepository,
                           LessonRepository lessonRepository,
                           EnrollmentRepository enrollmentRepository,
                           QuizRepository quizRepository,
                           QuizQuestionRepository quizQuestionRepository,
                           QuizOptionRepository quizOptionRepository,
                           AssignmentRepository assignmentRepository,
                           LessonProgressRepository lessonProgressRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizOptionRepository = quizOptionRepository;
        this.assignmentRepository = assignmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Seed data already present
        }

        // 1. Seed Users (1 Admin, 1 Teacher, 2 Students)
        User admin = new User("System Admin", "admin@lms.com", passwordEncoder.encode("admin123"), Role.ADMIN);
        User teacher = new User("Dr. Ramesh Sharma", "teacher@lms.com", passwordEncoder.encode("teacher123"), Role.TEACHER);
        User student1 = new User("Aman Kumar", "student1@lms.com", passwordEncoder.encode("student123"), Role.STUDENT);
        User student2 = new User("Pooja Verma", "student2@lms.com", passwordEncoder.encode("student123"), Role.STUDENT);

        userRepository.save(admin);
        teacher = userRepository.save(teacher);
        student1 = userRepository.save(student1);
        student2 = userRepository.save(student2);

        // 2. Course 1: Java Programming Masterclass
        Course course1 = new Course();
        course1.setTitle("Java Programming Masterclass");
        course1.setDescription("Comprehensive Java course covering OOP, Collections, Concurrency, and Modern Java Features.");
        course1.setTeacher(teacher);
        course1.setActive(true);
        course1 = courseRepository.save(course1);

        // Modules & Lessons for Course 1
        Module mod1 = new Module();
        mod1.setTitle("Object-Oriented Programming");
        mod1.setDescription("Core OOP principles in Java: Encapsulation, Inheritance, Polymorphism, Abstraction");
        mod1.setCourse(course1);
        mod1.setOrderIndex(1);
        mod1 = moduleRepository.save(mod1);

        Lesson lesson1 = new Lesson();
        lesson1.setTitle("Classes, Objects, and Inheritance");
        lesson1.setContent("Classes define blueprint for objects. Inheritance enables code reuse using the 'extends' keyword.");
        lesson1.setModule(mod1);
        lesson1.setOrderIndex(1);
        lesson1 = lessonRepository.save(lesson1);

        Lesson lesson2 = new Lesson();
        lesson2.setTitle("Polymorphism and Interfaces");
        lesson2.setContent("Polymorphism allows methods to perform different tasks based on object type. Interfaces define contracts.");
        lesson2.setModule(mod1);
        lesson2.setOrderIndex(2);
        lesson2 = lessonRepository.save(lesson2);

        Module mod2 = new Module();
        mod2.setTitle("Collections and Exception Handling");
        mod2.setDescription("Working with List, Set, Map, and robust try-catch mechanisms");
        mod2.setCourse(course1);
        mod2.setOrderIndex(2);
        mod2 = moduleRepository.save(mod2);

        Lesson lesson3 = new Lesson();
        lesson3.setTitle("Java Collections Framework");
        lesson3.setContent("Overview of ArrayList, LinkedList, HashSet, TreeSet, and HashMap implementations.");
        lesson3.setModule(mod2);
        lesson3.setOrderIndex(1);
        lesson3 = lessonRepository.save(lesson3);

        // Quiz for Course 1
        Quiz quiz1 = new Quiz();
        quiz1.setTitle("Java OOP Concepts Quiz");
        quiz1.setDescription("Assess your understanding of Java Object-Oriented Programming");
        quiz1.setCourse(course1);
        quiz1.setTotalMarks(20);
        quiz1.setActive(true);
        quiz1 = quizRepository.save(quiz1);

        // Quiz Question 1
        QuizQuestion q1 = new QuizQuestion();
        q1.setQuestionText("Which keyword is used to inherit a class in Java?");
        q1.setMarks(10);
        q1.setQuiz(quiz1);
        q1 = quizQuestionRepository.save(q1);

        QuizOption opt1 = new QuizOption();
        opt1.setOptionText("extends");
        opt1.setCorrect(true);
        opt1.setQuestion(q1);
        quizOptionRepository.save(opt1);

        QuizOption opt2 = new QuizOption();
        opt2.setOptionText("implements");
        opt2.setCorrect(false);
        opt2.setQuestion(q1);
        quizOptionRepository.save(opt2);

        QuizOption opt3 = new QuizOption();
        opt3.setOptionText("inherits");
        opt3.setCorrect(false);
        opt3.setQuestion(q1);
        quizOptionRepository.save(opt3);

        QuizOption opt4 = new QuizOption();
        opt4.setOptionText("super");
        opt4.setCorrect(false);
        opt4.setQuestion(q1);
        quizOptionRepository.save(opt4);

        // Quiz Question 2
        QuizQuestion q2 = new QuizQuestion();
        q2.setQuestionText("Which of the following is NOT a Java primitive data type?");
        q2.setMarks(10);
        q2.setQuiz(quiz1);
        q2 = quizQuestionRepository.save(q2);

        QuizOption opt5 = new QuizOption();
        opt5.setOptionText("String");
        opt5.setCorrect(true);
        opt5.setQuestion(q2);
        quizOptionRepository.save(opt5);

        QuizOption opt6 = new QuizOption();
        opt6.setOptionText("int");
        opt6.setCorrect(false);
        opt6.setQuestion(q2);
        quizOptionRepository.save(opt6);

        QuizOption opt7 = new QuizOption();
        opt7.setOptionText("boolean");
        opt7.setCorrect(false);
        opt7.setQuestion(q2);
        quizOptionRepository.save(opt7);

        // Assignment for Course 1
        Assignment assignment1 = new Assignment();
        assignment1.setTitle("Core Java Banking Console Application");
        assignment1.setDescription("Build a simple CLI banking program with Account, Customer, and Transaction classes.");
        assignment1.setCourse(course1);
        assignment1.setDeadline(LocalDateTime.now().plusDays(14));
        assignment1.setMaxMarks(100);
        assignmentRepository.save(assignment1);

        // 3. Course 2: Spring Boot REST API Development
        Course course2 = new Course();
        course2.setTitle("Spring Boot REST API Development");
        course2.setDescription("Learn production-ready RESTful web services with Spring Boot, JPA, Security, and MariaDB.");
        course2.setTeacher(teacher);
        course2.setActive(true);
        course2 = courseRepository.save(course2);

        Module sbMod1 = new Module();
        sbMod1.setTitle("Spring Boot Architecture");
        sbMod1.setDescription("Inversion of Control, Dependency Injection, and Autowiring");
        sbMod1.setCourse(course2);
        sbMod1.setOrderIndex(1);
        sbMod1 = moduleRepository.save(sbMod1);

        Lesson sbLesson1 = new Lesson();
        sbLesson1.setTitle("Spring Boot Starter and AutoConfiguration");
        sbLesson1.setContent("Spring Boot starters package common dependencies and autoconfigure beans based on classpath.");
        sbLesson1.setModule(sbMod1);
        sbLesson1.setOrderIndex(1);
        lessonRepository.save(sbLesson1);

        // 4. Course 3: MariaDB & Database Systems
        Course course3 = new Course();
        course3.setTitle("MariaDB & Database Systems");
        course3.setDescription("Relational database design, indexes, transactions, and foreign keys in MariaDB.");
        course3.setTeacher(teacher);
        course3.setActive(true);
        courseRepository.save(course3);

        // 5. Initial Enrollment & Progress for Student 1 in Course 1
        Enrollment enrollment1 = new Enrollment(student1, course1);
        enrollment1.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(enrollment1);

        LessonProgress progress1 = new LessonProgress(student1, lesson1);
        lessonProgressRepository.save(progress1);
    }
}
