package com.lms.repository;

import com.lms.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @EntityGraph(attributePaths = {"student", "assignment"})
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @EntityGraph(attributePaths = {"student", "assignment"})
    Optional<AssignmentSubmission> findById(Long id);

    @EntityGraph(attributePaths = {"student", "assignment"})
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    @EntityGraph(attributePaths = {"student", "assignment"})
    List<AssignmentSubmission> findByStudentId(Long studentId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.graded = false " +
           "AND s.assignment.course.teacher.id = :teacherId")
    long countPendingByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.student.id = :studentId AND s.graded = false")
    long countPendingByStudentId(@Param("studentId") Long studentId);
}
