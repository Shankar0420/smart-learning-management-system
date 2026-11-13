package com.lms.repository;

import com.lms.entity.StudyMaterialBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMaterialBookmarkRepository extends JpaRepository<StudyMaterialBookmark, Long> {
    List<StudyMaterialBookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<StudyMaterialBookmark> findByUserIdAndMaterialId(Long userId, Long materialId);
    boolean existsByUserIdAndMaterialId(Long userId, Long materialId);
    void deleteByUserIdAndMaterialId(Long userId, Long materialId);
}

