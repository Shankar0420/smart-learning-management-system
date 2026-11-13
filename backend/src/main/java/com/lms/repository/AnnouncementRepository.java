package com.lms.repository;

import com.lms.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @Query("SELECT a FROM Announcement a WHERE a.course IS NULL ORDER BY a.createdAt DESC")
    List<Announcement> findUniversityWideAnnouncements();

    @Query("SELECT a FROM Announcement a WHERE a.course IS NULL OR a.course.id IN :courseIds ORDER BY a.createdAt DESC")
    List<Announcement> findForStudentCourses(@Param("courseIds") List<Long> courseIds);

    List<Announcement> findAllByOrderByCreatedAtDesc();
}

