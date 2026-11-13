package com.lms.repository;

import com.lms.entity.AcademicCalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicCalendarRepository extends JpaRepository<AcademicCalendarEvent, Long> {

    @Query("SELECT e FROM AcademicCalendarEvent e WHERE e.course IS NULL OR e.course.id IN :courseIds ORDER BY e.startDate ASC")
    List<AcademicCalendarEvent> findRelevantEvents(@Param("courseIds") List<Long> courseIds);

    List<AcademicCalendarEvent> findByOrderByStartDateAsc();

    List<AcademicCalendarEvent> findByCourseIdOrderByStartDateAsc(Long courseId);
}

