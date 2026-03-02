package com.revpay.controller;

import com.revpay.entity.Invoice;
import com.revpay.entity.InvoiceItem;
import com.revpay.entity.User;
import com.revpay.service.InvoiceService;
import com.revpay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "http://localhost:4200")
public class InvoiceController {
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/create")
    public ResponseEntity<Invoice> createInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Invoice invoice) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user has BUSINESS role
        if (!business.getUserRole().equals(User.UserRole.BUSINESS)) {
            throw new RuntimeException("Only business users can create invoices");
        }
        
        Invoice createdInvoice = invoiceService.createInvoice(business, invoice);
        return ResponseEntity.ok(createdInvoice);
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<Invoice> updateInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Invoice invoice) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> existingInvoice = invoiceService.getInvoiceById(id);
        if (existingInvoice.isPresent() && !existingInvoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only update your own invoices");
        }
        
        Invoice updatedInvoice = invoiceService.updateInvoice(id, invoice);
        return ResponseEntity.ok(updatedInvoice);
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> existingInvoice = invoiceService.getInvoiceById(id);
        if (existingInvoice.isPresent() && !existingInvoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only delete your own invoices");
        }
        
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/my-invoices")
    public ResponseEntity<List<Invoice>> getMyInvoices(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        List<Invoice> invoices = invoiceService.getBusinessInvoices(business);
        return ResponseEntity.ok(invoices);
    }
    
    @GetMapping("/my-invoices/{status}")
    public ResponseEntity<List<Invoice>> getMyInvoicesByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Invoice.InvoiceStatus status) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        List<Invoice> invoices = invoiceService.getBusinessInvoicesByStatus(business, status);
        return ResponseEntity.ok(invoices);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        Optional<Invoice> invoice = invoiceService.getInvoiceById(id);
        if (invoice.isPresent() && invoice.get().getBusiness().getId().equals(business.getId())) {
            return ResponseEntity.ok(invoice.get());
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/send/{id}")
    public ResponseEntity<Invoice> sendInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> existingInvoice = invoiceService.getInvoiceById(id);
        if (existingInvoice.isPresent() && !existingInvoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only send your own invoices");
        }
        
        Invoice sentInvoice = invoiceService.sendInvoice(id);
        return ResponseEntity.ok(sentInvoice);
    }
    
    @PostMapping("/mark-paid/{id}")
    public ResponseEntity<Invoice> markInvoiceAsPaid(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> existingInvoice = invoiceService.getInvoiceById(id);
        if (existingInvoice.isPresent() && !existingInvoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only mark your own invoices as paid");
        }
        
        Invoice paidInvoice = invoiceService.markInvoiceAsPaid(id);
        return ResponseEntity.ok(paidInvoice);
    }
    
    @PostMapping("/cancel/{id}")
    public ResponseEntity<Invoice> cancelInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> existingInvoice = invoiceService.getInvoiceById(id);
        if (existingInvoice.isPresent() && !existingInvoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only cancel your own invoices");
        }
        
        Invoice cancelledInvoice = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(cancelledInvoice);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<Invoice>> getOverdueInvoices(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        List<Invoice> overdueInvoices = invoiceService.getOverdueInvoices(business);
        return ResponseEntity.ok(overdueInvoices);
    }
    
    @GetMapping("/payment-link/{id}")
    public ResponseEntity<String> generatePaymentLink(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> existingInvoice = invoiceService.getInvoiceById(id);
        if (existingInvoice.isPresent() && !existingInvoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only generate payment links for your own invoices");
        }
        
        String paymentLink = invoiceService.generatePaymentLink(id);
        return ResponseEntity.ok(paymentLink);
    }
    
    @PostMapping("/add-item/{invoiceId}")
    public ResponseEntity<Invoice> addItemToInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long invoiceId,
            @RequestBody InvoiceItem item) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> existingInvoice = invoiceService.getInvoiceById(invoiceId);
        if (existingInvoice.isPresent() && !existingInvoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only add items to your own invoices");
        }
        
        Invoice updatedInvoice = invoiceService.addItemToInvoice(invoiceId, item);
        return ResponseEntity.ok(updatedInvoice);
    }
    
    @DeleteMapping("/remove-item/{invoiceId}/{itemId}")
    public ResponseEntity<Invoice> removeItemFromInvoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long invoiceId,
            @PathVariable Long itemId) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user owns this invoice
        Optional<Invoice> existingInvoice = invoiceService.getInvoiceById(invoiceId);
        if (existingInvoice.isPresent() && !existingInvoice.get().getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("You can only remove items from your own invoices");
        }
        
        Invoice updatedInvoice = invoiceService.removeItemFromInvoice(invoiceId, itemId);
        return ResponseEntity.ok(updatedInvoice);
    }
}
