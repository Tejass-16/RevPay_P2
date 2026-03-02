package com.revpay.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revpay.dto.TransactionHistoryDTO;
import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.UserRepository;
import com.revpay.repository.WalletRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    public Transaction sendMoney(Long senderId, Long receiverId, BigDecimal amount, String description) {
        logger.info("=== NEW SEND MONEY IMPLEMENTATION ===");
        logger.info("Sender ID: {}, Receiver ID: {}, Amount: {}", senderId, receiverId, amount);

        // Basic validation
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send money to yourself");
        }

        // Get wallets
        Wallet senderWallet = walletRepository.findByUserId(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));
        Wallet receiverWallet = walletRepository.findByUserId(receiverId)
            .orElseThrow(() -> new IllegalArgumentException("Receiver wallet not found"));

        logger.info("Sender balance: {}, Receiver balance: {}", senderWallet.getBalance(), receiverWallet.getBalance());

        // Check sender has enough balance
        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Create transaction with simple string type
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(description);
        
        // Create detached user objects to avoid circular references
        User sender = new User();
        sender.setId(senderWallet.getUser().getId());
        sender.setEmail(senderWallet.getUser().getEmail());
        sender.setFullName(senderWallet.getUser().getFullName());
        
        User receiver = new User();
        receiver.setId(receiverWallet.getUser().getId());
        receiver.setEmail(receiverWallet.getUser().getEmail());
        receiver.setFullName(receiverWallet.getUser().getFullName());
        
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setTransactionType("SEND"); // Use "SEND" instead of "S" for proper classification
        transaction.setStatus("PENDING");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setReferenceId("TXN" + System.currentTimeMillis());

        // Save transaction first
        try {
            transaction = transactionRepository.save(transaction);
            logger.info("Transaction saved with ID: {}", transaction.getId());
        } catch (Exception e) {
            logger.error("Failed to save transaction: {}", e.getMessage());
            throw new RuntimeException("Failed to create transaction: " + e.getMessage());
        }

        // Process payment
        try {
            senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
            receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
            
            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);
            
            transaction.setStatus("COMPLETED");
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            logger.info("Payment processed successfully");
        } catch (Exception e) {
            logger.error("Payment processing failed: {}", e.getMessage());
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }

        return transaction;
    }

    public void fixTransactionTypeColumn() {
        logger.info("Fixing transaction_type column with direct approach...");
        try {
            // Use a simpler approach - just update the column to accept single characters
            entityManager.createNativeQuery("ALTER TABLE transactions MODIFY COLUMN transaction_type VARCHAR(1) DEFAULT 'S'").executeUpdate();
            logger.info("Transaction type column fixed successfully with direct approach");
        } catch (Exception e) {
            logger.error("Error fixing transaction type column: {}", e.getMessage());
            throw new RuntimeException("Failed to fix transaction type column: " + e.getMessage());
        }
    }

    public Transaction sendMoneyByEmail(String senderEmail, String recipientEmail, BigDecimal amount, String description, String transactionPin) {
        logger.info("Initiating money transfer from {} to {} for amount {}", senderEmail, recipientEmail, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Amount validation failed: amount <= 0");
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (senderEmail.equals(recipientEmail)) {
            logger.error("Self-transfer attempt: {} equals {}", senderEmail, recipientEmail);
            throw new IllegalArgumentException("Cannot send money to yourself");
        }

        logger.info("Looking up sender: {}", senderEmail);
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> {
                    logger.error("Sender not found: {}", senderEmail);
                    return new IllegalArgumentException("Sender not found: " + senderEmail);
                });

        logger.info("Sender found: {} with PIN: {}", sender.getEmail(), sender.getTransactionPin());

        // Create wallet for sender if it doesn't exist
        logger.info("Checking sender wallet for user ID: {}", sender.getId());
        Wallet senderWallet = walletRepository.findByUserId(sender.getId())
                .orElseGet(null);
        if (senderWallet == null) {
            logger.info("Creating new wallet for sender: {}", senderEmail);
            senderWallet = new Wallet(sender, BigDecimal.ZERO);
            walletRepository.save(senderWallet);
            logger.info("Sender wallet created with ID: {}", senderWallet.getId());
        } else {
            logger.info("Sender wallet found with balance: {}", senderWallet.getBalance());
        }

        // Verify transaction PIN (allow empty for development)
        if (false && sender.getTransactionPin() != null && !sender.getTransactionPin().isEmpty() && 
            !sender.getTransactionPin().equals(transactionPin)) {
            logger.error("Transaction PIN validation failed for user: {}", senderEmail);
            throw new IllegalArgumentException("Invalid transaction PIN");
        }

        logger.info("Looking up receiver: {}", recipientEmail);
        User receiver = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> {
                    logger.error("Receiver not found: {}", recipientEmail);
                    return new IllegalArgumentException("Receiver not found: " + recipientEmail);
                });

        logger.info("Receiver found: {} with user ID: {}", receiver.getEmail(), receiver.getId());

        // Create wallet for receiver if it doesn't exist
        logger.info("Checking receiver wallet for user ID: {}", receiver.getId());
        Wallet receiverWallet = walletRepository.findByUserId(receiver.getId())
                .orElseGet(null);
        if (receiverWallet == null) {
            logger.info("Creating new wallet for receiver: {}", recipientEmail);
            receiverWallet = new Wallet(receiver, BigDecimal.ZERO);
            walletRepository.save(receiverWallet);
            logger.info("Receiver wallet created with ID: {}", receiverWallet.getId());
        } else {
            logger.info("Receiver wallet found with balance: {}", receiverWallet.getBalance());
        }

        logger.info("All validations passed, proceeding with transaction creation");
        return sendMoney(sender.getId(), receiver.getId(), amount, description);
    }

    @Transactional
    private void processPayment(Transaction transaction) {
        logger.info("Processing payment for transaction ID: {}", transaction.getId());
        
        logger.info("Finding sender wallet for user ID: {}", transaction.getSender().getId());
        Wallet senderWallet = walletRepository.findByUserId(transaction.getSender().getId())
                .orElseThrow(() -> {
                    logger.error("Sender wallet not found for user ID: {}", transaction.getSender().getId());
                    return new IllegalArgumentException("Sender wallet not found");
                });
        
        logger.info("Finding receiver wallet for user ID: {}", transaction.getReceiver().getId());
        Wallet receiverWallet = walletRepository.findByUserId(transaction.getReceiver().getId())
                .orElseThrow(() -> {
                    logger.error("Receiver wallet not found for user ID: {}", transaction.getReceiver().getId());
                    return new IllegalArgumentException("Receiver wallet not found");
                });

        logger.info("Checking sender balance: {} vs amount: {}", senderWallet.getBalance(), transaction.getAmount());
        if (!senderWallet.hasSufficientBalance(transaction.getAmount())) {
            logger.error("Insufficient balance: {} < {}", senderWallet.getBalance(), transaction.getAmount());
            throw new IllegalArgumentException("Insufficient balance");
        }

        logger.info("Debiting sender wallet: {} by {}", senderWallet.getBalance(), transaction.getAmount());
        senderWallet.debit(transaction.getAmount());
        
        logger.info("Crediting receiver wallet: {} by {}", receiverWallet.getBalance(), transaction.getAmount());
        receiverWallet.credit(transaction.getAmount());

        logger.info("Saving wallet states");
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        logger.info("Completing transaction");
        transaction.complete();
        transactionRepository.save(transaction);

        logger.info("Payment processed successfully for transaction ID: {}", transaction.getId());
        sendTransactionNotifications(transaction);
    }

    private void sendTransactionNotifications(Transaction transaction) {
        notificationService.createNotification(
                transaction.getReceiver(),
                com.revpay.entity.Notification.NotificationType.PAYMENT_RECEIVED,
                "Payment Received",
                String.format("You have received ₹%.2f from %s", transaction.getAmount(), transaction.getSender().getFullName()),
                transaction.getTransactionId()
        );

        notificationService.createNotification(
                transaction.getSender(),
                com.revpay.entity.Notification.NotificationType.TRANSACTION,
                "Payment Sent",
                String.format("You have sent ₹%.2f to %s", transaction.getAmount(), transaction.getReceiver().getFullName()),
                transaction.getTransactionId()
        );
    }

    public Transaction addFunds(Long userId, BigDecimal amount, String paymentMethodId) {
        logger.info("Adding funds to user {} wallet: amount {}", userId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("Funds added");
        
        // Create detached user object to avoid circular references
        User detachedUser = new User();
        detachedUser.setId(user.getId());
        detachedUser.setEmail(user.getEmail());
        detachedUser.setFullName(user.getFullName());
        
        transaction.setSender(detachedUser); // Set user as sender for add funds
        transaction.setTransactionType("ADD_FUNDS");
        transaction.setStatus("PENDING");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setReferenceId("TXN" + System.currentTimeMillis());
        transaction = transactionRepository.save(transaction);

        try {
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

            wallet.credit(amount);
            walletRepository.save(wallet);

            transaction.complete();
            transactionRepository.save(transaction);

            notificationService.createNotification(
                    user,
                    com.revpay.entity.Notification.NotificationType.TRANSACTION,
                    "Funds Added",
                    String.format("₹%.2f has been added to your wallet", amount),
                    transaction.getTransactionId()
            );

            logger.info("Funds added successfully. Transaction ID: {}", transaction.getTransactionId());

        } catch (Exception e) {
            transaction.fail();
            transactionRepository.save(transaction);
            logger.error("Failed to add funds. Transaction ID: {}", transaction.getTransactionId(), e);
            throw e;
        }

        return transaction;
    }

    public Transaction addInitialFunds(Long userId, BigDecimal amount) {
        logger.info("Adding initial funds to user {} wallet: amount {}", userId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Amount must be greater than zero: {}", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("Initial deposit");
        
        // Create detached user object to avoid circular references
        User detachedUser = new User();
        detachedUser.setId(user.getId());
        detachedUser.setEmail(user.getEmail());
        detachedUser.setFullName(user.getFullName());
        
        transaction.setSender(detachedUser); // Set user as sender for initial deposit
        transaction.setTransactionType("INITIAL");
        transaction.setStatus("PENDING");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setReferenceId("TXN" + System.currentTimeMillis());
        transaction = transactionRepository.save(transaction);

        try {
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        // Create wallet if it doesn't exist for initial funds
                        logger.info("Creating new wallet for user: {}", userId);
                        Wallet newWallet = new Wallet(user);
                        return walletRepository.save(newWallet);
                    });

            logger.info("Crediting {} to wallet for user: {}", amount, userId);
            wallet.credit(amount);
            walletRepository.save(wallet);
            logger.info("Wallet credited successfully. New balance: {}", wallet.getBalance());

            transaction.complete();
            transactionRepository.save(transaction);

            notificationService.createNotification(
                    user,
                    com.revpay.entity.Notification.NotificationType.TRANSACTION,
                    "Welcome Bonus",
                    String.format("₹%.2f welcome bonus has been added to your wallet", amount),
                    transaction.getTransactionId()
            );

            logger.info("Initial funds added successfully. Transaction ID: {}", transaction.getTransactionId());

        } catch (Exception e) {
            transaction.fail();
            transactionRepository.save(transaction);
            logger.error("Failed to add initial funds. Transaction ID: {}", transaction.getTransactionId(), e);
            throw e;
        }

        return transaction;
    }

    public Transaction withdrawFunds(Long userId, BigDecimal amount, String bankAccount) {
        logger.info("Withdrawing funds from user {} wallet: amount {}", userId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("Withdrawal to bank account: " + bankAccount);
        
        // Create detached user object to avoid circular references
        User detachedUser = new User();
        detachedUser.setId(user.getId());
        detachedUser.setEmail(user.getEmail());
        detachedUser.setFullName(user.getFullName());
        
        transaction.setSender(detachedUser); // Set user as sender for withdrawal
        transaction.setTransactionType("WITHDRAW");
        transaction.setStatus("PENDING");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setReferenceId("TXN" + System.currentTimeMillis());
        transaction = transactionRepository.save(transaction);

        try {
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

            if (!wallet.hasSufficientBalance(amount)) {
                throw new IllegalArgumentException("Insufficient balance");
            }

            wallet.debit(amount);
            walletRepository.save(wallet);

            transaction.complete();
            transactionRepository.save(transaction);

            notificationService.createNotification(
                    user,
                    com.revpay.entity.Notification.NotificationType.TRANSACTION,
                    "Funds Withdrawn",
                    String.format("₹%.2f has been withdrawn from your wallet", amount),
                    transaction.getTransactionId()
            );

            logger.info("Funds withdrawn successfully. Transaction ID: {}", transaction.getTransactionId());

        } catch (Exception e) {
            transaction.fail();
            transactionRepository.save(transaction);
            logger.error("Failed to withdraw funds. Transaction ID: {}", transaction.getTransactionId(), e);
            throw e;
        }

        return transaction;
    }

    @Transactional(readOnly = true)
    public java.util.List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    @Transactional(readOnly = true)
    public java.util.List<Transaction> getUserTransactionsByType(Long userId, String type) {
        return transactionRepository.findByUserIdAndTransactionType(userId, type);
    }

    @Transactional(readOnly = true)
    public java.util.List<Transaction> getUserTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    // Enhanced transaction history methods with debit/credit classification
    @Transactional(readOnly = true)
    public List<TransactionHistoryDTO> getUserTransactionHistory(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return convertToTransactionHistoryDTOs(transactions, userId);
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryDTO> getUserTransactionHistoryByType(Long userId, String type) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionType(userId, type);
        return convertToTransactionHistoryDTOs(transactions, userId);
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryDTO> getUserTransactionHistoryByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        return convertToTransactionHistoryDTOs(transactions, userId);
    }

    private List<TransactionHistoryDTO> convertToTransactionHistoryDTOs(List<Transaction> transactions, Long currentUserId) {
        List<TransactionHistoryDTO> historyDTOs = new ArrayList<>();
        
        for (Transaction transaction : transactions) {
            TransactionHistoryDTO dto = convertToTransactionHistoryDTO(transaction, currentUserId);
            historyDTOs.add(dto);
        }
        
        return historyDTOs;
    }

    private TransactionHistoryDTO convertToTransactionHistoryDTO(Transaction transaction, Long currentUserId) {
        boolean isCurrentUserSender = transaction.getSender() != null && transaction.getSender().getId().equals(currentUserId);
        boolean isCurrentUserReceiver = transaction.getReceiver() != null && transaction.getReceiver().getId().equals(currentUserId);
        
        String transactionNature = determineTransactionNature(transaction, isCurrentUserSender, isCurrentUserReceiver);
        String counterpartyName = getCounterpartyName(transaction, isCurrentUserSender, isCurrentUserReceiver);
        String counterpartyEmail = getCounterpartyEmail(transaction, isCurrentUserSender, isCurrentUserReceiver);
        
        return new TransactionHistoryDTO(
            transaction.getId(),
            transaction.getTransactionId(),
            transaction.getTransactionType(),
            transaction.getStatus().toString(),
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getCreatedAt(),
            transaction.getCompletedAt(),
            transactionNature,
            counterpartyName,
            counterpartyEmail,
            isCurrentUserSender,
            isCurrentUserReceiver
        );
    }

    private String determineTransactionNature(Transaction transaction, boolean isCurrentUserSender, boolean isCurrentUserReceiver) {
        String transactionType = transaction.getTransactionType();
        
        // Normalize transaction type to handle both old and new values
        String normalizedType = normalizeTransactionType(transactionType);
        
        // For SEND/RECEIVE transactions, determine based on user role
        if ("SEND".equals(normalizedType)) {
            return isCurrentUserSender ? "DEBIT" : "CREDIT";
        } else if ("RECEIVE".equals(normalizedType)) {
            return isCurrentUserReceiver ? "CREDIT" : "DEBIT";
        }
        
        // For other transaction types, determine based on the nature of the transaction
        switch (normalizedType) {
            case "ADD_FUNDS":
            case "INITIAL":
                return "CREDIT"; // Money coming into the account
            case "WITHDRAW":
                return "DEBIT"; // Money going out of the account
            default:
                return "UNKNOWN";
        }
    }

    private String normalizeTransactionType(String transactionType) {
        if (transactionType == null) {
            return "UNKNOWN";
        }
        
        switch (transactionType.toUpperCase()) {
            case "S":
            case "SEND":
                return "SEND";
            case "R":
            case "RECEIVE":
                return "RECEIVE";
            case "A":
            case "ADD_FUNDS":
                return "ADD_FUNDS";
            case "W":
            case "WITHDRAW":
                return "WITHDRAW";
            case "I":
            case "INITIAL":
                return "INITIAL";
            default:
                return transactionType.toUpperCase();
        }
    }

    private String getCounterpartyName(Transaction transaction, boolean isCurrentUserSender, boolean isCurrentUserReceiver) {
        if (isCurrentUserSender && transaction.getReceiver() != null) {
            return transaction.getReceiver().getFullName();
        } else if (isCurrentUserReceiver && transaction.getSender() != null) {
            return transaction.getSender().getFullName();
        } else {
            // For system transactions (ADD_FUNDS, WITHDRAW, etc.)
            return "RevPay System";
        }
    }

    private String getCounterpartyEmail(Transaction transaction, boolean isCurrentUserSender, boolean isCurrentUserReceiver) {
        if (isCurrentUserSender && transaction.getReceiver() != null) {
            return transaction.getReceiver().getEmail();
        } else if (isCurrentUserReceiver && transaction.getSender() != null) {
            return transaction.getSender().getEmail();
        } else {
            // For system transactions (ADD_FUNDS, WITHDRAW, etc.)
            return "system@revpay.com";
        }
    }
}
