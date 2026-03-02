package com.revpay.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "invoice_number", unique = true, nullable = false, length = 50)
    private String invoiceNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private User business;
    
    @Column(name = "customer_name", nullable = false)
    @NotBlank
    private String customerName;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "customer_address")
    private String customerAddress;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "due_date", nullable = false)
    @NotNull
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;
    
    @Column(name = "payment_terms")
    private String paymentTerms;
    
    private String notes;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<InvoiceItem> items;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (invoiceNumber == null) {
            invoiceNumber = generateInvoiceNumber();
        }
        // Initialize subtotal to avoid JSON parsing errors
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        // Calculate totals if items exist
        calculateTotals();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateInvoiceNumber() {
        return "INV" + System.currentTimeMillis();
    }
    
    // Business methods
    public void calculateTotals() {
        if (items != null && !items.isEmpty()) {
            subtotal = items.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        // Safe null checks before addition
        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal safeTaxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        totalAmount = safeSubtotal.add(safeTaxAmount);
    }
    
    public void send() {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be sent");
        }
        this.status = InvoiceStatus.SENT;
    }
    
    public void markAsPaid() {
        if (status != InvoiceStatus.SENT && status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("Only SENT or OVERDUE invoices can be marked as paid");
        }
        this.status = InvoiceStatus.PAID;
    }
    
    public void cancel() {
        if (status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Paid invoices cannot be cancelled");
        }
        this.status = InvoiceStatus.CANCELLED;
    }
    
    public void markAsOverdue() {
        if (status != InvoiceStatus.SENT) {
            throw new IllegalStateException("Only SENT invoices can be marked as overdue");
        }
        this.status = InvoiceStatus.OVERDUE;
    }
    
    public boolean isOverdue() {
        return (status == InvoiceStatus.SENT || status == InvoiceStatus.OVERDUE) && LocalDate.now().isAfter(dueDate);
    }
    
    public boolean canBePaid() {
        return status == InvoiceStatus.SENT || status == InvoiceStatus.OVERDUE;
    }
    
    public boolean canBeSent() {
        return status == InvoiceStatus.DRAFT;
    }
    
    public boolean canBeCancelled() {
        return status != InvoiceStatus.PAID && status != InvoiceStatus.CANCELLED;
    }
    
    public boolean isFinalStatus() {
        return status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED;
    }
    
    // Constructors
    public Invoice() {}
    
    public Invoice(User business, String customerName, BigDecimal subtotal, BigDecimal taxAmount, LocalDate dueDate) {
        this.business = business;
        this.customerName = customerName;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        // Safe null checks before addition
        BigDecimal safeSubtotal = this.subtotal != null ? this.subtotal : BigDecimal.ZERO;
        BigDecimal safeTaxAmount = this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO;
        this.totalAmount = safeSubtotal.add(safeTaxAmount);
        this.dueDate = dueDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public User getBusiness() { return business; }
    public void setBusiness(User business) { this.business = business; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { 
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        calculateTotals(); // Recalculate total when subtotal changes
    }
    
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { 
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        calculateTotals(); // Recalculate total when taxAmount changes
    }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { 
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public enum InvoiceStatus {
        DRAFT, SENT, PAID, OVERDUE, CANCELLED
    }
}
