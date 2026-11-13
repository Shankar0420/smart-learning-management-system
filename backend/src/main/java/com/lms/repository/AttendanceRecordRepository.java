package com.lms.repository;

import com.lms.entity.AttendanceRecord;
import com.lms.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySessionId(Long sessionId);
    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, Long studentId);
    List<AttendanceRecord> findByStudentId(Long studentId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.student.id = :studentId AND r.session.course.id = :courseId")
    List<AttendanceRecord> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);
    long countByStudentId(Long studentId);

    @Query("SELECT COUNT(r) FROM AttendanceRecord r WHERE r.student.id = :studentId AND r.session.course.id = :courseId AND r.status = :status")
    long countByStudentIdAndCourseIdAndStatus(@Param("studentId") Long studentId, @Param("courseId") Long courseId, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(r) FROM AttendanceRecord r WHERE r.student.id = :studentId AND r.session.course.id = :courseId")
    long countByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}

