package com.revpay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_payments")
public class SocialPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_platform", nullable = false)
    private PaymentPlatform platform;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    
    @Column(name = "external_reference", length = 100)
    private String externalReference;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        transactionId = "SOC" + System.currentTimeMillis();
    }
    
    // Constructors
    public SocialPayment() {}
    
    public SocialPayment(User user, BigDecimal amount, PaymentPlatform platform, String description) {
        this.user = user;
        this.amount = amount;
        this.platform = platform;
        this.description = description;
    }
    
    // Business methods
    public void complete() {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void fail() {
        this.status = PaymentStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }
    
    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }
    
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
    
    // Enums
    public enum PaymentPlatform {
        PAYPAL, STRIPE, VENMO, CASHAPP, ZELLE, UPI, BANK_TRANSFER
    }
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public PaymentPlatform getPlatform() { return platform; }
    public void setPlatform(PaymentPlatform platform) { this.platform = platform; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
