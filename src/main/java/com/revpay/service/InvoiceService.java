package com.revpay.service;

import com.revpay.entity.Invoice;
import com.revpay.entity.InvoiceItem;
import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    public Invoice createInvoice(User business, Invoice invoice) {
        invoice.setBusiness(business);
        
        // Set invoice reference in each item to avoid null invoice_id
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                item.setInvoice(invoice);
            }
        }
        
        invoice.calculateTotals();
        return invoiceRepository.save(invoice);
    }
    
    public Invoice updateInvoice(Long invoiceId, Invoice updatedInvoice) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (existingInvoice.isPresent()) {
            Invoice invoice = existingInvoice.get();
            
            // Update basic fields
            invoice.setCustomerName(updatedInvoice.getCustomerName());
            invoice.setCustomerEmail(updatedInvoice.getCustomerEmail());
            invoice.setCustomerAddress(updatedInvoice.getCustomerAddress());
            invoice.setDueDate(updatedInvoice.getDueDate());
            invoice.setPaymentTerms(updatedInvoice.getPaymentTerms());
            invoice.setNotes(updatedInvoice.getNotes());
            
            // Update items if provided
            if (updatedInvoice.getItems() != null) {
                // Set invoice reference in each item to avoid null invoice_id
                for (InvoiceItem item : updatedInvoice.getItems()) {
                    item.setInvoice(invoice);
                }
                invoice.setItems(updatedInvoice.getItems());
                // Recalculate totals
                invoice.calculateTotals();
            }
            
            return invoiceRepository.save(invoice);
        }
        throw new RuntimeException("Invoice not found with ID: " + invoiceId);
    }
    
    public void deleteInvoice(Long invoiceId) {
        if (invoiceRepository.existsById(invoiceId)) {
            invoiceRepository.deleteById(invoiceId);
        } else {
            throw new RuntimeException("Invoice not found with ID: " + invoiceId);
        }
    }
    
    public List<Invoice> getBusinessInvoices(User business) {
        List<Invoice> invoices = invoiceRepository.findByBusiness(business);
        // Initialize business relationship to avoid lazy loading issues
        invoices.forEach(invoice -> {
            invoice.getBusiness().getFullName(); // Initialize the proxy
            if (invoice.getItems() != null) {
                invoice.getItems().size(); // Initialize items if needed
            }
        });
        return invoices;
    }
    
    public List<Invoice> getBusinessInvoicesByStatus(User business, Invoice.InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepository.findByBusinessAndStatus(business, status);
        // Initialize business relationship to avoid lazy loading issues
        invoices.forEach(invoice -> {
            invoice.getBusiness().getFullName(); // Initialize the proxy
            if (invoice.getItems() != null) {
                invoice.getItems().size(); // Initialize items if needed
            }
        });
        return invoices;
    }
    
    public Optional<Invoice> getInvoiceById(Long invoiceId) {
        Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);
        // Initialize business relationship to avoid lazy loading issues
        invoice.ifPresent(inv -> {
            inv.getBusiness().getFullName(); // Initialize the proxy
            if (inv.getItems() != null) {
                inv.getItems().size(); // Initialize items if needed
            }
        });
        return invoice;
    }
    
    public Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        Optional<Invoice> invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber);
        // Initialize business relationship to avoid lazy loading issues
        invoice.ifPresent(inv -> {
            inv.getBusiness().getFullName(); // Initialize the proxy
            if (inv.getItems() != null) {
                inv.getItems().size(); // Initialize items if needed
            }
        });
        return invoice;
    }
    
    public Invoice sendInvoice(Long invoiceId) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (existingInvoice.isPresent()) {
            Invoice invoice = existingInvoice.get();
            invoice.send();
            return invoiceRepository.save(invoice);
        }
        throw new RuntimeException("Invoice not found with ID: " + invoiceId);
    }
    
    public Invoice markInvoiceAsPaid(Long invoiceId) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (existingInvoice.isPresent()) {
            Invoice invoice = existingInvoice.get();
            invoice.markAsPaid();
            return invoiceRepository.save(invoice);
        }
        throw new RuntimeException("Invoice not found with ID: " + invoiceId);
    }
    
    public Invoice cancelInvoice(Long invoiceId) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (existingInvoice.isPresent()) {
            Invoice invoice = existingInvoice.get();
            invoice.cancel();
            return invoiceRepository.save(invoice);
        }
        throw new RuntimeException("Invoice not found with ID: " + invoiceId);
    }
    
    public List<Invoice> getOverdueInvoices(User business) {
        return invoiceRepository.findOverdueInvoices(business, LocalDate.now());
    }
    
    public Invoice getInvoiceStatistics(User business) {
        Invoice stats = new Invoice();
        
        long totalInvoices = invoiceRepository.countByBusinessAndStatus(business, Invoice.InvoiceStatus.PAID);
        long draftInvoices = invoiceRepository.countByBusinessAndStatus(business, Invoice.InvoiceStatus.DRAFT);
        long sentInvoices = invoiceRepository.countByBusinessAndStatus(business, Invoice.InvoiceStatus.SENT);
        long overdueInvoices = invoiceRepository.findOverdueInvoices(business, LocalDate.now()).size();
        
        Double totalPaid = invoiceRepository.getTotalPaidAmount(business);
        Double totalOutstanding = invoiceRepository.getTotalOutstandingAmount(business);
        
        // You can create a DTO class for this, but for now using Invoice as a container
        return stats;
    }
    
    // Customer-facing methods
    public List<Invoice> getCustomerInvoices(String customerEmail) {
        List<Invoice> invoices = invoiceRepository.findByCustomerEmail(customerEmail);
        // Initialize business relationship to avoid lazy loading issues
        invoices.forEach(invoice -> {
            invoice.getBusiness().getFullName(); // Initialize the proxy
            if (invoice.getItems() != null) {
                invoice.getItems().size(); // Initialize items if needed
            }
        });
        return invoices;
    }
    
    public List<Invoice> getCustomerInvoicesByStatus(String customerEmail, Invoice.InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepository.findByCustomerEmailAndStatus(customerEmail, status);
        // Initialize business relationship to avoid lazy loading issues
        invoices.forEach(invoice -> {
            invoice.getBusiness().getFullName(); // Initialize the proxy
            if (invoice.getItems() != null) {
                invoice.getItems().size(); // Initialize items if needed
            }
        });
        return invoices;
    }
    
    public Optional<Invoice> getCustomerInvoiceByNumber(String customerEmail, String invoiceNumber) {
        Optional<Invoice> invoice = invoiceRepository.findByCustomerEmailAndInvoiceNumber(customerEmail, invoiceNumber);
        // Initialize business relationship to avoid lazy loading issues
        invoice.ifPresent(inv -> {
            inv.getBusiness().getFullName(); // Initialize the proxy
            if (inv.getItems() != null) {
                inv.getItems().size(); // Initialize items if needed
            }
        });
        return invoice;
    }
    
    public List<Invoice> getCustomerOverdueInvoices(String customerEmail) {
        List<Invoice> invoices = invoiceRepository.findOverdueInvoicesForCustomer(customerEmail, LocalDate.now());
        // Initialize business relationship to avoid lazy loading issues
        invoices.forEach(invoice -> {
            invoice.getBusiness().getFullName(); // Initialize the proxy
            if (invoice.getItems() != null) {
                invoice.getItems().size(); // Initialize items if needed
            }
        });
        return invoices;
    }
    
    // Invoice payment integration
    @Transactional
    public Invoice payInvoice(Long invoiceId, User customer) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (existingInvoice.isEmpty()) {
            throw new RuntimeException("Invoice not found with ID: " + invoiceId);
        }
        
        Invoice invoice = existingInvoice.get();
        
        // Validate invoice can be paid using entity method
        if (!invoice.canBePaid()) {
            throw new RuntimeException("Invoice cannot be paid. Current status: " + invoice.getStatus());
        }
        
        // Validate customer email matches
        if (!invoice.getCustomerEmail().equals(customer.getEmail())) {
            throw new RuntimeException("This invoice is not addressed to you");
        }
        
        // Create transaction for payment
        String description = "Payment for Invoice #" + invoice.getInvoiceNumber();
        
        // Find the business user (receiver of payment)
        User business = invoice.getBusiness();
        
        // Create payment transaction from customer to business
        Transaction paymentTransaction = transactionService.sendMoney(
            customer.getId(), 
            business.getId(), 
            invoice.getTotalAmount(), 
            description
        );
        
        // Mark invoice as paid using entity method
        invoice.markAsPaid();
        
        return invoiceRepository.save(invoice);
    }
    
    public boolean canPayInvoice(Long invoiceId, User customer) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (existingInvoice.isEmpty()) {
            return false;
        }
        
        Invoice invoice = existingInvoice.get();
        
        // Check if invoice can be paid and belongs to customer
        return invoice.canBePaid() && 
               invoice.getCustomerEmail().equals(customer.getEmail());
    }
    
    public String generatePaymentLink(Long invoiceId) {
        Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);
        if (invoice.isPresent()) {
            // Generate a unique payment link
            String paymentLink = "https://revpay.com/pay/" + invoice.get().getInvoiceNumber();
            return paymentLink;
        }
        throw new RuntimeException("Invoice not found with ID: " + invoiceId);
    }
    
    public Invoice addItemToInvoice(Long invoiceId, InvoiceItem item) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (existingInvoice.isPresent()) {
            Invoice invoice = existingInvoice.get();
            item.setInvoice(invoice);
            invoice.getItems().add(item);
            invoice.calculateTotals();
            return invoiceRepository.save(invoice);
        }
        throw new RuntimeException("Invoice not found with ID: " + invoiceId);
    }
    
    public Invoice removeItemFromInvoice(Long invoiceId, Long itemId) {
        Optional<Invoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (existingInvoice.isPresent()) {
            Invoice invoice = existingInvoice.get();
            invoice.getItems().removeIf(item -> item.getId().equals(itemId));
            invoice.calculateTotals();
            return invoiceRepository.save(invoice);
        }
        throw new RuntimeException("Invoice not found with ID: " + invoiceId);
    }
}
