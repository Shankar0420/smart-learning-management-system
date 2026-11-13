package com.lms.repository;

import com.lms.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    List<StudyMaterial> findByCourseId(Long courseId);
    List<StudyMaterial> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    List<StudyMaterial> findByCourseIdAndPublishedTrueOrderByCreatedAtDesc(Long courseId);
    List<StudyMaterial> findByCourseIdInAndPublishedTrueOrderByCreatedAtDesc(List<Long> courseIds);
    List<StudyMaterial> findByUploadedByIdOrderByCreatedAtDesc(Long teacherId);
    List<StudyMaterial> findByUploadedById(Long teacherId);
    long countByCourseId(Long courseId);
    long countByCourseIdIn(List<Long> courseIds);

    @Query("SELECT m FROM StudyMaterial m WHERE m.course.id IN :courseIds AND m.published = true " +
           "AND (:category IS NULL OR m.category = :category) " +
           "AND (:semester IS NULL OR m.semester = :semester) " +
           "AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY m.createdAt DESC")
    List<StudyMaterial> searchMaterials(@Param("courseIds") List<Long> courseIds,
                                        @Param("category") String category,
                                        @Param("semester") String semester,
                                        @Param("keyword") String keyword);
}

