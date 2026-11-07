package com.lms.service;

import com.lms.dto.MessageResponse;
import com.lms.dto.ResetPasswordRequest;
import com.lms.entity.PasswordResetToken;
import com.lms.entity.User;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.PasswordResetTokenRepository;
import com.lms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public MessageResponse initiatePasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Clear any active tokens for this user
            tokenRepository.deleteByUser(user);

            // Generate single-use secure token valid for 30 minutes
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user, 30);
            tokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/pages/reset-password.html?token=" + token;
            log.info("Password reset initiated for user id {}. Link: {}", user.getId(), resetLink);
        }

        // Generic security response: never reveal whether user exists
        return new MessageResponse("If your email is registered in Galgotias LMS, instructions to reset your password have been issued.");
    }

    @Transactional(readOnly = true)
    public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) return false;
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        PasswordResetToken resetToken = tokenOpt.get();
        return !resetToken.isUsed() && !resetToken.isExpired();
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new BadRequestException("Password reset token is required");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("This password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new BadRequestException("This password reset token has expired. Please request a new one");
        }

        User user = resetToken.getUser();

        // 1. Update and hash password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 2. Invalidate all active JWT tokens by incrementing tokenVersion
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        // 3. Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user id {}", user.getId());
        return new MessageResponse("Your password has been successfully reset. You may now log in with your new credentials.");
    }
}

