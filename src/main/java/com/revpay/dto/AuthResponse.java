package com.revpay.dto;

import com.revpay.entity.User;

public class AuthResponse {
    
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private User.UserRole role;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String email, String fullName, User.UserRole role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public User.UserRole getRole() { return role; }
    public void setRole(User.UserRole role) { this.role = role; }
}
