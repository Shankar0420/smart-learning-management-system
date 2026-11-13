package com.lms.repository;

import com.lms.entity.Grade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    @EntityGraph(attributePaths = {"student", "course"})
    List<Grade> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<Grade> findAll();
}
