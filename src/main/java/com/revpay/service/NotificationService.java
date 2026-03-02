package com.revpay.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revpay.entity.Notification;
import com.revpay.entity.User;
import com.revpay.repository.NotificationRepository;
import com.revpay.repository.UserRepository;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Notification createNotification(User user, Notification.NotificationType type, String title, String message) {
        return createNotification(user, type, title, message, null);
    }

    public Notification createNotification(User user, Notification.NotificationType type, String title, String message, String referenceId) {
        logger.info("Creating notification for user {}: {}", user.getId(), title);

        Notification notification = new Notification(user, type, title, message, referenceId);
        notification = notificationRepository.save(notification);

        checkLowBalanceAlert(user);

        return notification;
    }

    private void checkLowBalanceAlert(User user) {
        if (user.getWallet() != null && user.getWallet().getBalance().compareTo(java.math.BigDecimal.valueOf(100.00)) < 0) {
            boolean hasRecentLowBalanceAlert = notificationRepository
                    .findByUserAndNotificationTypeAndCreatedAtAfter(
                            user, 
                            Notification.NotificationType.LOW_BALANCE, 
                            LocalDateTime.now().minusHours(24)
                    ).stream()
                    .anyMatch(n -> !n.isRead());

            if (!hasRecentLowBalanceAlert) {
                createNotification(
                        user,
                        Notification.NotificationType.LOW_BALANCE,
                        "Low Balance Alert",
                        "Your wallet balance is below $100.00. Consider adding funds to avoid transaction failures."
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // Initialize lazy-loaded User relationships to avoid LazyInitializationException
        for (Notification notification : notifications) {
            if (notification.getUser() != null) {
                notification.getUser().getId(); // Initialize User proxy safely
            }
        }
        
        return notifications;
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public void clearReadNotifications(Long userId) {
        List<Notification> readNotifications = notificationRepository
                .findByUserIdAndIsReadTrueOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(readNotifications);
    }

    // New methods for NotificationController
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserNotifications(String email) {
        logger.info("Getting notifications for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Initialize lazy-loaded User relationships to avoid LazyInitializationException
        if (user.getWallet() != null) {
            user.getWallet().getBalance(); // Initialize wallet proxy
        }
        
        List<Notification> notifications = getUserNotifications(user.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Notification notification : notifications) {
            Map<String, Object> notifMap = new HashMap<>();
            notifMap.put("id", notification.getId());
            notifMap.put("type", notification.getNotificationType().name());
            notifMap.put("title", notification.getTitle());
            notifMap.put("message", notification.getMessage());
            notifMap.put("createdAt", notification.getCreatedAt());
            notifMap.put("isRead", notification.isRead());
            
            // Initialize User proxy safely
            if (notification.getUser() != null) {
                notifMap.put("userId", notification.getUser().getId());
            } else {
                notifMap.put("userId", null);
            }
            
            result.add(notifMap);
        }
        
        logger.info("Found {} notifications for user {}", result.size(), email);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNotificationPreferences(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("emailNotifications", true);
        preferences.put("smsNotifications", false);
        preferences.put("pushNotifications", true);
        preferences.put("transactionAlerts", true);
        preferences.put("moneyRequestAlerts", true);
        return preferences;
    }

    @Transactional
    public void updateNotificationPreferences(String email, Map<String, Object> preferences) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Update notification preferences logic here
        // This would typically save to database or user preferences table
    }

    @Transactional
    public void markAsRead(String email, Long notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Notification does not belong to user");
        }
        
        markAsRead(notificationId);
    }
}
