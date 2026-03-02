package com.revpay.controller;

import com.revpay.entity.MoneyRequest;
import com.revpay.entity.User;
import com.revpay.service.MoneyRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/money-requests")
public class MoneyRequestController {

    @Autowired
    private MoneyRequestService moneyRequestService;

    @PostMapping("/create")
    public ResponseEntity<?> createMoneyRequest(@RequestBody Map<String, Object> request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            Long receiverId = Long.parseLong(request.get("receiverId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = request.get("description").toString();

            MoneyRequest moneyRequest = moneyRequestService.createMoneyRequest(email, receiverId, amount, description);
            return ResponseEntity.ok(moneyRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<?> acceptMoneyRequest(@PathVariable Long requestId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            MoneyRequest moneyRequest = moneyRequestService.acceptMoneyRequest(email, requestId);
            return ResponseEntity.ok(moneyRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/decline")
    public ResponseEntity<?> declineMoneyRequest(@PathVariable Long requestId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            MoneyRequest moneyRequest = moneyRequestService.declineMoneyRequest(email, requestId);
            return ResponseEntity.ok(moneyRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelMoneyRequest(@PathVariable Long requestId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            MoneyRequest moneyRequest = moneyRequestService.cancelMoneyRequest(email, requestId);
            return ResponseEntity.ok(moneyRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        String email = userDetails.getUsername();
        List<MoneyRequest> pendingRequests = moneyRequestService.getPendingRequests(email);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getSentRequests(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        String email = userDetails.getUsername();
        List<MoneyRequest> sentRequests = moneyRequestService.getSentRequests(email);
        return ResponseEntity.ok(sentRequests);
    }

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedRequests(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        String email = userDetails.getUsername();
        List<MoneyRequest> receivedRequests = moneyRequestService.getReceivedRequests(email);
        return ResponseEntity.ok(receivedRequests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getMoneyRequest(@PathVariable Long requestId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        try {
            String email = userDetails.getUsername();
            MoneyRequest moneyRequest = moneyRequestService.getMoneyRequest(email, requestId);
            return ResponseEntity.ok(moneyRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/pay")
    public ResponseEntity<?> payMoneyRequest(@PathVariable Long requestId,
                                           @RequestBody Map<String, String> request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        try {
            String email = userDetails.getUsername();
            String transactionPin = request.get("transactionPin");
            
            if (transactionPin == null || transactionPin.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Transaction pin is required"));
            }
            
            MoneyRequest moneyRequest = moneyRequestService.payMoneyRequest(email, requestId, transactionPin);
            return ResponseEntity.ok(moneyRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}