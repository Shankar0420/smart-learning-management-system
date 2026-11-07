package com.lms.repository;

import com.lms.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    List<StudyMaterial> findByCourseId(Long courseId);
    List<StudyMaterial> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    List<StudyMaterial> findByUploadedById(Long teacherId);
    long countByCourseId(Long courseId);
    long countByCourseIdIn(List<Long> courseIds);
}

