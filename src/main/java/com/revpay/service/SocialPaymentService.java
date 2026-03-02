package com.revpay.service;

import com.revpay.entity.SocialPayment;
import com.revpay.entity.User;
import com.revpay.repository.SocialPaymentRepository;
import com.revpay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SocialPaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(SocialPaymentService.class);
    
    @Autowired
    private SocialPaymentRepository socialPaymentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public SocialPayment createPayment(Long userId, BigDecimal amount, SocialPayment.PaymentPlatform platform, String description, String externalReference) {
        try {
            logger.info("Creating social payment for user {} with amount {} on platform {}", userId, amount, platform);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            SocialPayment payment = new SocialPayment(user, amount, platform, description);
            payment.setExternalReference(externalReference);
            payment = socialPaymentRepository.save(payment);
            
            logger.info("Social payment created successfully with ID: {}", payment.getId());
            return payment;
        } catch (Exception e) {
            logger.error("Error creating social payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create social payment: " + e.getMessage());
        }
    }
    
    @Transactional
    public SocialPayment completePayment(Long paymentId, Long userId) {
        try {
            logger.info("Completing social payment {} by user {}", paymentId, userId);
            
            SocialPayment payment = socialPaymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            
            // Verify user owns the payment
            if (!payment.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("You can only complete your own payments");
            }
            
            if (!payment.isPending()) {
                throw new IllegalArgumentException("Payment is no longer pending");
            }
            
            payment.complete();
            payment = socialPaymentRepository.save(payment);
            
            logger.info("Social payment {} completed successfully", paymentId);
            return payment;
        } catch (Exception e) {
            logger.error("Error completing social payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to complete social payment: " + e.getMessage());
        }
    }
    
    @Transactional
    public SocialPayment failPayment(Long paymentId, Long userId) {
        try {
            logger.info("Failing social payment {} by user {}", paymentId, userId);
            
            SocialPayment payment = socialPaymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            
            // Verify user owns the payment
            if (!payment.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("You can only fail your own payments");
            }
            
            if (!payment.isPending()) {
                throw new IllegalArgumentException("Payment is no longer pending");
            }
            
            payment.fail();
            payment = socialPaymentRepository.save(payment);
            
            logger.info("Social payment {} failed successfully", paymentId);
            return payment;
        } catch (Exception e) {
            logger.error("Error failing social payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fail social payment: " + e.getMessage());
        }
    }
    
    // Get methods
    public List<SocialPayment> getPaymentsForUser(Long userId) {
        return socialPaymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<SocialPayment> getPaymentsByPlatform(SocialPayment.PaymentPlatform platform) {
        return socialPaymentRepository.findByPlatformOrderByCreatedAtDesc(platform);
    }
    
    public List<SocialPayment> getCompletedPaymentsForUser(Long userId) {
        return socialPaymentRepository.findCompletedPaymentsByUserId(userId);
    }
    
    public SocialPayment getPaymentById(Long paymentId) {
        return socialPaymentRepository.findById(paymentId).orElse(null);
    }
    
    public Long getTotalPaymentsForUser(Long userId) {
        return socialPaymentRepository.countByUserIdAndStatus(userId, SocialPayment.PaymentStatus.COMPLETED);
    }
}
