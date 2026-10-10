-- ===================================================
-- Smart Learning Management System (LMS)
-- Seed Development Data for MariaDB
-- Database: lms_db
-- ===================================================

USE lms_db;

-- 1. Users (BCrypt password hash for: 'admin123', 'teacher123', 'student123')
-- Password for admin123: $2a$10$w8uD94mYxZepJ0XvNfM2I.kY8y5j2/Q6q1R9F3bIqWJmE0sVd7e6u
-- Password for teacher123: $2a$10$7vNnJtVqHkR1qEa0Ld5H3.G0jQZ3hF7sL2bW8mI6kX4uY9vC8pA1e
-- Password for student123: $2a$10$R9nQ4hE7mJ2yU8vB5kL1z.X0wA2dC4fG6hJ8kL0mN2pQ4rS6tU8vW

INSERT INTO users (id, name, email, password, role, employee_id, approval_status, active, created_at, updated_at) VALUES
(1, 'System Admin', 'admin@lms.com', '$2a$10$3omuBPp1H7OnkidT5qhP4OmDZiHThW.AGhbv6msDL/Rgn.qRiSe76', 'ADMIN', NULL, 'APPROVED', 1, NOW(), NOW()),
(2, 'Dr. Ramesh Sharma', 'teacher@lms.com', '$2a$10$Vohx/ns2myWV2d3Ynsc2OO8jhj0FNieYnfnGkJ1G0/Y/ckLF/LHaG', 'TEACHER', 'EMP1001', 'APPROVED', 1, NOW(), NOW()),
(3, 'Aman Kumar', 'student1@lms.com', '$2a$10$2gzhH.C2mpOiwO7hKe0PwuhSmTcJOJZ8Q4gYwOq6qIeyoehL1gUMu', 'STUDENT', NULL, 'APPROVED', 1, NOW(), NOW()),
(4, 'Pooja Verma', 'student2@lms.com', '$2a$10$yVGR7M.9y278redmO/MzzOvAXBvXuinSnxmo8tJG8ez2WgORDf6BG', 'STUDENT', NULL, 'APPROVED', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;


-- 2. Courses
INSERT INTO courses (id, title, description, teacher_id, active, created_at, updated_at) VALUES
(1, 'Java Programming Masterclass', 'Comprehensive Java course covering OOP, Collections, Concurrency, and Modern Java Features.', 2, 1, NOW(), NOW()),
(2, 'Spring Boot REST API Development', 'Learn production-ready RESTful web services with Spring Boot, JPA, Security, and MariaDB.', 2, 1, NOW(), NOW()),
(3, 'MariaDB & Database Systems', 'Relational database design, indexes, transactions, and foreign keys in MariaDB.', 2, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 3. Modules
INSERT INTO modules (id, title, description, course_id, order_index, created_at, updated_at) VALUES
(1, 'Object-Oriented Programming', 'Core OOP principles in Java: Encapsulation, Inheritance, Polymorphism, Abstraction', 1, 1, NOW(), NOW()),
(2, 'Collections and Exception Handling', 'Working with List, Set, Map, and robust try-catch mechanisms', 1, 2, NOW(), NOW()),
(3, 'Spring Boot Architecture', 'Inversion of Control, Dependency Injection, and Autowiring', 2, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 4. Lessons
INSERT INTO lessons (id, title, content, module_id, order_index, created_at, updated_at) VALUES
(1, 'Classes, Objects, and Inheritance', 'Classes define blueprint for objects. Inheritance enables code reuse using the extends keyword.', 1, 1, NOW(), NOW()),
(2, 'Polymorphism and Interfaces', 'Polymorphism allows methods to perform different tasks based on object type. Interfaces define contracts.', 1, 2, NOW(), NOW()),
(3, 'Java Collections Framework', 'Overview of ArrayList, LinkedList, HashSet, TreeSet, and HashMap implementations.', 2, 1, NOW(), NOW()),
(4, 'Spring Boot Starter and AutoConfiguration', 'Spring Boot starters package common dependencies and autoconfigure beans based on classpath.', 3, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 5. Enrollments
INSERT INTO enrollments (id, student_id, course_id, status, enrolled_at) VALUES
(1, 3, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 6. Quizzes
INSERT INTO quizzes (id, title, description, course_id, total_marks, active, created_at, updated_at) VALUES
(1, 'Java OOP Concepts Quiz', 'Assess your understanding of Java Object-Oriented Programming', 1, 20, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 7. Quiz Questions
INSERT INTO quiz_questions (id, question_text, quiz_id, marks, created_at) VALUES
(1, 'Which keyword is used to inherit a class in Java?', 1, 10, NOW()),
(2, 'Which of the following is NOT a Java primitive data type?', 1, 10, NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 8. Quiz Options
INSERT INTO quiz_options (id, option_text, correct, question_id, created_at) VALUES
(1, 'extends', 1, 1, NOW()),
(2, 'implements', 0, 1, NOW()),
(3, 'inherits', 0, 1, NOW()),
(4, 'super', 0, 1, NOW()),
(5, 'String', 1, 2, NOW()),
(6, 'int', 0, 2, NOW()),
(7, 'boolean', 0, 2, NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 9. Assignments
INSERT INTO assignments (id, title, description, course_id, deadline, max_marks, created_at, updated_at) VALUES
(1, 'Core Java Banking Console Application', 'Build a simple CLI banking program with Account, Customer, and Transaction classes.', 1, DATE_ADD(NOW(), INTERVAL 14 DAY), 100, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 10. Lesson Progress
INSERT INTO lesson_progress (id, student_id, lesson_id, completed, completed_at, created_at) VALUES
(1, 3, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;
