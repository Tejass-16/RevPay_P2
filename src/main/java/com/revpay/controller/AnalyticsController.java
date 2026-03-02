package com.revpay.controller;

import com.revpay.dto.PersonalAnalyticsDTO;
import com.revpay.dto.BusinessAnalyticsDTO;
import com.revpay.entity.User;
import com.revpay.service.AnalyticsService;
import com.revpay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UserRepository userRepository;

    // Personal Analytics Endpoints
    @GetMapping("/personal/summary")
    public ResponseEntity<?> getPersonalAnalytics(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("=== ANALYTICS DEBUG ===");
            System.out.println("User email: " + userDetails.getUsername());
            
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            System.out.println("User found: " + user.getEmail() + ", ID: " + user.getId());
            
            PersonalAnalyticsDTO analytics = analyticsService.getPersonalAnalytics(user.getId());
            System.out.println("Analytics data created: " + (analytics != null ? "NOT NULL" : "CREATED"));
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            System.out.println("ERROR in analytics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/personal/transactions/filters")
    public ResponseEntity<?> getPersonalTransactionsWithFilters(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            PersonalAnalyticsDTO analytics = analyticsService.getPersonalAnalytics(user.getId());
            
            // Apply filters to recent transactions
            List<PersonalAnalyticsDTO.TransactionSummaryDTO> filteredTransactions = 
                    analytics.getRecentTransactions().stream()
                    .filter(transaction -> {
                        if (type != null && !type.isEmpty() && !type.equals(transaction.getType())) {
                            return false;
                        }
                        if (minAmount != null && transaction.getAmount().doubleValue() < minAmount) {
                            return false;
                        }
                        if (maxAmount != null && transaction.getAmount().doubleValue() > maxAmount) {
                            return false;
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "transactions", filteredTransactions,
                "totalCount", filteredTransactions.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/personal/export/csv")
    public ResponseEntity<?> exportPersonalTransactionsCSV(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            byte[] csvData = analyticsService.exportPersonalTransactionsCSV(user.getId());
            
            String filename = "transactions_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/personal/export/pdf")
    public ResponseEntity<?> exportPersonalTransactionsPDF(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // For now, return CSV as PDF would require additional libraries
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            byte[] csvData = analyticsService.exportPersonalTransactionsCSV(user.getId());
            
            String filename = "transactions_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Business Analytics Endpoints
    @GetMapping("/business/revenue/daily")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<?> getDailyRevenue(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User business = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            BusinessAnalyticsDTO analytics = analyticsService.getBusinessAnalytics(business.getId());
            
            // Filter daily trends
            var dailyTrends = analytics.getRevenueTrends().stream()
                    .filter(trend -> "DAILY".equals(trend.getPeriod()))
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "dailyTrends", dailyTrends,
                "totalRevenue", analytics.getTotalRevenue(),
                "outstandingInvoices", analytics.getOutstandingInvoices()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/business/revenue/weekly")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<?> getWeeklyRevenue(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User business = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            BusinessAnalyticsDTO analytics = analyticsService.getBusinessAnalytics(business.getId());
            
            // Group daily trends into weekly
            Map<String, List<BusinessAnalyticsDTO.RevenueTrendDTO>> weeklyGroups = new java.util.HashMap<>();
            
            for (BusinessAnalyticsDTO.RevenueTrendDTO trend : analytics.getRevenueTrends()) {
                if ("DAILY".equals(trend.getPeriod())) {
                    String weekKey = getWeekKey(trend.getLabel());
                    weeklyGroups.computeIfAbsent(weekKey, k -> new java.util.ArrayList<>()).add(trend);
                }
            }
            
            // Aggregate weekly data
            java.util.List<BusinessAnalyticsDTO.RevenueTrendDTO> weeklyTrends = weeklyGroups.entrySet().stream()
                    .map(entry -> {
                        String weekKey = entry.getKey();
                        java.util.List<BusinessAnalyticsDTO.RevenueTrendDTO> days = entry.getValue();
                        
                        BigDecimal totalRevenue = days.stream()
                                .map(BusinessAnalyticsDTO.RevenueTrendDTO::getRevenue)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                        
                        BigDecimal totalOutstanding = days.stream()
                                .map(BusinessAnalyticsDTO.RevenueTrendDTO::getOutstandingRevenue)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                        
                        int totalTransactions = days.stream()
                                .mapToInt(BusinessAnalyticsDTO.RevenueTrendDTO::getTransactionCount)
                                .sum();
                        
                        return new BusinessAnalyticsDTO.RevenueTrendDTO(
                                "WEEKLY", weekKey, totalRevenue, totalOutstanding, totalTransactions, 0);
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "weeklyTrends", weeklyTrends,
                "totalRevenue", analytics.getTotalRevenue(),
                "outstandingInvoices", analytics.getOutstandingInvoices()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/business/revenue/monthly")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<?> getMonthlyRevenue(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User business = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            BusinessAnalyticsDTO analytics = analyticsService.getBusinessAnalytics(business.getId());
            
            // Group daily trends into monthly
            Map<String, List<BusinessAnalyticsDTO.RevenueTrendDTO>> monthlyGroups = new java.util.HashMap<>();
            
            for (BusinessAnalyticsDTO.RevenueTrendDTO trend : analytics.getRevenueTrends()) {
                if ("DAILY".equals(trend.getPeriod())) {
                    String monthKey = trend.getLabel().substring(0, 7); // yyyy-MM
                    monthlyGroups.computeIfAbsent(monthKey, k -> new java.util.ArrayList<>()).add(trend);
                }
            }
            
            // Aggregate monthly data
            java.util.List<BusinessAnalyticsDTO.RevenueTrendDTO> monthlyTrends = monthlyGroups.entrySet().stream()
                    .map(entry -> {
                        String monthKey = entry.getKey();
                        java.util.List<BusinessAnalyticsDTO.RevenueTrendDTO> days = entry.getValue();
                        
                        BigDecimal totalRevenue = days.stream()
                                .map(BusinessAnalyticsDTO.RevenueTrendDTO::getRevenue)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                        
                        BigDecimal totalOutstanding = days.stream()
                                .map(BusinessAnalyticsDTO.RevenueTrendDTO::getOutstandingRevenue)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                        
                        int totalTransactions = days.stream()
                                .mapToInt(BusinessAnalyticsDTO.RevenueTrendDTO::getTransactionCount)
                                .sum();
                        
                        return new BusinessAnalyticsDTO.RevenueTrendDTO(
                                "MONTHLY", monthKey, totalRevenue, totalOutstanding, totalTransactions, 0);
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "monthlyTrends", monthlyTrends,
                "totalRevenue", analytics.getTotalRevenue(),
                "outstandingInvoices", analytics.getOutstandingInvoices()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/business/customers/top")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<?> getTopCustomers(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User business = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            BusinessAnalyticsDTO analytics = analyticsService.getBusinessAnalytics(business.getId());
            
            return ResponseEntity.ok(Map.of(
                "topCustomers", analytics.getTopCustomers(),
                "totalCustomers", analytics.getTotalCustomers(),
                "activeCustomers", analytics.getActiveCustomers()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/business/trends/charts")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<?> getBusinessCharts(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User business = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            BusinessAnalyticsDTO analytics = analyticsService.getBusinessAnalytics(business.getId());
            
            return ResponseEntity.ok(Map.of(
                "revenueTrends", analytics.getRevenueTrends(),
                "topCustomers", analytics.getTopCustomers(),
                "totalRevenue", analytics.getTotalRevenue(),
                "outstandingInvoices", analytics.getOutstandingInvoices(),
                "paidInvoices", analytics.getPaidInvoices(),
                "totalCustomers", analytics.getTotalCustomers(),
                "activeCustomers", analytics.getActiveCustomers()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/business/export/csv")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<?> exportBusinessInvoicesCSV(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User business = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            byte[] csvData = analyticsService.exportBusinessInvoicesCSV(business.getId());
            
            String filename = "invoices_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Helper method to get week key from date string
    private String getWeekKey(String dateString) {
        // Simple implementation - in production, use proper date handling
        return "Week " + dateString.substring(8, 10); // Day of month
    }
}
