package com.revpay.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_methods")
public class PaymentMethod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;
    
    @Column(name = "card_number_encrypted", nullable = false)
    private String cardNumberEncrypted;
    
    @Column(name = "card_last_four", nullable = false, length = 4)
    private String cardLastFour;
    
    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;
    
    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;
    
    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;
    
    @Column(name = "billing_address")
    private String billingAddress;
    
    @Column(name = "is_default")
    private boolean isDefault = false;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public PaymentMethod() {}
    
    public PaymentMethod(User user, CardType cardType, String cardNumberEncrypted, 
                        String cardLastFour, Integer expiryMonth, Integer expiryYear, 
                        String cardholderName) {
        this.user = user;
        this.cardType = cardType;
        this.cardNumberEncrypted = cardNumberEncrypted;
        this.cardLastFour = cardLastFour;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cardholderName = cardholderName;
    }
    
    // Business methods
    public boolean isExpired() {
        int currentYear = java.time.Year.now().getValue() % 100;
        int currentMonth = java.time.LocalDateTime.now().getMonthValue();
        
        if (expiryYear < currentYear) {
            return true;
        } else if (expiryYear == currentYear && expiryMonth < currentMonth) {
            return true;
        }
        return false;
    }
    
    public String getMaskedCardNumber() {
        return "****-****-****-" + cardLastFour;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }
    
    public String getCardNumberEncrypted() { return cardNumberEncrypted; }
    public void setCardNumberEncrypted(String cardNumberEncrypted) { this.cardNumberEncrypted = cardNumberEncrypted; }
    
    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }
    
    public Integer getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }
    
    public Integer getExpiryYear() { return expiryYear; }
    public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }
    
    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
    
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public enum CardType {
        CREDIT, DEBIT
    }
}
