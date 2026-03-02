package com.revpay.controller;

import com.revpay.entity.Invoice;
import com.revpay.entity.User;
import com.revpay.service.InvoiceService;
import com.revpay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment-requests")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentRequestController {
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/generate/{invoiceId}")
    public ResponseEntity<Map<String, Object>> generatePaymentRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long invoiceId) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> invoice = invoiceService.getInvoiceById(invoiceId);
        if (invoice.isPresent() && !invoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only generate payment requests for your own invoices");
        }
        
        if (!invoice.isPresent()) {
            throw new RuntimeException("Invoice not found");
        }
        
        Invoice inv = invoice.get();
        
        // Generate payment request details
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("invoiceId", inv.getId());
        paymentRequest.put("invoiceNumber", inv.getInvoiceNumber());
        paymentRequest.put("amount", inv.getTotalAmount());
        paymentRequest.put("customerEmail", inv.getCustomerEmail());
        paymentRequest.put("customerName", inv.getCustomerName());
        paymentRequest.put("dueDate", inv.getDueDate());
        paymentRequest.put("paymentLink", "https://revpay.com/pay/" + inv.getInvoiceNumber());
        paymentRequest.put("qrCode", generateQRCode(inv.getInvoiceNumber()));
        paymentRequest.put("status", inv.getStatus());
        
        return ResponseEntity.ok(paymentRequest);
    }
    
    @GetMapping("/details/{invoiceNumber}")
    public ResponseEntity<Map<String, Object>> getPaymentRequestDetails(
            @PathVariable String invoiceNumber) {
        
        Optional<Invoice> invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        
        if (!invoice.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Invoice inv = invoice.get();
        
        // Return public payment request details (without sensitive business info)
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("invoiceNumber", inv.getInvoiceNumber());
        paymentDetails.put("amount", inv.getTotalAmount());
        paymentDetails.put("businessName", inv.getBusiness().getBusinessName());
        paymentDetails.put("dueDate", inv.getDueDate());
        paymentDetails.put("status", inv.getStatus());
        
        // Include items if invoice is sent
        if (inv.getStatus() == Invoice.InvoiceStatus.SENT && inv.getItems() != null) {
            paymentDetails.put("items", inv.getItems());
        }
        
        return ResponseEntity.ok(paymentDetails);
    }
    
    @PostMapping("/send-reminder/{invoiceId}")
    public ResponseEntity<String> sendPaymentReminder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long invoiceId) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> invoice = invoiceService.getInvoiceById(invoiceId);
        if (invoice.isPresent() && !invoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only send reminders for your own invoices");
        }
        
        if (!invoice.isPresent()) {
            throw new RuntimeException("Invoice not found");
        }
        
        Invoice inv = invoice.get();
        
        // In a real implementation, this would send an email/SMS
        // For now, we'll just return a success message
        String reminderMessage = String.format(
            "Payment reminder sent to %s for invoice %s. Amount: ₹%.2f",
            inv.getCustomerEmail(),
            inv.getInvoiceNumber(),
            inv.getTotalAmount()
        );
        
        return ResponseEntity.ok(reminderMessage);
    }
    
    private String generateQRCode(String invoiceNumber) {
        // In a real implementation, this would generate an actual QR code
        // For now, return a placeholder URL
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=https://revpay.com/pay/" + invoiceNumber;
    }
}
