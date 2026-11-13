package com.lms.repository;

import com.lms.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByCourseIdOrderBySessionDateDesc(Long courseId);
    List<AttendanceSession> findByTeacherIdOrderBySessionDateDesc(Long teacherId);
    Optional<AttendanceSession> findByCourseIdAndSessionDate(Long courseId, LocalDate sessionDate);
}

