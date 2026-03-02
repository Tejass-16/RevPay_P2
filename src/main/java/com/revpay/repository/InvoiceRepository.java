package com.revpay.repository;

import com.revpay.entity.Invoice;
import com.revpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByBusiness(User business);
    
    List<Invoice> findByBusinessAndStatus(User business, Invoice.InvoiceStatus status);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    // Customer-facing methods
    List<Invoice> findByCustomerEmail(String customerEmail);
    
    List<Invoice> findByCustomerEmailAndStatus(String customerEmail, Invoice.InvoiceStatus status);
    
    Optional<Invoice> findByCustomerEmailAndInvoiceNumber(String customerEmail, String invoiceNumber);
    
    @Query("SELECT i FROM Invoice i WHERE i.customerEmail = :customerEmail AND i.dueDate < :currentDate AND i.status = 'SENT'")
    List<Invoice> findOverdueInvoicesForCustomer(@Param("customerEmail") String customerEmail, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.business = :business AND i.dueDate < :currentDate AND i.status = 'SENT'")
    List<Invoice> findOverdueInvoices(@Param("business") User business, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.business = :business AND i.status = :status")
    long countByBusinessAndStatus(@Param("business") User business, @Param("status") Invoice.InvoiceStatus status);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.business = :business AND i.status = 'PAID'")
    Double getTotalPaidAmount(@Param("business") User business);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.business = :business AND i.status IN ('SENT', 'OVERDUE')")
    Double getTotalOutstandingAmount(@Param("business") User business);
    
    @Query("SELECT i FROM Invoice i WHERE i.business.id = :businessId ORDER BY i.createdAt DESC")
    List<Invoice> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") Long businessId);
}
