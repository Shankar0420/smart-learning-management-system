package com.lms.repository;

import com.lms.entity.Course;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findByTeacher(User teacher);

    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findByActiveTrue();

    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findAll();

    @EntityGraph(attributePaths = {"teacher"})
    Optional<Course> findById(Long id);

    long countByTeacher(User teacher);
}
