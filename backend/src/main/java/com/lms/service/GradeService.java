package com.lms.service;

import com.lms.dto.GradeResponse;
import com.lms.entity.Grade;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.GradeRepository;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;

    public GradeService(GradeRepository gradeRepository, UserRepository userRepository) {
        this.gradeRepository = gradeRepository;
        this.userRepository = userRepository;
    }

    public List<GradeResponse> getGradesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Grade> grades;
        if (user.getRole() == Role.STUDENT) {
            grades = gradeRepository.findByStudentId(user.getId());
        } else {
            grades = gradeRepository.findAll();
        }

        return grades.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public GradeResponse mapToResponse(Grade grade) {
        double percentage = grade.getTotalMarks() > 0
                ? (grade.getMarks() / grade.getTotalMarks()) * 100.0
                : 0.0;
        return new GradeResponse(
                grade.getId(),
                grade.getCourse().getId(),
                grade.getCourse().getTitle(),
                grade.getStudent().getId(),
                grade.getStudent().getName(),
                grade.getMarks(),
                grade.getTotalMarks(),
                Math.round(percentage * 10.0) / 10.0,
                grade.getRemarks(),
                grade.getCreatedAt()
        );
    }
}
