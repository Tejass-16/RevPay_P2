package com.revpay.controller;

import com.revpay.entity.SocialPayment;
import com.revpay.entity.User;
import com.revpay.repository.UserRepository;
import com.revpay.service.SocialPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social-payments")
public class SocialPaymentController {

    @Autowired
    private SocialPaymentService socialPaymentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createSocialPayment(@RequestBody Map<String, Object> request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String platform = request.get("platform").toString();
            String description = request.get("description").toString();

            // Find user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Convert platform string to enum
            SocialPayment.PaymentPlatform paymentPlatform = SocialPayment.PaymentPlatform.valueOf(platform.toUpperCase());
            
            SocialPayment socialPayment = socialPaymentService.createPayment(user.getId(), amount, paymentPlatform, description, null);
            return ResponseEntity.ok(socialPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<?> completeSocialPayment(@PathVariable Long paymentId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            // Find user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            SocialPayment socialPayment = socialPaymentService.completePayment(paymentId, user.getId());
            return ResponseEntity.ok(socialPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<?> failSocialPayment(@PathVariable Long paymentId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            // Find user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            SocialPayment socialPayment = socialPaymentService.failPayment(paymentId, user.getId());
            return ResponseEntity.ok(socialPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<SocialPayment>> getUserSocialPayments(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<SocialPayment> socialPayments = socialPaymentService.getPaymentsForUser(user.getId());
        return ResponseEntity.ok(socialPayments);
    }

    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<SocialPayment>> getSocialPaymentsByPlatform(@PathVariable String platform,
                                                                          @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Convert platform string to enum
        SocialPayment.PaymentPlatform paymentPlatform = SocialPayment.PaymentPlatform.valueOf(platform.toUpperCase());
        
        List<SocialPayment> socialPayments = socialPaymentService.getPaymentsByPlatform(paymentPlatform);
        return ResponseEntity.ok(socialPayments);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<SocialPayment>> getCompletedSocialPayments(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<SocialPayment> socialPayments = socialPaymentService.getCompletedPaymentsForUser(user.getId());
        return ResponseEntity.ok(socialPayments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getSocialPayment(@PathVariable Long paymentId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            // Find user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            SocialPayment socialPayment = socialPaymentService.getPaymentById(paymentId);
            
            // Verify user owns the payment
            if (socialPayment != null && !socialPayment.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("You can only view your own payments");
            }
            
            if (socialPayment == null) {
                throw new IllegalArgumentException("Payment not found");
            }
            return ResponseEntity.ok(socialPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}