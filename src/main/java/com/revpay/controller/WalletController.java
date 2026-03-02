package com.revpay.controller;

import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.UserRepository;
import com.revpay.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WalletController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @GetMapping("/balance")
    public ResponseEntity<?> getCurrentUserBalance(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Wallet wallet = walletRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
            
            return ResponseEntity.ok(Map.of(
                "balance", wallet.getBalance(),
                "accountNumber", wallet.getAccountNumber(),
                "userId", user.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Wallet wallet = walletRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
            
            Map<String, Object> response = new HashMap<>();
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            String fullName = user.getFullName();
            if (fullName != null && fullName.contains(" ")) {
                String[] names = fullName.split(" ", 2);
                userData.put("firstName", names[0]);
                userData.put("lastName", names[1]);
            } else {
                userData.put("firstName", fullName != null ? fullName : "");
                userData.put("lastName", "");
            }
            userData.put("email", user.getEmail());
            userData.put("phone", user.getPhone());
            userData.put("role", user.getUserRole().name());
            userData.put("isActive", user.isActive());
            userData.put("isVerified", user.isVerified());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("businessName", user.getBusinessName());
            userData.put("businessType", user.getBusinessType());
            userData.put("businessAddress", user.getBusinessAddress());
            userData.put("taxId", user.getTaxId());
            userData.put("verificationStatus", user.getVerificationStatus().name());
            
            Map<String, Object> walletData = new HashMap<>();
            walletData.put("balance", wallet.getBalance());
            walletData.put("accountNumber", wallet.getAccountNumber());
            walletData.put("isActive", wallet.isActive());
            walletData.put("createdAt", wallet.getCreatedAt());
            
            response.put("user", userData);
            response.put("wallet", walletData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserWalletBalance(@PathVariable Long userId) {
        try {
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
            
            return ResponseEntity.ok(Map.of(
                "balance", wallet.getBalance(),
                "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
