package com.revpay.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revpay.entity.MoneyRequest;
import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.repository.MoneyRequestRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.UserRepository;
import com.revpay.repository.WalletRepository;

@Service
public class MoneyRequestService {

    @Autowired
    private MoneyRequestRepository moneyRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionService transactionService;

    @Transactional
    public MoneyRequest createMoneyRequest(String requesterEmail, Long receiverId, BigDecimal amount, String description) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        if (requester.getId().equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send money request to yourself");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        MoneyRequest request = new MoneyRequest(requester, receiver, receiver, amount, description);
        return moneyRequestRepository.save(request);
    }

    @Transactional
    public MoneyRequest acceptMoneyRequest(String userEmail, Long requestId) {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Money request not found"));

        // Initialize lazy-loaded relationships to avoid LazyInitializationException
        if (request.getRequester() != null) {
            request.getRequester().getEmail(); // Initialize the proxy safely
            request.getRequester().getFullName(); // Initialize the proxy safely
        }
        if (request.getRequestedFrom() != null) {
            request.getRequestedFrom().getEmail(); // Initialize the proxy safely
            request.getRequestedFrom().getFullName(); // Initialize the proxy safely
        }
        if (request.getReceiver() != null) {
            request.getReceiver().getEmail(); // Initialize the proxy safely
            request.getReceiver().getFullName(); // Initialize the proxy safely
        }

        // Debug logging
        System.out.println("DEBUG: User trying to accept: " + userEmail);
        System.out.println("DEBUG: Request ID: " + requestId);
        System.out.println("DEBUG: Request receiver email: " + request.getReceiver().getEmail());
        System.out.println("DEBUG: Request requester email: " + request.getRequester().getEmail());
        System.out.println("DEBUG: Request status: " + request.getStatus());
        System.out.println("DEBUG: Request expired: " + request.isExpired());

        if (!request.getReceiver().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You cannot accept a request that was not sent to you. Only the receiver can accept money requests.");
        }

        if (!request.isPending()) {
            throw new IllegalArgumentException("Request is not pending. Current status: " + request.getStatus());
        }

        if (request.isExpired()) {
            throw new IllegalArgumentException("Request has expired.");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setSender(request.getReceiver());
        transaction.setReceiver(request.getRequester());
        transaction.setAmount(request.getAmount());
        transaction.setDescription("Money request payment: " + request.getPurpose());
        transaction.setStatus("COMPLETED");
        transaction.setTransactionType("SEND"); // Use "SEND" for money request payments
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setReferenceId("TXN" + System.currentTimeMillis());

        transactionRepository.save(transaction);

        // Update money request
        request.accept();
        MoneyRequest savedRequest = moneyRequestRepository.save(request);
        
        // Create notifications for money request payment
        notificationService.createNotification(
                request.getRequester(),
                com.revpay.entity.Notification.NotificationType.PAYMENT_RECEIVED,
                "Money Request Payment Received",
                String.format("You have received ₹%.2f from %s for money request: %s", 
                    request.getAmount(), request.getReceiver().getFullName(), request.getPurpose()),
                transaction.getReferenceId()
        );
        
        notificationService.createNotification(
                request.getReceiver(),
                com.revpay.entity.Notification.NotificationType.TRANSACTION,
                "Money Request Payment Sent",
                String.format("You have sent ₹%.2f to %s for money request: %s", 
                    request.getAmount(), request.getRequester().getFullName(), request.getPurpose()),
                transaction.getReferenceId()
        );
        
        // Initialize lazy-loaded relationships to avoid JSON serialization issues
        if (savedRequest.getRequester() != null) {
            savedRequest.getRequester().getEmail(); // Initialize the proxy safely
            savedRequest.getRequester().getFullName(); // Initialize the proxy safely
        }
        if (savedRequest.getRequestedFrom() != null) {
            savedRequest.getRequestedFrom().getEmail(); // Initialize the proxy safely
            savedRequest.getRequestedFrom().getFullName(); // Initialize the proxy safely
        }
        if (savedRequest.getReceiver() != null) {
            savedRequest.getReceiver().getEmail(); // Initialize the proxy safely
            savedRequest.getReceiver().getFullName(); // Initialize the proxy safely
        }
        
        return savedRequest;
    }

    @Transactional
    public MoneyRequest declineMoneyRequest(String userEmail, Long requestId) {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Money request not found"));

        if (!request.getReceiver().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You are not authorized to decline this request");
        }

        if (!request.isPending()) {
            throw new IllegalArgumentException("Request is not pending");
        }

        if (request.isExpired()) {
            throw new IllegalArgumentException("Request has expired");
        }

        request.decline();
        return moneyRequestRepository.save(request);
    }

    @Transactional
    public MoneyRequest cancelMoneyRequest(String userEmail, Long requestId) {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Money request not found"));

        if (!request.getRequester().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You are not authorized to cancel this request");
        }

        if (!request.isPending()) {
            throw new IllegalArgumentException("Request is not pending");
        }

        if (request.isExpired()) {
            throw new IllegalArgumentException("Request has expired");
        }

        request.cancel();
        return moneyRequestRepository.save(request);
    }

    @Transactional
    public List<MoneyRequest> getPendingRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<MoneyRequest> requests = moneyRequestRepository.findPendingRequestsByUserId(user.getId());
        // Initialize lazy-loaded relationships to avoid JSON serialization issues
        requests.forEach(request -> {
            if (request.getRequester() != null) {
                request.getRequester().getEmail(); // Initialize the proxy safely
                request.getRequester().getFullName(); // Initialize the proxy safely
            }
            if (request.getRequestedFrom() != null) {
                request.getRequestedFrom().getEmail(); // Initialize the proxy safely
                request.getRequestedFrom().getFullName(); // Initialize the proxy safely
            }
            if (request.getReceiver() != null) {
                request.getReceiver().getEmail(); // Initialize the proxy safely
                request.getReceiver().getFullName(); // Initialize the proxy safely
            }
        });
        return requests;
    }

    @Transactional
    public List<MoneyRequest> getSentRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<MoneyRequest> requests = moneyRequestRepository.findSentRequestsByUserId(user.getId());
        // Initialize lazy-loaded relationships to avoid JSON serialization issues
        requests.forEach(request -> {
            if (request.getRequester() != null) {
                request.getRequester().getEmail(); // Initialize the proxy safely
                request.getRequester().getFullName(); // Initialize the proxy safely
            }
            if (request.getRequestedFrom() != null) {
                request.getRequestedFrom().getEmail(); // Initialize the proxy safely
                request.getRequestedFrom().getFullName(); // Initialize the proxy safely
            }
            if (request.getReceiver() != null) {
                request.getReceiver().getEmail(); // Initialize the proxy safely
                request.getReceiver().getFullName(); // Initialize the proxy safely
            }
        });
        return requests;
    }

    @Transactional
    public List<MoneyRequest> getReceivedRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<MoneyRequest> requests = moneyRequestRepository.findReceivedRequestsByUserId(user.getId());
        // Initialize lazy-loaded relationships to avoid JSON serialization issues
        requests.forEach(request -> {
            if (request.getRequester() != null) {
                request.getRequester().getEmail(); // Initialize the proxy safely
                request.getRequester().getFullName(); // Initialize the proxy safely
            }
            if (request.getRequestedFrom() != null) {
                request.getRequestedFrom().getEmail(); // Initialize the proxy safely
                request.getRequestedFrom().getFullName(); // Initialize the proxy safely
            }
            if (request.getReceiver() != null) {
                request.getReceiver().getEmail(); // Initialize the proxy safely
                request.getReceiver().getFullName(); // Initialize the proxy safely
            }
        });
        return requests;
    }

    @Transactional
    public MoneyRequest getMoneyRequest(String userEmail, Long requestId) {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Money request not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!request.getRequester().getId().equals(user.getId()) && 
            !request.getReceiver().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to view this request");
        }

        // Initialize lazy-loaded relationships to avoid JSON serialization issues
        if (request.getRequester() != null) {
            request.getRequester().getEmail(); // Initialize the proxy safely
            request.getRequester().getFullName(); // Initialize the proxy safely
        }
        if (request.getRequestedFrom() != null) {
            request.getRequestedFrom().getEmail(); // Initialize the proxy safely
            request.getRequestedFrom().getFullName(); // Initialize the proxy safely
        }
        if (request.getReceiver() != null) {
            request.getReceiver().getEmail(); // Initialize the proxy safely
            request.getReceiver().getFullName(); // Initialize the proxy safely
        }

        return request;
    }

    @Transactional
    public MoneyRequest payMoneyRequest(String userEmail, Long requestId, String transactionPin) {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Money request not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate authorization
        if (!request.getReceiver().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to pay this request");
        }

        // Validate request status
        if (!request.isPending()) {
            throw new IllegalArgumentException("Request is not pending");
        }

        // Validate expiration
        if (request.isExpired()) {
            throw new IllegalArgumentException("Request has expired");
        }

        // Use TransactionService to process payment with transaction pin
        try {
            transactionService.sendMoneyByEmail(
                user.getEmail(),           // sender (receiver of money request)
                request.getRequester().getEmail(), // receiver (requester of money request)
                request.getAmount(),
                "Money request payment: " + request.getPurpose(),
                transactionPin
            );

            // Update money request status
            request.accept();
            return moneyRequestRepository.save(request);

        } catch (Exception e) {
            throw new IllegalArgumentException("Payment failed: " + e.getMessage());
        }
    }

    @Transactional
    public void processExpiredRequests() {
        List<MoneyRequest> expiredRequests = moneyRequestRepository.findExpiredRequests(LocalDateTime.now());
        for (MoneyRequest request : expiredRequests) {
            request.cancel();
            moneyRequestRepository.save(request);
        }
    }
}