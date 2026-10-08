package com.lms.repository;

import com.lms.entity.Enrollment;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @EntityGraph(attributePaths = {"student", "course", "course.teacher"})
    List<Enrollment> findByStudent(User student);

    @EntityGraph(attributePaths = {"student", "course", "course.teacher"})
    List<Enrollment> findByStudentId(Long studentId);

    long countByStudentId(Long studentId);

    long countByCourseId(Long courseId);

    long countByCourseIdIn(List<Long> courseIds);
}
