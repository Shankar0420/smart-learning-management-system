package com.lms.repository;

import com.lms.entity.FacultyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyDocumentRepository extends JpaRepository<FacultyDocument, Long> {
    List<FacultyDocument> findByFacultyIdOrderByCreatedAtDesc(Long facultyId);
}

