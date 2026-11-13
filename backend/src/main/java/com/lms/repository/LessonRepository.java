package com.lms.repository;

import com.lms.entity.Lesson;
import com.lms.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModuleOrderByOrderIndexAsc(Module module);

    List<Lesson> findByModuleIdOrderByOrderIndexAsc(Long moduleId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId")
    List<Lesson> findByCourseId(@Param("courseId") Long courseId);
}
