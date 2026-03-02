package com.revpay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PersonalAnalyticsDTO {
    private BigDecimal totalSent;
    private BigDecimal totalReceived;
    private BigDecimal totalWithdrawn;
    private Integer transactionCount;
    private BigDecimal averageTransaction;
    private List<MonthlyTrendDTO> monthlyTrends;
    private List<TransactionSummaryDTO> recentTransactions;

    // Constructors
    public PersonalAnalyticsDTO() {}

    public PersonalAnalyticsDTO(BigDecimal totalSent, BigDecimal totalReceived, BigDecimal totalWithdrawn, 
                               Integer transactionCount, BigDecimal averageTransaction) {
        this.totalSent = totalSent;
        this.totalReceived = totalReceived;
        this.totalWithdrawn = totalWithdrawn;
        this.transactionCount = transactionCount;
        this.averageTransaction = averageTransaction;
    }

    // Getters and Setters
    public BigDecimal getTotalSent() { return totalSent; }
    public void setTotalSent(BigDecimal totalSent) { this.totalSent = totalSent; }

    public BigDecimal getTotalReceived() { return totalReceived; }
    public void setTotalReceived(BigDecimal totalReceived) { this.totalReceived = totalReceived; }

    public BigDecimal getTotalWithdrawn() { return totalWithdrawn; }
    public void setTotalWithdrawn(BigDecimal totalWithdrawn) { this.totalWithdrawn = totalWithdrawn; }

    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

    public BigDecimal getAverageTransaction() { return averageTransaction; }
    public void setAverageTransaction(BigDecimal averageTransaction) { this.averageTransaction = averageTransaction; }

    public List<MonthlyTrendDTO> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrendDTO> monthlyTrends) { this.monthlyTrends = monthlyTrends; }

    public List<TransactionSummaryDTO> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<TransactionSummaryDTO> recentTransactions) { this.recentTransactions = recentTransactions; }

    // Inner classes for detailed data
    public static class MonthlyTrendDTO {
        private String month;
        private BigDecimal sent;
        private BigDecimal received;
        private Integer transactionCount;

        public MonthlyTrendDTO() {}

        public MonthlyTrendDTO(String month, BigDecimal sent, BigDecimal received, Integer transactionCount) {
            this.month = month;
            this.sent = sent;
            this.received = received;
            this.transactionCount = transactionCount;
        }

        // Getters and Setters
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public BigDecimal getSent() { return sent; }
        public void setSent(BigDecimal sent) { this.sent = sent; }

        public BigDecimal getReceived() { return received; }
        public void setReceived(BigDecimal received) { this.received = received; }

        public Integer getTransactionCount() { return transactionCount; }
        public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }
    }

    public static class TransactionSummaryDTO {
        private Long id;
        private String type; // SENT, RECEIVED, WITHDRAWN
        private BigDecimal amount;
        private String description;
        private LocalDateTime createdAt;
        private String counterpartyName;

        public TransactionSummaryDTO() {}

        public TransactionSummaryDTO(Long id, String type, BigDecimal amount, String description, 
                                   LocalDateTime createdAt, String counterpartyName) {
            this.id = id;
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.createdAt = createdAt;
            this.counterpartyName = counterpartyName;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public String getCounterpartyName() { return counterpartyName; }
        public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }
    }
}
