package com.revpay.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revpay.entity.PaymentMethod;
import com.revpay.entity.User;
import com.revpay.repository.PaymentMethodRepository;

@Service
@Transactional
public class PaymentMethodService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodService.class);

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethod> getUserPaymentMethods(Long userId) {
        return paymentMethodRepository.findByUserIdAndIsActive(userId, true);
    }

    public PaymentMethod addPaymentMethod(Long userId, String cardType, String cardNumber, 
                                     Integer expiryMonth, Integer expiryYear, 
                                     String cardholderName, String billingAddress, 
                                     boolean isDefault) {
        logger.info("Adding payment method for user {}", userId);

        // Validate card details
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }

        if (expiryMonth == null || expiryYear == null) {
            throw new IllegalArgumentException("Expiry date is required");
        }

        if (cardholderName == null || cardholderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cardholder name is required");
        }

        // Validate expiry date
        int currentYear = java.time.Year.now().getValue() % 100;
        int currentMonth = java.time.LocalDateTime.now().getMonthValue();
        
        if (expiryYear < currentYear || (expiryYear == currentYear && expiryMonth < currentMonth)) {
            throw new IllegalArgumentException("Card has expired");
        }

        // If setting as default, unset other default methods
        if (isDefault) {
            List<PaymentMethod> existingMethods = paymentMethodRepository.findByUserId(userId);
            for (PaymentMethod method : existingMethods) {
                method.setDefault(false);
                paymentMethodRepository.save(method);
            }
        }

        // Convert string card type to enum with fallback
        private CardType convertToCardType(String cardType) {
            if (cardType == null || cardType.trim().isEmpty()) {
                    return PaymentMethod.CardType.CREDIT; // Default fallback
            }
            
            String normalizedType = cardType.toUpperCase().trim();
            
            // Handle various card type inputs
            switch (normalizedType) {
                    case "VISA":
                    case "MASTERCARD":
                    case "AMEX":
                    case "DISCOVER":
                    case "CREDIT":
                            return PaymentMethod.CardType.CREDIT;
                    case "DEBIT":
                    case "MAESTRO":
                    case "SWITCH":
                            return PaymentMethod.CardType.DEBIT;
                    default:
                            logger.warn("Unknown card type: {}, defaulting to CREDIT", cardType);
                            return PaymentMethod.CardType.CREDIT;
            }
        }

        // Encrypt card number (in real app, use proper encryption)
        String cardNumberEncrypted = encryptCardNumber(cardNumber);
        String cardLastFour = cardNumber.substring(cardNumber.length() - 4);

        PaymentMethod paymentMethod = new PaymentMethod();
        // Create a dummy user for now - in real app, fetch from repository
        User dummyUser = new User();
        dummyUser.setId(userId);
        paymentMethod.setUser(dummyUser);
        paymentMethod.setCardType(convertToCardType(cardType));
        paymentMethod.setCardNumberEncrypted(cardNumberEncrypted);
        paymentMethod.setCardLastFour(cardLastFour);
        paymentMethod.setExpiryMonth(expiryMonth);
        paymentMethod.setExpiryYear(expiryYear);
        paymentMethod.setCardholderName(cardholderName);
        paymentMethod.setBillingAddress(billingAddress);
        paymentMethod.setDefault(isDefault);
        paymentMethod.setActive(true);

        paymentMethod = paymentMethodRepository.save(paymentMethod);
        logger.info("Payment method added successfully with ID: {}", paymentMethod.getId());
        return paymentMethod;
    }

    public PaymentMethod updatePaymentMethod(Long paymentMethodId, Long userId, java.util.Map<String, Object> updates) {
        logger.info("Updating payment method {} for user {}", paymentMethodId, userId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found"));

        // Verify ownership
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Payment method does not belong to user");
        }

        // Update fields
        if (updates.containsKey("cardholderName")) {
            paymentMethod.setCardholderName(updates.get("cardholderName").toString());
        }

        if (updates.containsKey("billingAddress")) {
            paymentMethod.setBillingAddress(updates.get("billingAddress").toString());
        }

        if (updates.containsKey("expiryMonth")) {
            paymentMethod.setExpiryMonth(Integer.parseInt(updates.get("expiryMonth").toString()));
        }

        if (updates.containsKey("expiryYear")) {
            paymentMethod.setExpiryYear(Integer.parseInt(updates.get("expiryYear").toString()));
        }

        if (updates.containsKey("isDefault")) {
            boolean newDefault = Boolean.parseBoolean(updates.get("isDefault").toString());
            if (newDefault) {
                // Unset other defaults
                List<PaymentMethod> userMethods = paymentMethodRepository.findByUserId(userId);
                for (PaymentMethod method : userMethods) {
                    if (!method.getId().equals(paymentMethodId)) {
                        method.setDefault(false);
                        paymentMethodRepository.save(method);
                    }
                }
            }
            paymentMethod.setDefault(newDefault);
        }

        paymentMethod = paymentMethodRepository.save(paymentMethod);
        logger.info("Payment method updated successfully");
        return paymentMethod;
    }

    public void deletePaymentMethod(Long paymentMethodId, Long userId) {
        logger.info("Deleting payment method {} for user {}", paymentMethodId, userId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found"));

        // Verify ownership
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Payment method does not belong to user");
        }

        paymentMethodRepository.delete(paymentMethod);
        logger.info("Payment method deleted successfully");
    }

    public void setDefaultPaymentMethod(Long paymentMethodId, Long userId) {
        logger.info("Setting payment method {} as default for user {}", paymentMethodId, userId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found"));

        // Verify ownership
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Payment method does not belong to user");
        }

        // Unset all other defaults
        List<PaymentMethod> userMethods = paymentMethodRepository.findByUserId(userId);
        for (PaymentMethod method : userMethods) {
            method.setDefault(false);
            paymentMethodRepository.save(method);
        }

        // Set new default
        paymentMethod.setDefault(true);
        paymentMethodRepository.save(paymentMethod);

        logger.info("Payment method set as default successfully");
    }

    private String encryptCardNumber(String cardNumber) {
        // Simple encryption for demo - in production, use proper encryption
        return "encrypted_" + cardNumber;
    }
}
