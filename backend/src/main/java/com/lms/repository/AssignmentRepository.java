package com.lms.repository;

import com.lms.entity.Assignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @EntityGraph(attributePaths = {"course", "course.teacher"})
    Optional<Assignment> findById(Long id);

    @EntityGraph(attributePaths = {"course", "course.teacher"})
    List<Assignment> findByCourseId(Long courseId);
}
