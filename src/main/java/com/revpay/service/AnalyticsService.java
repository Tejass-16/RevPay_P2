package com.revpay.service;

import com.revpay.dto.PersonalAnalyticsDTO;
import com.revpay.dto.BusinessAnalyticsDTO;
import com.revpay.entity.User;
import com.revpay.entity.Transaction;
import com.revpay.entity.Invoice;
import com.revpay.repository.UserRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    // Personal Analytics Methods
    public PersonalAnalyticsDTO getPersonalAnalytics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        System.out.println("=== ANALYTICS SERVICE DEBUG ===");
        System.out.println("Getting personal analytics for userId: " + userId);

        // Get all transactions for the user
        List<Transaction> sentTransactions = transactionRepository.findBySenderIdOrderByCreatedAtDesc(userId);
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverIdOrderByCreatedAtDesc(userId);

        System.out.println("Sent transactions found: " + sentTransactions.size());
        System.out.println("Received transactions found: " + receivedTransactions.size());

        // Debug: Print transaction details
        System.out.println("=== SENT TRANSACTIONS DEBUG ===");
        for (Transaction t : sentTransactions) {
            System.out.println("ID: " + t.getId() + ", Type: '" + t.getTransactionType() + "', Amount: " + t.getAmount());
        }
        
        System.out.println("=== RECEIVED TRANSACTIONS DEBUG ===");
        for (Transaction t : receivedTransactions) {
            System.out.println("ID: " + t.getId() + ", Type: '" + t.getTransactionType() + "', Amount: " + t.getAmount());
        }

        // Calculate totals with flexible transaction type matching
        BigDecimal totalSent = sentTransactions.stream()
                .filter(t -> t.getTransactionType() != null && 
                           ("SENT".equalsIgnoreCase(t.getTransactionType()) || 
                            "PAYMENT".equalsIgnoreCase(t.getTransactionType()) ||
                            "TRANSFER".equalsIgnoreCase(t.getTransactionType())))
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = receivedTransactions.stream()
                .filter(t -> t.getTransactionType() != null && 
                           ("RECEIVED".equalsIgnoreCase(t.getTransactionType()) || 
                            "INCOME".equalsIgnoreCase(t.getTransactionType()) ||
                            "TRANSFER".equalsIgnoreCase(t.getTransactionType())))
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawn = sentTransactions.stream()
                .filter(t -> t.getTransactionType() != null && 
                           ("WITHDRAWN".equalsIgnoreCase(t.getTransactionType()) || 
                            "WITHDRAWAL".equalsIgnoreCase(t.getTransactionType()) ||
                            "DEBIT".equalsIgnoreCase(t.getTransactionType())))
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int transactionCount = sentTransactions.size() + receivedTransactions.size();

        BigDecimal averageTransaction = transactionCount > 0 ?
                totalSent.add(totalReceived).divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        System.out.println("Total Sent: " + totalSent);
        System.out.println("Total Received: " + totalReceived);
        System.out.println("Total Withdrawn: " + totalWithdrawn);
        System.out.println("Transaction Count: " + transactionCount);

        // Create analytics DTO
        PersonalAnalyticsDTO analytics = new PersonalAnalyticsDTO(
                totalSent, totalReceived, totalWithdrawn, transactionCount, averageTransaction
        );

        // Add monthly trends
        analytics.setMonthlyTrends(getMonthlyTrends(userId));

        // Add recent transactions
        analytics.setRecentTransactions(getRecentTransactions(userId));

        System.out.println("Final analytics object created");
        return analytics;
    }

    public List<PersonalAnalyticsDTO.MonthlyTrendDTO> getMonthlyTrends(Long userId) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Transaction> transactions = transactionRepository.findByUserIdAndCreatedAtAfter(userId, sixMonthsAgo);

        Map<String, PersonalAnalyticsDTO.MonthlyTrendDTO> monthlyData = new HashMap<>();

        // Initialize last 6 months
        LocalDateTime date = LocalDateTime.now().minusMonths(6);
        while (date.isBefore(LocalDateTime.now().plusMonths(1))) {
            String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            if (!monthlyData.containsKey(monthKey)) {
                monthlyData.put(monthKey, new PersonalAnalyticsDTO.MonthlyTrendDTO(
                        monthKey, BigDecimal.ZERO, BigDecimal.ZERO, 0));
            }
            date = date.plusMonths(1);
        }

        return new ArrayList<>(monthlyData.values()).stream()
                .sorted(Comparator.comparing(PersonalAnalyticsDTO.MonthlyTrendDTO::getMonth))
                .collect(Collectors.toList());
    }

    public List<PersonalAnalyticsDTO.TransactionSummaryDTO> getRecentTransactions(Long userId) {
        List<Transaction> sentTransactions = transactionRepository.findBySenderIdOrderByCreatedAtDesc(userId);
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverIdOrderByCreatedAtDesc(userId);

        List<PersonalAnalyticsDTO.TransactionSummaryDTO> allTransactions = new ArrayList<>();

        // Process sent transactions
        for (Transaction transaction : sentTransactions) {
            String type = "SENT";
            if ("WITHDRAWN".equals(transaction.getTransactionType())) {
                type = "WITHDRAWN";
            }
            
            // Safe access to receiver with null checks
            String receiverName = "Unknown";
            if (transaction.getReceiver() != null) {
                receiverName = transaction.getReceiver().getFullName() != null ? 
                               transaction.getReceiver().getFullName() : "Unknown";
            }
            
            allTransactions.add(new PersonalAnalyticsDTO.TransactionSummaryDTO(
                    transaction.getId(),
                    type,
                    transaction.getAmount(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    receiverName
            ));
        }

        // Process received transactions
        for (Transaction transaction : receivedTransactions) {
            // Safe access to sender with null checks
            String senderName = "Unknown";
            if (transaction.getSender() != null) {
                senderName = transaction.getSender().getFullName() != null ? 
                             transaction.getSender().getFullName() : "Unknown";
            }
            
            allTransactions.add(new PersonalAnalyticsDTO.TransactionSummaryDTO(
                    transaction.getId(),
                    "RECEIVED",
                    transaction.getAmount(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    senderName
            ));
        }

        // Sort by created date descending
        allTransactions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        // Limit to recent 10 transactions
        return allTransactions.stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    // Business Analytics Methods
    public BusinessAnalyticsDTO getBusinessAnalytics(Long businessId) {
        User business = userRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        // Get all invoices for the business
        List<Invoice> invoices = invoiceRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);

        // Calculate revenue metrics
        BigDecimal totalRevenue = invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstandingInvoices = invoices.stream()
                .filter(i -> "PENDING".equals(i.getStatus()) || "OVERDUE".equals(i.getStatus()))
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidInvoices = invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count unique customers
        Set<String> uniqueCustomers = invoices.stream()
                .map(Invoice::getCustomerEmail)
                .collect(Collectors.toSet());

        int totalCustomers = uniqueCustomers.size();

        // Count active customers (customers with paid invoices in last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int activeCustomers = (int) invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .filter(i -> i.getUpdatedAt() != null && i.getUpdatedAt().isAfter(thirtyDaysAgo))
                .map(Invoice::getCustomerEmail)
                .collect(Collectors.toSet())
                .size();

        // Create analytics DTO
        BusinessAnalyticsDTO analytics = new BusinessAnalyticsDTO(
                totalRevenue, outstandingInvoices, paidInvoices, totalCustomers, activeCustomers
        );

        // Add top customers
        analytics.setTopCustomers(getTopCustomers(businessId));

        // Add revenue trends
        analytics.setRevenueTrends(getRevenueTrends(businessId));

        // Add recent invoices
        analytics.setRecentInvoices(getRecentInvoices(businessId));

        return analytics;
    }

    public List<BusinessAnalyticsDTO.CustomerAnalyticsDTO> getTopCustomers(Long businessId) {
        List<Invoice> invoices = invoiceRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);

        Map<String, BusinessAnalyticsDTO.CustomerAnalyticsDTO> customerMap = new HashMap<>();

        for (Invoice invoice : invoices) {
            String customerEmail = invoice.getCustomerEmail();
            if (!customerMap.containsKey(customerEmail)) {
                customerMap.put(customerEmail, new BusinessAnalyticsDTO.CustomerAnalyticsDTO());
                customerMap.get(customerEmail).setCustomerEmail(customerEmail);
                customerMap.get(customerEmail).setCustomerName(customerEmail.split("@")[0]); // Simple name extraction
                customerMap.get(customerEmail).setTotalSpent(BigDecimal.ZERO);
                customerMap.get(customerEmail).setTransactionCount(0);
                customerMap.get(customerEmail).setInvoiceCount(0);
            }

            BusinessAnalyticsDTO.CustomerAnalyticsDTO customer = customerMap.get(customerEmail);
            customer.setTotalSpent(customer.getTotalSpent().add(invoice.getTotalAmount()));
            customer.setTransactionCount(customer.getTransactionCount() + 1);
            customer.setInvoiceCount(customer.getInvoiceCount() + 1);

            if ("PAID".equals(invoice.getStatus())) {
                customer.setLastTransactionDate(invoice.getUpdatedAt());
            }
        }

        return customerMap.values().stream()
                .sorted(Comparator.comparing(BusinessAnalyticsDTO.CustomerAnalyticsDTO::getTotalSpent).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<BusinessAnalyticsDTO.RevenueTrendDTO> getRevenueTrends(Long businessId) {
        List<Invoice> invoices = invoiceRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        Map<String, BusinessAnalyticsDTO.RevenueTrendDTO> dailyData = new HashMap<>();

        // Initialize last 30 days
        LocalDateTime date = thirtyDaysAgo;
        while (date.isBefore(LocalDateTime.now().plusDays(1))) {
            String dayKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            dailyData.put(dayKey, new BusinessAnalyticsDTO.RevenueTrendDTO(
                    "DAILY", dayKey, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0));
            date = date.plusDays(1);
        }

        // Process invoices
        for (Invoice invoice : invoices) {
            if (invoice.getCreatedAt() != null && invoice.getCreatedAt().isAfter(thirtyDaysAgo)) {
                String dayKey = invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                BusinessAnalyticsDTO.RevenueTrendDTO trend = dailyData.get(dayKey);
                if (trend != null) {
                    if ("PAID".equals(invoice.getStatus())) {
                        trend.setRevenue(trend.getRevenue().add(invoice.getTotalAmount()));
                    } else {
                        trend.setOutstandingRevenue(trend.getOutstandingRevenue().add(invoice.getTotalAmount()));
                    }
                    trend.setTransactionCount(trend.getTransactionCount() + 1);
                }
            }
        }

        return new ArrayList<>(dailyData.values()).stream()
                .sorted(Comparator.comparing(BusinessAnalyticsDTO.RevenueTrendDTO::getLabel))
                .collect(Collectors.toList());
    }

    public List<BusinessAnalyticsDTO.InvoiceSummaryDTO> getRecentInvoices(Long businessId) {
        List<Invoice> invoices = invoiceRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);

        return invoices.stream()
                .limit(20)
                .map(invoice -> new BusinessAnalyticsDTO.InvoiceSummaryDTO(
                        invoice.getId(),
                        "INV-" + invoice.getId(),
                        invoice.getCustomerEmail().split("@")[0], // Simple name extraction
                        invoice.getTotalAmount(),
                        invoice.getStatus().toString(),
                        invoice.getCreatedAt(),
                        invoice.getDueDate() != null ? invoice.getDueDate().atStartOfDay() : null
                ))
                .collect(Collectors.toList());
    }

    // Export Methods
    public byte[] exportPersonalTransactionsCSV(Long userId) {
        List<PersonalAnalyticsDTO.TransactionSummaryDTO> transactions = getRecentTransactions(userId);
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Type,Amount,Description,Date,Counterparty\n");
        
        for (PersonalAnalyticsDTO.TransactionSummaryDTO transaction : transactions) {
            csv.append(transaction.getId()).append(",")
               .append(transaction.getType()).append(",")
               .append(transaction.getAmount()).append(",")
               .append("\"").append(transaction.getDescription()).append("\",")
               .append(transaction.getCreatedAt()).append(",")
               .append("\"").append(transaction.getCounterpartyName()).append("\"\n");
        }
        
        return csv.toString().getBytes();
    }

    public byte[] exportBusinessInvoicesCSV(Long businessId) {
        List<BusinessAnalyticsDTO.InvoiceSummaryDTO> invoices = getRecentInvoices(businessId);
        
        StringBuilder csv = new StringBuilder();
        csv.append("Invoice Number,Customer,Amount,Status,Created Date,Due Date\n");
        
        for (BusinessAnalyticsDTO.InvoiceSummaryDTO invoice : invoices) {
            csv.append(invoice.getInvoiceNumber()).append(",")
               .append("\"").append(invoice.getCustomerName()).append("\",")
               .append(invoice.getAmount()).append(",")
               .append(invoice.getStatus()).append(",")
               .append(invoice.getCreatedAt()).append(",")
               .append(invoice.getDueDate()).append("\n");
        }
        
        return csv.toString().getBytes();
    }
}
