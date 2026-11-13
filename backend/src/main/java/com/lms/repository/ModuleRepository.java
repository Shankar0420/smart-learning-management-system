package com.lms.repository;

import com.lms.entity.Course;
import com.lms.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByCourseOrderByOrderIndexAsc(Course course);

    List<Module> findByCourseIdOrderByOrderIndexAsc(Long courseId);
}
