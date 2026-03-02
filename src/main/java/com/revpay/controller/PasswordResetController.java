package com.revpay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.revpay.service.UserService;
import com.revpay.entity.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    // Store reset codes temporarily (in production, use Redis or database)
    private static final Map<String, String> resetCodes = new HashMap<>();
    private static final Map<String, Long> resetCodeTimestamps = new HashMap<>();

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("username"); // Frontend sends username but we treat it as email
        
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check if user exists
        User user = userService.findByEmail(email);
        
        // Generate a reset code even if user doesn't exist (security best practice)
        String resetCode = generateResetCode();
        resetCodes.put(email, resetCode);
        resetCodeTimestamps.put(email, System.currentTimeMillis());
        
        // Log to console for development (as requested)
        System.out.println("=== PASSWORD RESET CODE ===");
        System.out.println("Email: " + email);
        System.out.println("Reset Code: " + resetCode);
        System.out.println("Generated at: " + new java.util.Date());
        System.out.println("========================");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "If the email exists, a reset code has been generated");
        response.put("developmentCode", resetCode); // Only for development
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("username"); // Frontend sends username but we treat it as email
        String resetCode = request.get("resetCode");
        String newPassword = request.get("newPassword");
        
        if (email == null || resetCode == null || newPassword == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "All fields are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate reset code
        String storedCode = resetCodes.get(email);
        Long timestamp = resetCodeTimestamps.get(email);
        
        if (storedCode == null || !storedCode.equals(resetCode)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid reset code");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check if code is expired (15 minutes)
        if (System.currentTimeMillis() - timestamp > 15 * 60 * 1000) {
            resetCodes.remove(email);
            resetCodeTimestamps.remove(email);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Reset code has expired");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Update password
        try {
            User user = userService.findByEmail(email);
            if (user != null) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userService.saveUser(user);
                
                // Clean up reset code
                resetCodes.remove(email);
                resetCodeTimestamps.remove(email);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Password reset successfully");
                return ResponseEntity.ok(response);
            }
        } catch (IllegalArgumentException e) {
            // User doesn't exist, but don't reveal this for security
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Unable to reset password");
        return ResponseEntity.badRequest().body(response);
    }
    
    private String generateResetCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
