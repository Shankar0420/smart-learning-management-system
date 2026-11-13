package com.lms.repository;

import com.lms.entity.DiscussionThread;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, Long> {

    @EntityGraph(attributePaths = {"author", "course"})
    List<DiscussionThread> findByCourseIdOrderByPinnedDescCreatedAtDesc(Long courseId);

    @EntityGraph(attributePaths = {"author", "course", "replies", "replies.author"})
    Optional<DiscussionThread> findWithRepliesById(Long id);
}

