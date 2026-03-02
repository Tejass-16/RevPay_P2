package com.revpay.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "money_requests")
public class MoneyRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", unique = true, nullable = false, length = 50)
    private String requestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_from_id", nullable = false)
    private User requestedFrom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal amount;
    
    private String purpose;
    
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusDays(7); // Auto-expire after 7 days
        if (requestId == null) {
            requestId = generateRequestId();
        }
    }
    
    private String generateRequestId() {
        return "REQ" + System.currentTimeMillis();
    }
    
    // Business methods
    public void accept() {
        this.status = RequestStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }
    
    public void decline() {
        this.status = RequestStatus.DECLINED;
        this.respondedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = RequestStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public String getReferenceId() {
        return requestId;
    }
    
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }
    
    // Constructors
    public MoneyRequest() {}
    
    public MoneyRequest(User requester, User requestedFrom, User receiver, BigDecimal amount, String purpose) {
        this.requester = requester;
        this.requestedFrom = requestedFrom;
        this.receiver = receiver;
        this.amount = amount;
        this.purpose = purpose;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    
    public User getRequestedFrom() { return requestedFrom; }
    public void setRequestedFrom(User requestedFrom) { this.requestedFrom = requestedFrom; }
    
    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public enum RequestStatus {
        PENDING, ACCEPTED, DECLINED, CANCELLED, EXPIRED
    }
}
