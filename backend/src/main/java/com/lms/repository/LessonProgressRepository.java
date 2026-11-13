package com.lms.repository;

import com.lms.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);

    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.student.id = :studentId " +
           "AND lp.lesson.module.course.id = :courseId AND lp.completed = true")
    long countCompletedByStudentAndCourse(@Param("studentId") Long studentId,
                                          @Param("courseId") Long courseId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.student.id = :studentId " +
           "AND lp.completed = true")
    long countCompletedByStudentId(@Param("studentId") Long studentId);
}
