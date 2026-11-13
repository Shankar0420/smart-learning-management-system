package com.lms.service;

import com.lms.dto.NotificationResponse;
import com.lms.entity.Notification;
import com.lms.entity.User;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.NotificationRepository;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<NotificationResponse> getUserNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        return notificationRepository.countByUserIdAndReadStatusFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, String userEmail) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (!notif.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found for current user");
        }

        notif.setReadStatus(true);
        notificationRepository.save(notif);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        notificationRepository.markAllAsReadForUser(user.getId());
    }

    @Transactional
    public void sendNotification(User user, String title, String message, String category, String link) {
        Notification notification = new Notification(user, title, message, category, link);
        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getCategory(),
                n.isReadStatus(),
                n.getLink(),
                n.getCreatedAt()
        );
    }
}

