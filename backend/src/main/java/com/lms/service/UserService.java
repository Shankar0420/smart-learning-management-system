package com.lms.service;

import com.lms.dto.UserResponse;
import com.lms.entity.ApprovalStatus;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfessionalIdService professionalIdService;

    public UserService(UserRepository userRepository, ProfessionalIdService professionalIdService) {
        this.userRepository = userRepository;
        this.professionalIdService = professionalIdService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getPendingTeachers() {
        return userRepository.findByRoleAndApprovalStatus(Role.TEACHER, ApprovalStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getPendingStudents() {
        return userRepository.findByRoleAndApprovalStatus(Role.STUDENT, ApprovalStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(query.trim()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse approveTeacher(Long teacherId) {
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        if (user.getRole() != Role.TEACHER) {
            throw new BadRequestException("User with id " + teacherId + " is not a teacher");
        }

        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setActive(true);

        // Concurrency-safe atomic generation of Teacher ID (GUL-TEA-2026-XXX)
        if (user.getProfessionalId() == null || user.getProfessionalId().trim().isEmpty()) {
            user.setProfessionalId(professionalIdService.generateNextId(Role.TEACHER));
        }

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    public UserResponse rejectTeacher(Long teacherId) {
        User user = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        if (user.getRole() != Role.TEACHER) {
            throw new BadRequestException("User with id " + teacherId + " is not a teacher");
        }

        user.setApprovalStatus(ApprovalStatus.REJECTED);
        user.setActive(false);
        user.setTokenVersion(user.getTokenVersion() + 1);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    public UserResponse approveStudent(Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("User with id " + studentId + " is not a student");
        }

        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setActive(true);

        // Concurrency-safe atomic generation of Student ID (GUL-STU-2026-XXX)
        if (user.getProfessionalId() == null || user.getProfessionalId().trim().isEmpty()) {
            user.setProfessionalId(professionalIdService.generateNextId(Role.STUDENT));
        }

        if (user.getEnrollmentNumber() == null || user.getEnrollmentNumber().trim().isEmpty()) {
            user.setEnrollmentNumber("ENR" + Year.now().getValue() + String.format("%03d", user.getId()));
        }

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    public UserResponse rejectStudent(Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("User with id " + studentId + " is not a student");
        }

        user.setApprovalStatus(ApprovalStatus.REJECTED);
        user.setActive(false);
        user.setTokenVersion(user.getTokenVersion() + 1);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    public UserResponse setActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Primary Administrator Protection: id=1 or email admin@lms.com cannot be deactivated
        if ((userId.equals(1L) || "admin@lms.com".equalsIgnoreCase(user.getEmail())) && !active) {
            throw new BadRequestException("Primary System Administrator account cannot be deactivated or locked.");
        }

        user.setActive(active);

        // When deactivating, increment tokenVersion to immediately revoke any active JWT sessions
        if (!active) {
            user.setTokenVersion(user.getTokenVersion() + 1);
        }

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    public UserResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    public UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getEmployeeId(),
                user.getProfessionalId(),
                user.getEnrollmentNumber(),
                user.getDepartment(),
                user.getApprovalStatus() != null ? user.getApprovalStatus().name() : ApprovalStatus.APPROVED.name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
