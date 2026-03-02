package com.revpay.controller;

import com.revpay.entity.User;
import com.revpay.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getUserNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            List<Map<String, Object>> notifications = notificationService.getUserNotifications(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notifications
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/preferences")
    public ResponseEntity<?> getNotificationPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            Map<String, Object> preferences = notificationService.getNotificationPreferences(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "preferences", preferences
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updateNotificationPreferences(@AuthenticationPrincipal UserDetails userDetails,
                                                   @RequestBody Map<String, Object> preferences) {
        try {
            String email = userDetails.getUsername();
            notificationService.updateNotificationPreferences(email, preferences);
            return ResponseEntity.ok(Map.of("message", "Notification preferences updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/mark-read/{id}")
    public ResponseEntity<?> markNotificationAsRead(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long id) {
        try {
            String email = userDetails.getUsername();
            notificationService.markAsRead(email, id);
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
