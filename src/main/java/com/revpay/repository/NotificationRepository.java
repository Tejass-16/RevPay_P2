package com.revpay.repository;

import com.revpay.entity.Notification;
import com.revpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndIsReadTrueOrderByCreatedAtDesc(Long userId);
    
    long countByUserIdAndIsReadFalse(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.notificationType = :type AND n.createdAt > :after")
    List<Notification> findByUserAndNotificationTypeAndCreatedAtAfter(@Param("user") User user, 
                                                                   @Param("type") Notification.NotificationType type, 
                                                                   @Param("after") LocalDateTime after);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.notificationType = :type ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndNotificationType(@Param("userId") Long userId, @Param("type") Notification.NotificationType type);
}
