package com.revpay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistoryDTO {
    private Long id;
    private String transactionId;
    private String transactionType;
    private String status;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String transactionNature; // DEBIT or CREDIT
    private String counterpartyName; // Name of the other party
    private String counterpartyEmail; // Email of the other party
    private boolean isCurrentUserSender;
    private boolean isCurrentUserReceiver;

    // Constructors
    public TransactionHistoryDTO() {}

    public TransactionHistoryDTO(Long id, String transactionId, String transactionType, String status, 
                            BigDecimal amount, String description, LocalDateTime createdAt, 
                            LocalDateTime completedAt, String transactionNature, String counterpartyName, 
                            String counterpartyEmail, boolean isCurrentUserSender, boolean isCurrentUserReceiver) {
        this.id = id;
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.status = status;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.transactionNature = transactionNature;
        this.counterpartyName = counterpartyName;
        this.counterpartyEmail = counterpartyEmail;
        this.isCurrentUserSender = isCurrentUserSender;
        this.isCurrentUserReceiver = isCurrentUserReceiver;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getTransactionNature() { return transactionNature; }
    public void setTransactionNature(String transactionNature) { this.transactionNature = transactionNature; }

    public String getCounterpartyName() { return counterpartyName; }
    public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }

    public String getCounterpartyEmail() { return counterpartyEmail; }
    public void setCounterpartyEmail(String counterpartyEmail) { this.counterpartyEmail = counterpartyEmail; }

    public boolean isCurrentUserSender() { return isCurrentUserSender; }
    public void setCurrentUserSender(boolean currentUserSender) { isCurrentUserSender = currentUserSender; }

    public boolean isCurrentUserReceiver() { return isCurrentUserReceiver; }
    public void setCurrentUserReceiver(boolean currentUserReceiver) { isCurrentUserReceiver = currentUserReceiver; }
}
