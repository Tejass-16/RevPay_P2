package com.revpay.controller;

import com.revpay.dto.TransactionHistoryDTO;
import com.revpay.entity.User;
import com.revpay.entity.Transaction;
import com.revpay.entity.Wallet;
import com.revpay.service.TransactionService;
import com.revpay.repository.UserRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @PostMapping("/test-simple")
    public ResponseEntity<?> testSimple(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Simple test received");
            return ResponseEntity.ok(Map.of("message", "Simple test working"));
        } catch (Exception e) {
            System.out.println("Error in simple test: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/fix-transaction-type")
    public ResponseEntity<?> fixTransactionTypeColumn() {
        try {
            System.out.println("Fixing transaction_type column...");
            transactionService.fixTransactionTypeColumn();
            return ResponseEntity.ok(Map.of("message", "Transaction type column fixed successfully"));
        } catch (Exception e) {
            System.out.println("Error fixing transaction type column: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> testSendMoney(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Test send money request received");
            System.out.println("Request data: " + request);
            
            return ResponseEntity.ok(Map.of("message", "Test endpoint working", "data", request));
        } catch (Exception e) {
            System.out.println("Error in test send money: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMoney(@RequestBody Map<String, Object> request, 
                                      @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Debug logging
            System.out.println("Send money request received");
            System.out.println("Authenticated user: " + (userDetails != null ? userDetails.getUsername() : "null"));
            System.out.println("Request data: " + request);
            
            if (userDetails == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            
            // Validate required fields
            if (request.get("recipientEmail") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Recipient email is required"));
            }
            
            if (request.get("amount") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            
            String recipientEmail = request.get("recipientEmail").toString();
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = request.get("description") != null ? request.get("description").toString() : "";

            // Get sender from authenticated user
            String senderEmail = userDetails.getUsername();
            
            String transactionPin = request.get("transactionPin") != null ? request.get("transactionPin").toString() : "";
            
            System.out.println("Processed request - recipient: " + recipientEmail + ", amount: " + amount + ", pin: '" + transactionPin + "'");
            
            Transaction transaction = transactionService.sendMoneyByEmail(senderEmail, recipientEmail, amount, description, transactionPin);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            System.out.println("Error in send money: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add-funds")
    public ResponseEntity<?> addFunds(@RequestBody Map<String, Object> request,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String paymentMethodId = request.get("paymentMethodId") != null ? 
                                    request.get("paymentMethodId").toString() : "";

            Transaction transaction = transactionService.addFunds(user.getId(), amount, paymentMethodId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Transaction controller is working"));
    }

    @PostMapping("/test-request")
    public ResponseEntity<?> testRequest(@RequestBody Map<String, Object> request) {
        System.out.println("Test request received: " + request);
        return ResponseEntity.ok(Map.of("received", request));
    }

    @PostMapping("/debug-request")
    public ResponseEntity<?> debugRequest(@RequestBody String requestBody) {
        System.out.println("Debug request received: " + requestBody);
        return ResponseEntity.ok(Map.of("received", requestBody));
    }

    @PostMapping("/fix-database")
    public ResponseEntity<?> fixDatabase() {
        try {
            System.out.println("Attempting to fix database schema...");
            
            // This would ideally be handled by a proper migration tool
            // For now, let's just return a message about the fix needed
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Database schema fix required");
            result.put("action", "Run the SQL script: ALTER TABLE transactions MODIFY COLUMN transaction_type VARCHAR(20)");
            result.put("file", "fix_transaction_column.sql");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error in fixDatabase: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test-database")
    public ResponseEntity<?> testDatabase() {
        try {
            System.out.println("Testing database connection...");
            
            // Test user repository
            long userCount = userRepository.count();
            System.out.println("Total users in database: " + userCount);
            
            // Test transaction repository
            long transactionCount = transactionRepository.count();
            System.out.println("Total transactions in database: " + transactionCount);
            
            // Test wallet repository
            long walletCount = walletRepository.count();
            System.out.println("Total wallets in database: " + walletCount);
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", userCount);
            result.put("transactions", transactionCount);
            result.put("wallets", walletCount);
            result.put("database", "connected");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test-enum")
    public ResponseEntity<?> testEnum(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Test enum received");
            System.out.println("Request body: " + request);
            
            // Just test the string conversion without database
            String testString = "Test transaction";
            String testType = "I";
            System.out.println("String value: " + testString);
            System.out.println("Type value: " + testType);
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "enumValue", testType,
                "enumName", "TEST",
                "message", "Enum test successful"
            ));
        } catch (Exception e) {
            System.out.println("Error in testEnum: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test-transaction")
    public ResponseEntity<?> testTransaction(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Test transaction creation received");
            System.out.println("Request body: " + request);
            
            if (request.get("amount") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            System.out.println("Creating test transaction with amount: " + amount);
            
            // Check if we have users
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No users found in database"));
            }
            
            User testUser = users.get(0);
            System.out.println("Using user: " + testUser.getEmail());
            
            // Create a simple transaction to test the enum
            Transaction testTransaction = new Transaction(
                testUser, 
                null, 
                amount, 
                "I", 
                "Test transaction"
            );
            
            testTransaction = transactionRepository.save(testTransaction);
            System.out.println("Test transaction created successfully with ID: " + testTransaction.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "transactionId", testTransaction.getId(),
                "transactionType", testTransaction.getTransactionType()
            ));
        } catch (Exception e) {
            System.out.println("Error in testTransaction: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test-amount")
    public ResponseEntity<?> testAmount(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Test amount request received");
            System.out.println("Request body: " + request);
            
            if (request.get("amount") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            System.out.println("Parsed amount: " + amount);
            System.out.println("Amount type: " + amount.getClass().getName());
            
            return ResponseEntity.ok(Map.of("success", true, "amount", amount));
        } catch (Exception e) {
            System.out.println("Error in testAmount: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add-funds-simple")
    public ResponseEntity<?> addFundsSimple(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Simple add funds request received");
            System.out.println("Request body: " + request);
            
            if (request.get("amount") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            System.out.println("Adding simple funds amount: " + amount);
            
            // First, let's check if we have any users in the database
            List<User> users = userRepository.findAll();
            System.out.println("Total users in database: " + users.size());
            
            if (users.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No users found in database. Please register a user first."));
            }
            
            // Use the first user found
            User firstUser = users.get(0);
            System.out.println("Using user: " + firstUser.getEmail() + " (ID: " + firstUser.getId() + ")");
            
            Transaction transaction = transactionService.addInitialFunds(firstUser.getId(), amount);
            System.out.println("Simple funds added successfully");
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            System.out.println("Error in addFundsSimple: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add-funds-initial")
    public ResponseEntity<?> addInitialFunds(@RequestBody Map<String, Object> request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("Add initial funds request received");
            System.out.println("Request body: " + request);
            
            if (request.get("amount") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            System.out.println("Adding initial funds amount: " + amount);
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than zero"));
            }
            
            // Get user - try authenticated first, then fallback to first user
            User user = null;
            if (userDetails != null) {
                String email = userDetails.getUsername();
                user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                System.out.println("Using authenticated user: " + email);
            } else {
                List<User> users = userRepository.findAll();
                if (users.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "No users found in database"));
                }
                user = users.get(0);
                System.out.println("Using first user in database: " + user.getEmail());
            }
            
            // Direct wallet update without transaction creation
            Wallet wallet = walletRepository.findByUserId(user.getId())
                    .orElse(null);
            
            if (wallet == null) {
                System.out.println("Creating new wallet for user: " + user.getId());
                wallet = new Wallet(user);
                wallet = walletRepository.save(wallet);
            }
            
            System.out.println("Current wallet balance: " + wallet.getBalance());
            wallet.credit(amount);
            wallet = walletRepository.save(wallet);
            System.out.println("New wallet balance: " + wallet.getBalance());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Initial funds added successfully",
                "amount", amount,
                "newBalance", wallet.getBalance()
            ));
        } catch (Exception e) {
            System.out.println("Error in addInitialFunds: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long userId) {
        try {
            List<Transaction> transactions = transactionService.getUserTransactions(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<?> getTransactionsByType(@PathVariable Long userId, @PathVariable String type) {
        try {
            List<Transaction> transactions = transactionService.getUserTransactionsByType(userId, type);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<?> getUserTransactionsByDateRange(@PathVariable Long userId,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            List<Transaction> transactions = transactionService.getUserTransactionsByDateRange(userId, start, end);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Enhanced transaction history endpoints with debit/credit classification
    @GetMapping("/history/current")
    public ResponseEntity<?> getCurrentUserTransactionHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            List<TransactionHistoryDTO> transactions = transactionService.getUserTransactionHistory(user.getId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transactions", transactions
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history/current/type/{type}")
    public ResponseEntity<?> getCurrentUserTransactionHistoryByType(@AuthenticationPrincipal UserDetails userDetails,
                                                              @PathVariable String type) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            List<TransactionHistoryDTO> transactions = transactionService.getUserTransactionHistoryByType(user.getId(), type);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transactions", transactions
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history/current/date-range")
    public ResponseEntity<?> getCurrentUserTransactionHistoryByDateRange(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @RequestParam String startDate,
                                                                   @RequestParam String endDate) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            List<TransactionHistoryDTO> transactions = transactionService.getUserTransactionHistoryByDateRange(user.getId(), start, end);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transactions", transactions
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Helper method for debugging - without authentication
    private ResponseEntity<?> addInitialFundsWithoutAuth(Map<String, Object> request) {
        try {
            System.out.println("Processing without authentication");
            
            // For debugging, use a hardcoded user ID (this should be replaced with proper auth)
            Long userId = 1L; // This is just for debugging
            
            if (request.get("amount") == null) {
                System.out.println("Amount is null in request");
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            System.out.println("Adding initial funds amount without auth: " + amount);
            
            // Add initial funds without requiring payment method for new users
            Transaction transaction = transactionService.addInitialFunds(userId, amount);
            System.out.println("Initial funds added successfully without auth");
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            System.out.println("Error in addInitialFundsWithoutAuth: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
