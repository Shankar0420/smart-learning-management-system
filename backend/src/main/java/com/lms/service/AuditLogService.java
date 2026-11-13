package com.lms.service;

import com.lms.dto.AuditLogResponse;
import com.lms.entity.AuditLog;
import com.lms.entity.User;
import com.lms.repository.AuditLogRepository;
import com.lms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String userEmail, String action, String entityType, String entityId, String details, String ipAddress) {
        try {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            AuditLog auditLog = new AuditLog(user, userEmail != null ? userEmail : "SYSTEM", action, entityType, entityId, details, ipAddress);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to persist audit log: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUser() != null ? log.getUser().getId() : null,
                log.getUserEmail(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}

