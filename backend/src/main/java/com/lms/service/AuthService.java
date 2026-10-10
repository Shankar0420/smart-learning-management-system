package com.lms.service;

import com.lms.dto.AdminRegisterRequest;
import com.lms.dto.AuthResponse;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.dto.TeacherRegisterRequest;
import com.lms.dto.UserResponse;
import com.lms.entity.ApprovalStatus;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.BadRequestException;
import com.lms.exception.ConflictException;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.exception.UnauthorizedException;
import com.lms.repository.UserRepository;
import com.lms.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${app.admin.registration-key:ADMIN_SECRET_KEY_2026_GALGOTIAS}")
    private String adminRegistrationKey;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered: " + request.getEmail());
        }

        User user = new User(
                request.getName().trim(),
                request.getEmail().trim(),
                passwordEncoder.encode(request.getPassword()),
                Role.STUDENT
        );
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setActive(true);

        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    @Transactional
    public UserResponse registerTeacher(TeacherRegisterRequest request) {
        String email = request.getEmail().trim();
        String employeeId = request.getEmployeeId().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        if (userRepository.existsByEmployeeId(employeeId)) {
            throw new ConflictException("Employee ID is already registered: " + employeeId);
        }

        User user = new User(
                request.getName().trim(),
                email,
                passwordEncoder.encode(request.getPassword()),
                Role.TEACHER,
                employeeId,
                ApprovalStatus.PENDING
        );
        user.setActive(false);

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.getEmployeeId(),
                saved.getApprovalStatus().name(),
                saved.isActive(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public UserResponse registerAdmin(AdminRegisterRequest request) {
        if (request.getRegistrationKey() == null || !adminRegistrationKey.equals(request.getRegistrationKey().trim())) {
            throw new ForbiddenException("Invalid administrator registration key");
        }

        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        User user = new User(
                request.getName().trim(),
                email,
                passwordEncoder.encode(request.getPassword()),
                Role.ADMIN
        );
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setActive(true);

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.isActive(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public UserResponse createAdminByAdmin(AdminRegisterRequest request) {
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        User user = new User(
                request.getName().trim(),
                email,
                passwordEncoder.encode(request.getPassword()),
                Role.ADMIN
        );
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setActive(true);

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.isActive(),
                saved.getCreatedAt()
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Check teacher approval status before allowing access
        if (user.getRole() == Role.TEACHER) {
            if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
                throw new UnauthorizedException("Your teacher registration is pending approval by an administrator.");
            }
            if (user.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new UnauthorizedException("Your teacher registration has been rejected. Please contact administration.");
            }
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Account has been deactivated. Please contact admin.");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
