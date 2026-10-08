package com.lms.repository;

import com.lms.entity.QuizAttempt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @EntityGraph(attributePaths = {"student", "quiz"})
    List<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);

    @EntityGraph(attributePaths = {"student", "quiz"})
    List<QuizAttempt> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"student", "quiz"})
    List<QuizAttempt> findAll();

    boolean existsByQuizIdAndStudentId(Long quizId, Long studentId);
}
