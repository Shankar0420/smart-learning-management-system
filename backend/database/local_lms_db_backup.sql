-- MySQL dump 10.13  Distrib 26.7.0, for macos26.6 (arm64)
--
-- Host: localhost    Database: lms_db
-- ------------------------------------------------------
-- Server version	26.7.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '115430ea-bdba-11f1-95ff-4ffaf8ecb6cb:1-292';

--
-- Table structure for table `assignment_submissions`
--

DROP TABLE IF EXISTS `assignment_submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignment_submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `answer_text` text NOT NULL,
  `marks_obtained` int DEFAULT NULL,
  `feedback` varchar(1000) DEFAULT NULL,
  `graded` tinyint(1) NOT NULL DEFAULT '0',
  `submitted_at` datetime NOT NULL,
  `graded_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_assignment` (`student_id`,`assignment_id`),
  UNIQUE KEY `UKjpaoiqlq2bm3rv52lcri47g4s` (`student_id`,`assignment_id`),
  KEY `fk_submission_assignment` (`assignment_id`),
  CONSTRAINT `fk_submission_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_submission_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignment_submissions`
--

LOCK TABLES `assignment_submissions` WRITE;
/*!40000 ALTER TABLE `assignment_submissions` DISABLE KEYS */;
INSERT INTO `assignment_submissions` VALUES (1,2,3,'Implemented findByTitleContainingIgnoreCase and custom DTO projection interfaces.',48,'Excellent query optimizations!',1,'2026-10-24 23:42:04','2026-10-24 23:42:05'),(2,3,3,'Implemented findByTitleContainingIgnoreCase and custom DTO projection interfaces.',48,'Excellent query optimizations!',1,'2026-10-24 23:42:35','2026-10-24 23:42:35'),(3,4,3,'Implemented findByTitleContainingIgnoreCase and custom DTO projection interfaces.',48,'Excellent query optimizations!',1,'2026-10-24 23:42:57','2026-10-24 23:42:57'),(4,5,3,'Implemented findByTitleContainingIgnoreCase and custom DTO projection interfaces.',48,'Excellent query optimizations!',1,'2026-10-24 23:46:30','2026-10-24 23:46:30');
/*!40000 ALTER TABLE `assignment_submissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assignments`
--

DROP TABLE IF EXISTS `assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `deadline` datetime NOT NULL,
  `max_marks` int NOT NULL DEFAULT '100',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_assignment_course` (`course_id`),
  CONSTRAINT `fk_assignment_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignments`
--

LOCK TABLES `assignments` WRITE;
/*!40000 ALTER TABLE `assignments` DISABLE KEYS */;
INSERT INTO `assignments` VALUES (1,'Core Java Banking Console Application','Build a simple CLI banking program with Account, Customer, and Transaction classes.',1,'2026-11-07 23:41:09',100,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(2,'Spring Data JPA Custom Queries','Implement repository methods using @Query and projections.',1,'2026-10-31 23:42:04',50,'2026-10-24 23:42:04','2026-10-24 23:42:04'),(3,'Spring Data JPA Custom Queries','Implement repository methods using @Query and projections.',1,'2026-10-31 23:42:35',50,'2026-10-24 23:42:35','2026-10-24 23:42:35'),(4,'Spring Data JPA Custom Queries','Implement repository methods using @Query and projections.',1,'2026-10-31 23:42:57',50,'2026-10-24 23:42:57','2026-10-24 23:42:57'),(5,'Spring Data JPA Custom Queries','Implement repository methods using @Query and projections.',1,'2026-10-31 23:46:30',50,'2026-10-24 23:46:30','2026-10-24 23:46:30');
/*!40000 ALTER TABLE `assignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `description` text NOT NULL,
  `teacher_id` bigint NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_course_teacher` (`teacher_id`),
  CONSTRAINT `fk_course_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES (1,'Java Programming Masterclass','Comprehensive Java course covering OOP, Collections, Concurrency, and Modern Java Features.',2,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(2,'Spring Boot REST API Development','Learn production-ready RESTful web services with Spring Boot, JPA, Security, and MariaDB.',2,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(3,'MariaDB & Database Systems','Relational database design, indexes, transactions, and foreign keys in MariaDB.',2,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(4,'Full Stack Cloud Engineering','Master scalable microservices and cloud deployments.',2,1,'2026-10-24 23:42:04','2026-10-24 23:42:04'),(5,'Full Stack Cloud Engineering','Master scalable microservices and cloud deployments.',2,1,'2026-10-24 23:42:35','2026-10-24 23:42:35'),(6,'Full Stack Cloud Engineering','Master scalable microservices and cloud deployments.',2,1,'2026-10-24 23:42:56','2026-10-24 23:42:56'),(7,'Full Stack Cloud Engineering','Master scalable microservices and cloud deployments.',2,1,'2026-10-24 23:46:29','2026-10-24 23:46:29');
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `status` enum('ACTIVE','COMPLETED','DROPPED') NOT NULL,
  `enrolled_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_course` (`student_id`,`course_id`),
  UNIQUE KEY `UKi0g6mfijtuh199nj653nva6j5` (`student_id`,`course_id`),
  KEY `fk_enrollment_course` (`course_id`),
  CONSTRAINT `fk_enrollment_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_enrollment_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollments`
--

LOCK TABLES `enrollments` WRITE;
/*!40000 ALTER TABLE `enrollments` DISABLE KEYS */;
INSERT INTO `enrollments` VALUES (1,3,1,'ACTIVE','2026-10-24 23:41:09');
/*!40000 ALTER TABLE `enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grades` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `marks` double NOT NULL,
  `total_marks` double NOT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_grade_student` (`student_id`),
  KEY `fk_grade_course` (`course_id`),
  CONSTRAINT `fk_grade_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_grade_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grades`
--

LOCK TABLES `grades` WRITE;
/*!40000 ALTER TABLE `grades` DISABLE KEYS */;
INSERT INTO `grades` VALUES (1,3,1,48,50,'Spring Data JPA Custom Queries: Excellent query optimizations!','2026-10-24 23:42:05','2026-10-24 23:42:05'),(2,3,1,48,50,'Spring Data JPA Custom Queries: Excellent query optimizations!','2026-10-24 23:42:35','2026-10-24 23:42:35'),(3,3,1,48,50,'Spring Data JPA Custom Queries: Excellent query optimizations!','2026-10-24 23:42:57','2026-10-24 23:42:57'),(4,3,1,48,50,'Spring Data JPA Custom Queries: Excellent query optimizations!','2026-10-24 23:46:30','2026-10-24 23:46:30');
/*!40000 ALTER TABLE `grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lesson_progress`
--

DROP TABLE IF EXISTS `lesson_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lesson_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `lesson_id` bigint NOT NULL,
  `completed` tinyint(1) NOT NULL DEFAULT '0',
  `completed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_lesson` (`student_id`,`lesson_id`),
  UNIQUE KEY `UKe61llilib3ygeij2bcjle9w1l` (`student_id`,`lesson_id`),
  KEY `fk_progress_lesson` (`lesson_id`),
  CONSTRAINT `fk_progress_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_progress_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lesson_progress`
--

LOCK TABLES `lesson_progress` WRITE;
/*!40000 ALTER TABLE `lesson_progress` DISABLE KEYS */;
INSERT INTO `lesson_progress` VALUES (1,3,1,1,'2026-10-24 23:41:09','2026-10-24 23:41:09');
/*!40000 ALTER TABLE `lesson_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lessons`
--

DROP TABLE IF EXISTS `lessons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lessons` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `module_id` bigint NOT NULL,
  `order_index` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_lesson_module` (`module_id`),
  CONSTRAINT `fk_lesson_module` FOREIGN KEY (`module_id`) REFERENCES `modules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lessons`
--

LOCK TABLES `lessons` WRITE;
/*!40000 ALTER TABLE `lessons` DISABLE KEYS */;
INSERT INTO `lessons` VALUES (1,'Classes, Objects, and Inheritance','Classes define blueprint for objects. Inheritance enables code reuse using the extends keyword.',1,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(2,'Polymorphism and Interfaces','Polymorphism allows methods to perform different tasks based on object type. Interfaces define contracts.',1,2,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(3,'Java Collections Framework','Overview of ArrayList, LinkedList, HashSet, TreeSet, and HashMap implementations.',2,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(4,'Spring Boot Starter and AutoConfiguration','Spring Boot starters package common dependencies and autoconfigure beans based on classpath.',3,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(5,'Service Discovery and API Gateways','Detailed guide on configuring Spring Cloud Gateway and Eureka.',4,1,'2026-10-24 23:42:04','2026-10-24 23:42:04'),(6,'Service Discovery and API Gateways','Detailed guide on configuring Spring Cloud Gateway and Eureka.',5,1,'2026-10-24 23:42:35','2026-10-24 23:42:35'),(7,'Service Discovery and API Gateways','Detailed guide on configuring Spring Cloud Gateway and Eureka.',6,1,'2026-10-24 23:42:56','2026-10-24 23:42:56'),(8,'Service Discovery and API Gateways','Detailed guide on configuring Spring Cloud Gateway and Eureka.',7,1,'2026-10-24 23:46:29','2026-10-24 23:46:29');
/*!40000 ALTER TABLE `lessons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modules`
--

DROP TABLE IF EXISTS `modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `modules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `order_index` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_module_course` (`course_id`),
  CONSTRAINT `fk_module_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modules`
--

LOCK TABLES `modules` WRITE;
/*!40000 ALTER TABLE `modules` DISABLE KEYS */;
INSERT INTO `modules` VALUES (1,'Object-Oriented Programming','Core OOP principles in Java: Encapsulation, Inheritance, Polymorphism, Abstraction',1,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(2,'Collections and Exception Handling','Working with List, Set, Map, and robust try-catch mechanisms',1,2,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(3,'Spring Boot Architecture','Inversion of Control, Dependency Injection, and Autowiring',2,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(4,'Introduction to Microservices','Architecture patterns and design.',4,1,'2026-10-24 23:42:04','2026-10-24 23:42:04'),(5,'Introduction to Microservices','Architecture patterns and design.',5,1,'2026-10-24 23:42:35','2026-10-24 23:42:35'),(6,'Introduction to Microservices','Architecture patterns and design.',6,1,'2026-10-24 23:42:56','2026-10-24 23:42:56'),(7,'Introduction to Microservices','Architecture patterns and design.',7,1,'2026-10-24 23:46:29','2026-10-24 23:46:29');
/*!40000 ALTER TABLE `modules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_attempts`
--

DROP TABLE IF EXISTS `quiz_attempts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_attempts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quiz_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `score` int NOT NULL,
  `total_marks` int NOT NULL,
  `attempted_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_attempt_quiz` (`quiz_id`),
  KEY `fk_attempt_student` (`student_id`),
  CONSTRAINT `fk_attempt_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_attempt_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_attempts`
--

LOCK TABLES `quiz_attempts` WRITE;
/*!40000 ALTER TABLE `quiz_attempts` DISABLE KEYS */;
INSERT INTO `quiz_attempts` VALUES (1,2,3,10,10,'2026-10-24 23:42:05'),(2,3,3,10,10,'2026-10-24 23:42:36'),(3,4,3,10,10,'2026-10-24 23:42:57'),(4,5,3,10,10,'2026-10-24 23:46:30');
/*!40000 ALTER TABLE `quiz_attempts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_options`
--

DROP TABLE IF EXISTS `quiz_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_options` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `option_text` varchar(500) NOT NULL,
  `correct` tinyint(1) NOT NULL DEFAULT '0',
  `question_id` bigint NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_option_question` (`question_id`),
  CONSTRAINT `fk_option_question` FOREIGN KEY (`question_id`) REFERENCES `quiz_questions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_options`
--

LOCK TABLES `quiz_options` WRITE;
/*!40000 ALTER TABLE `quiz_options` DISABLE KEYS */;
INSERT INTO `quiz_options` VALUES (1,'extends',1,1,'2026-10-24 23:41:09'),(2,'implements',0,1,'2026-10-24 23:41:09'),(3,'inherits',0,1,'2026-10-24 23:41:09'),(4,'super',0,1,'2026-10-24 23:41:09'),(5,'String',1,2,'2026-10-24 23:41:09'),(6,'int',0,2,'2026-10-24 23:41:09'),(7,'boolean',0,2,'2026-10-24 23:41:09'),(8,'Runnable',1,3,'2026-10-24 23:42:05'),(9,'Callable',0,3,'2026-10-24 23:42:05'),(10,'Threadable',0,3,'2026-10-24 23:42:05'),(11,'Executable',0,3,'2026-10-24 23:42:05'),(12,'Runnable',1,4,'2026-10-24 23:42:36'),(13,'Callable',0,4,'2026-10-24 23:42:36'),(14,'Threadable',0,4,'2026-10-24 23:42:36'),(15,'Executable',0,4,'2026-10-24 23:42:36'),(16,'Runnable',1,5,'2026-10-24 23:42:57'),(17,'Callable',0,5,'2026-10-24 23:42:57'),(18,'Threadable',0,5,'2026-10-24 23:42:57'),(19,'Executable',0,5,'2026-10-24 23:42:57'),(20,'Runnable',1,6,'2026-10-24 23:46:30'),(21,'Callable',0,6,'2026-10-24 23:46:30'),(22,'Threadable',0,6,'2026-10-24 23:46:30'),(23,'Executable',0,6,'2026-10-24 23:46:30');
/*!40000 ALTER TABLE `quiz_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_questions`
--

DROP TABLE IF EXISTS `quiz_questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_questions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_text` varchar(1000) NOT NULL,
  `quiz_id` bigint NOT NULL,
  `marks` int NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_question_quiz` (`quiz_id`),
  CONSTRAINT `fk_question_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_questions`
--

LOCK TABLES `quiz_questions` WRITE;
/*!40000 ALTER TABLE `quiz_questions` DISABLE KEYS */;
INSERT INTO `quiz_questions` VALUES (1,'Which keyword is used to inherit a class in Java?',1,10,'2026-10-24 23:41:09'),(2,'Which of the following is NOT a Java primitive data type?',1,10,'2026-10-24 23:41:09'),(3,'Which interface must be implemented to create a thread using lambda?',2,10,'2026-10-24 23:42:05'),(4,'Which interface must be implemented to create a thread using lambda?',3,10,'2026-10-24 23:42:36'),(5,'Which interface must be implemented to create a thread using lambda?',4,10,'2026-10-24 23:42:57'),(6,'Which interface must be implemented to create a thread using lambda?',5,10,'2026-10-24 23:46:30');
/*!40000 ALTER TABLE `quiz_questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quizzes`
--

DROP TABLE IF EXISTS `quizzes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quizzes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `total_marks` int NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_quiz_course` (`course_id`),
  CONSTRAINT `fk_quiz_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quizzes`
--

LOCK TABLES `quizzes` WRITE;
/*!40000 ALTER TABLE `quizzes` DISABLE KEYS */;
INSERT INTO `quizzes` VALUES (1,'Java OOP Concepts Quiz','Assess your understanding of Java Object-Oriented Programming',1,20,1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(2,'Java Concurrency & Threads','Testing knowledge of multithreading in Java.',1,10,1,'2026-10-24 23:42:05','2026-10-24 23:42:05'),(3,'Java Concurrency & Threads','Testing knowledge of multithreading in Java.',1,10,1,'2026-10-24 23:42:35','2026-10-24 23:42:36'),(4,'Java Concurrency & Threads','Testing knowledge of multithreading in Java.',1,10,1,'2026-10-24 23:42:57','2026-10-24 23:42:57'),(5,'Java Concurrency & Threads','Testing knowledge of multithreading in Java.',1,10,1,'2026-10-24 23:46:30','2026-10-24 23:46:30');
/*!40000 ALTER TABLE `quizzes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','TEACHER','STUDENT') NOT NULL,
  `employee_id` varchar(50) DEFAULT NULL,
  `approval_status` enum('PENDING','APPROVED','REJECTED') NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `employee_id` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'System Admin','admin@lms.com','$2a$10$3omuBPp1H7OnkidT5qhP4OmDZiHThW.AGhbv6msDL/Rgn.qRiSe76','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(2,'Dr. Ramesh Sharma','teacher@lms.com','$2a$10$Vohx/ns2myWV2d3Ynsc2OO8jhj0FNieYnfnGkJ1G0/Y/ckLF/LHaG','TEACHER','EMP1001','APPROVED',1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(3,'Aman Kumar','student1@lms.com','$2a$10$2gzhH.C2mpOiwO7hKe0PwuhSmTcJOJZ8Q4gYwOq6qIeyoehL1gUMu','STUDENT',NULL,'APPROVED',1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(4,'Pooja Verma','student2@lms.com','$2a$10$yVGR7M.9y278redmO/MzzOvAXBvXuinSnxmo8tJG8ez2WgORDf6BG','STUDENT',NULL,'APPROVED',1,'2026-10-24 23:41:09','2026-10-24 23:41:09'),(5,'First Admin','dup.admin.1792865522199@lms.com','$2a$10$i0YchoaE5swzzD2F0WXEPe79AwVM4Ckig/vTgjdTMoQ8xXr2Lhcju','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:02','2026-10-24 23:42:02'),(6,'Dr. Academic Dean','dean.1792865522460@lms.com','$2a$10$BI1ZhAYJWJ4mgWFCHjgSwOtQ9wIwKnYP6y5oxEeHtUt.JSTfQ0eZO','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:03','2026-10-24 23:42:03'),(7,'Associate Dean Registrar','reg.admin.1792865522568@lms.com','$2a$10$lpDlRuju7dMtCxoynMMhr.SABeXUir0Z2QZfZqdsx88CbzYygsWI.','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:03','2026-10-24 23:42:03'),(8,'Teacher One','dup.1792865522741@galgotias.edu','$2a$10$WHfW3Cyqyd8fibEkzSbA4OWU5di/qKyYy0RQelsF/QzW.FzWvtYMq','TEACHER','EMP1-1792865522741','REJECTED',0,'2026-10-24 23:42:03','2026-11-01 19:55:35'),(9,'Teacher Alpha','alpha.1792865522829@galgotias.edu','$2a$10$1kt3zyHASircvBpimEG5Cu2eBHGnXehCT60kPR5CkWGB0THj1kE.6','TEACHER','EMP-SAME-1792865522829','PENDING',0,'2026-10-24 23:42:03','2026-10-24 23:42:03'),(10,'Prof. Rejected Candidate','reject.1792865522911@galgotias.edu','$2a$10$9pFvLT56HtCLiaNkbvtugePR6MWKnr8Rinq9rGNQj0CWEni7ZpFM2','TEACHER','EMP-REJ-1792865522911','REJECTED',0,'2026-10-24 23:42:03','2026-10-24 23:42:03'),(11,'Prof. Alok Nath','prof.1792865523147@galgotias.edu','$2a$10$Q81D.6Uerk2FnJYv8uo6EeOquLYPQGTJDiS438Wo2it1s8zm4fAUi','TEACHER','EMP-1792865523147','APPROVED',1,'2026-10-24 23:42:03','2026-10-24 23:42:03'),(12,'Neha Sharma','neha.1792865524229@lms.com','$2a$10$CT.yr.yPwE.LJsYmquTlL.fbsvfKZJ2unOCjFTeivluBbPkCf4EN6','STUDENT',NULL,'APPROVED',1,'2026-10-24 23:42:04','2026-10-24 23:42:04'),(13,'First Admin','dup.admin.1792865553160@lms.com','$2a$10$Nr7fTmtOOTDll9PDfYUmHuQFj5vg/E04jXRrOpYTAclD4VOGgchaK','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:33','2026-10-24 23:42:33'),(14,'Dr. Academic Dean','dean.1792865553417@lms.com','$2a$10$EgifV4WskK6gvNPUKvt4T.YV0pb4veOTuJmBX1gJdizJR03RliwRu','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:34','2026-10-24 23:42:34'),(15,'Associate Dean Registrar','reg.admin.1792865553525@lms.com','$2a$10$DKCo7FSTIe7CWu7lyurLrO0HECaQlnYcaem8PVLxqzjDciGA8RRk6','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:34','2026-10-24 23:42:34'),(16,'Teacher One','dup.1792865553698@galgotias.edu','$2a$10$HcNa/mp1YBnPOFt1ffeVQOs.vyEg3b9mNu3SJIarK.6fdF4SDs6mq','TEACHER','EMP1-1792865553698','PENDING',0,'2026-10-24 23:42:34','2026-10-24 23:42:34'),(17,'Teacher Alpha','alpha.1792865553799@galgotias.edu','$2a$10$SNWeq4/Dd/m6cuYFlxj14OTaxd1e0fkqQxRtEjXqyjcmFDWIMEv3G','TEACHER','EMP-SAME-1792865553799','PENDING',0,'2026-10-24 23:42:34','2026-10-24 23:42:34'),(18,'Prof. Rejected Candidate','reject.1792865553880@galgotias.edu','$2a$10$gXmVKXBCxg3EwS6QMVyTVumOEEoZSc/i1Id92f.69Ylgdp7D7o4Ji','TEACHER','EMP-REJ-1792865553880','REJECTED',0,'2026-10-24 23:42:34','2026-10-24 23:42:34'),(19,'Prof. Alok Nath','prof.1792865554121@galgotias.edu','$2a$10$zBjl7jq9gv9COtfL/DjQGuPbha4/7yyIvP0BN2bepoLjXY5vZoxC2','TEACHER','EMP-1792865554121','APPROVED',1,'2026-10-24 23:42:34','2026-10-24 23:42:34'),(20,'Neha Sharma','neha.1792865555179@lms.com','$2a$10$.lLWDwDEEiXYX2mH4bvROeVMg2XocEZz1YlZGDNapi72zCwmo9fPm','STUDENT',NULL,'APPROVED',1,'2026-10-24 23:42:35','2026-10-24 23:42:35'),(21,'First Admin','dup.admin.1792865574435@lms.com','$2a$10$mdUkp0iSVw2g5OfjBE0aXuIapdQzrf5T3U67wz3YhxjWca9EOXITa','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:55','2026-10-24 23:42:55'),(22,'Dr. Academic Dean','dean.1792865574689@lms.com','$2a$10$NVeG1i7GsMQ8JpOu1hOUcu7xsytQK3grjxYHlbMe77iY2j06Mteiu','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:55','2026-10-24 23:42:55'),(23,'Associate Dean Registrar','reg.admin.1792865574806@lms.com','$2a$10$KLpXObZH2erygPzO7MTJ5eE2lyBYFSivq2guNjjppG88zAUhVVISm','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:42:55','2026-10-24 23:42:55'),(24,'Teacher One','dup.1792865574980@galgotias.edu','$2a$10$WhNFju2qz6b9guGRzjLzv.gWNwLXDWaPqroKkzjiQII4JX7Ywgzpu','TEACHER','EMP1-1792865574980','PENDING',0,'2026-10-24 23:42:55','2026-10-24 23:42:55'),(25,'Teacher Alpha','alpha.1792865575069@galgotias.edu','$2a$10$3zt/nb6yze36H.iNoeEfn.P6IMHol212pttHm14bwlbkmB1HeuTy.','TEACHER','EMP-SAME-1792865575069','PENDING',0,'2026-10-24 23:42:55','2026-10-24 23:42:55'),(26,'Prof. Rejected Candidate','reject.1792865575147@galgotias.edu','$2a$10$0l0LlPDaKMOvdmvyrcLRbehTI73XFossvePMWSbqdGua0TopASzLi','TEACHER','EMP-REJ-1792865575147','REJECTED',0,'2026-10-24 23:42:55','2026-10-24 23:42:55'),(27,'Prof. Alok Nath','prof.1792865575385@galgotias.edu','$2a$10$FB2eTVWA8RNSjHkbzdvuaef03xAfPOkXgcFgATQsYxz1BX3r6m17a','TEACHER','EMP-1792865575385','APPROVED',1,'2026-10-24 23:42:55','2026-10-24 23:42:56'),(28,'Neha Sharma','neha.1792865576385@lms.com','$2a$10$mT/Kl3ZWU/iognLYDTYFquV2t4H41QS2ZsdPus6uXCq.3b0/6iNCO','STUDENT',NULL,'APPROVED',1,'2026-10-24 23:42:56','2026-10-24 23:42:56'),(29,'First Admin','dup.admin.1792865787603@lms.com','$2a$10$nPagTN/CDQVTUaBKngRYN.vK82ahFJyawF8lunGyh.XxMJvEQA6FO','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:46:28','2026-10-24 23:46:28'),(30,'Dr. Academic Dean','dean.1792865787874@lms.com','$2a$10$cnY4OrPeB97wElXmIt8ygeuclE94cn6Usy.jtLWK0nG1kQsRt39Ha','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:46:28','2026-10-24 23:46:28'),(31,'Associate Dean Registrar','reg.admin.1792865787992@lms.com','$2a$10$iqC8aamSjA/Y1XFuyDUyFeTZNpZatUuyWaZisKJ/OjJBQF938FtrK','ADMIN',NULL,'APPROVED',1,'2026-10-24 23:46:28','2026-10-24 23:46:28'),(32,'Teacher One','dup.1792865788170@galgotias.edu','$2a$10$OhTo8GZZFRyxl6jLpJTlm.nD1dMuoHVl5LuHrtD8t8wTidS/qN6P6','TEACHER','EMP1-1792865788170','PENDING',0,'2026-10-24 23:46:28','2026-10-24 23:46:28'),(33,'Teacher Alpha','alpha.1792865788267@galgotias.edu','$2a$10$Ym3tXQHS4RCiXM3ZUxrCueh46O2NrL1CTrFHtB/flIM.B0.VHdOF.','TEACHER','EMP-SAME-1792865788267','PENDING',0,'2026-10-24 23:46:28','2026-10-24 23:46:28'),(34,'Prof. Rejected Candidate','reject.1792865788352@galgotias.edu','$2a$10$J0NCGSAE5uxI4Rxej4fwC.Nzkyv8irf0v2vFTJznwdgALQiG2mcBK','TEACHER','EMP-REJ-1792865788352','REJECTED',0,'2026-10-24 23:46:28','2026-10-24 23:46:29'),(35,'Prof. Alok Nath','prof.1792865788592@galgotias.edu','$2a$10$3.2xoPXxDNVGtE66iOZ.xuozquJWeeuRJYK3Qize5ngnMF8NmNHN6','TEACHER','EMP-1792865788592','APPROVED',1,'2026-10-24 23:46:29','2026-10-24 23:46:29'),(36,'Neha Sharma','neha.1792865789595@lms.com','$2a$10$HusbKyPd0E2P09EvpzGL1uDL/1QCBqtaFY13a3v4gKBW7peeWoFfq','STUDENT',NULL,'APPROVED',1,'2026-10-24 23:46:30','2026-10-24 23:46:30'),(37,'Shankar Chaudhary','choudharyshankar0420@gmail.com','$2a$10$ly2CMKMdHIkCNR6LxN8NYO4uSTZiFlpsr62Io75o.kwEHoe9GDgga','ADMIN',NULL,'APPROVED',1,'2026-10-25 23:25:20','2026-10-25 23:25:20');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-07 18:29:13
