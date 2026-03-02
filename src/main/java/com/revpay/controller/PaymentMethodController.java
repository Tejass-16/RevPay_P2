package com.revpay.controller;

import com.revpay.entity.PaymentMethod;
import com.revpay.entity.User;
import com.revpay.repository.UserRepository;
import com.revpay.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-methods")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getUserPaymentMethods(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("=== PAYMENT METHODS DEBUG ===");
            System.out.println("User email: " + userDetails.getUsername());
            
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            System.out.println("User found: " + user.getEmail() + ", ID: " + user.getId());
            
            List<PaymentMethod> paymentMethods = paymentMethodService.getUserPaymentMethods(user.getId());
            System.out.println("Payment methods found: " + paymentMethods.size());
            
            for (PaymentMethod pm : paymentMethods) {
                System.out.println("Payment Method ID: " + pm.getId() + 
                                   ", Card Type: " + pm.getCardType() +
                                   ", Card Last Four: " + pm.getCardLastFour() +
                                   ", Is Active: " + pm.isActive() +
                                   ", Is Default: " + pm.isDefault());
            }
            
            return ResponseEntity.ok(paymentMethods);
        } catch (Exception e) {
            System.out.println("ERROR in payment methods: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> addPaymentMethod(@RequestBody Map<String, Object> request,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            PaymentMethod paymentMethod = paymentMethodService.addPaymentMethod(
                user.getId(),
                request.get("cardType").toString(),
                request.get("cardNumber").toString(),
                request.get("expiryMonth") != null ? Integer.parseInt(request.get("expiryMonth").toString()) : null,
                request.get("expiryYear") != null ? Integer.parseInt(request.get("expiryYear").toString()) : null,
                request.get("cardholderName") != null ? request.get("cardholderName").toString() : null,
                request.get("billingAddress") != null ? request.get("billingAddress").toString() : null,
                request.get("isDefault") != null ? Boolean.parseBoolean(request.get("isDefault").toString()) : false
            );
            
            return ResponseEntity.ok(paymentMethod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePaymentMethod(@PathVariable Long id,
                                          @RequestBody Map<String, Object> request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            PaymentMethod paymentMethod = paymentMethodService.updatePaymentMethod(
                id, user.getId(), request
            );
            
            return ResponseEntity.ok(paymentMethod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            paymentMethodService.deletePaymentMethod(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Payment method deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<?> setDefaultPaymentMethod(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            paymentMethodService.setDefaultPaymentMethod(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Payment method set as default"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
