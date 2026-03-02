package com.revpay.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BusinessAnalyticsDTO {
    private BigDecimal totalRevenue;
    private BigDecimal outstandingInvoices;
    private BigDecimal paidInvoices;
    private Integer totalCustomers;
    private Integer activeCustomers;
    private List<CustomerAnalyticsDTO> topCustomers;
    private List<RevenueTrendDTO> revenueTrends;
    private List<InvoiceSummaryDTO> recentInvoices;

    // Constructors
    public BusinessAnalyticsDTO() {}

    public BusinessAnalyticsDTO(BigDecimal totalRevenue, BigDecimal outstandingInvoices, 
                               BigDecimal paidInvoices, Integer totalCustomers, Integer activeCustomers) {
        this.totalRevenue = totalRevenue;
        this.outstandingInvoices = outstandingInvoices;
        this.paidInvoices = paidInvoices;
        this.totalCustomers = totalCustomers;
        this.activeCustomers = activeCustomers;
    }

    // Getters and Setters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getOutstandingInvoices() { return outstandingInvoices; }
    public void setOutstandingInvoices(BigDecimal outstandingInvoices) { this.outstandingInvoices = outstandingInvoices; }

    public BigDecimal getPaidInvoices() { return paidInvoices; }
    public void setPaidInvoices(BigDecimal paidInvoices) { this.paidInvoices = paidInvoices; }

    public Integer getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(Integer totalCustomers) { this.totalCustomers = totalCustomers; }

    public Integer getActiveCustomers() { return activeCustomers; }
    public void setActiveCustomers(Integer activeCustomers) { this.activeCustomers = activeCustomers; }

    public List<CustomerAnalyticsDTO> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<CustomerAnalyticsDTO> topCustomers) { this.topCustomers = topCustomers; }

    public List<RevenueTrendDTO> getRevenueTrends() { return revenueTrends; }
    public void setRevenueTrends(List<RevenueTrendDTO> revenueTrends) { this.revenueTrends = revenueTrends; }

    public List<InvoiceSummaryDTO> getRecentInvoices() { return recentInvoices; }
    public void setRecentInvoices(List<InvoiceSummaryDTO> recentInvoices) { this.recentInvoices = recentInvoices; }

    // Inner classes for detailed data
    public static class CustomerAnalyticsDTO {
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private BigDecimal totalSpent;
        private Integer transactionCount;
        private LocalDateTime lastTransactionDate;
        private Integer invoiceCount;

        public CustomerAnalyticsDTO() {}

        public CustomerAnalyticsDTO(Long customerId, String customerName, String customerEmail, 
                                   BigDecimal totalSpent, Integer transactionCount, 
                                   LocalDateTime lastTransactionDate, Integer invoiceCount) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.customerEmail = customerEmail;
            this.totalSpent = totalSpent;
            this.transactionCount = transactionCount;
            this.lastTransactionDate = lastTransactionDate;
            this.invoiceCount = invoiceCount;
        }

        // Getters and Setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

        public Integer getTransactionCount() { return transactionCount; }
        public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

        public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
        public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

        public Integer getInvoiceCount() { return invoiceCount; }
        public void setInvoiceCount(Integer invoiceCount) { this.invoiceCount = invoiceCount; }
    }

    public static class RevenueTrendDTO {
        private String period; // Daily, Weekly, Monthly
        private String label; // Date label
        private BigDecimal revenue;
        private BigDecimal outstandingRevenue;
        private Integer transactionCount;
        private Integer customerCount;

        public RevenueTrendDTO() {}

        public RevenueTrendDTO(String period, String label, BigDecimal revenue, 
                               BigDecimal outstandingRevenue, Integer transactionCount, Integer customerCount) {
            this.period = period;
            this.label = label;
            this.revenue = revenue;
            this.outstandingRevenue = outstandingRevenue;
            this.transactionCount = transactionCount;
            this.customerCount = customerCount;
        }

        // Getters and Setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public BigDecimal getOutstandingRevenue() { return outstandingRevenue; }
        public void setOutstandingRevenue(BigDecimal outstandingRevenue) { this.outstandingRevenue = outstandingRevenue; }

        public Integer getTransactionCount() { return transactionCount; }
        public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

        public Integer getCustomerCount() { return customerCount; }
        public void setCustomerCount(Integer customerCount) { this.customerCount = customerCount; }
    }

    public static class InvoiceSummaryDTO {
        private Long invoiceId;
        private String invoiceNumber;
        private String customerName;
        private BigDecimal amount;
        private String status; // PAID, PENDING, OVERDUE
        private LocalDateTime createdAt;
        private LocalDateTime dueDate;

        public InvoiceSummaryDTO() {}

        public InvoiceSummaryDTO(Long invoiceId, String invoiceNumber, String customerName, 
                                BigDecimal amount, String status, LocalDateTime createdAt, LocalDateTime dueDate) {
            this.invoiceId = invoiceId;
            this.invoiceNumber = invoiceNumber;
            this.customerName = customerName;
            this.amount = amount;
            this.status = status;
            this.createdAt = createdAt;
            this.dueDate = dueDate;
        }

        // Getters and Setters
        public Long getInvoiceId() { return invoiceId; }
        public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    }
}
