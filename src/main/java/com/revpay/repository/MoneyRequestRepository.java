package com.revpay.repository;

import com.revpay.entity.MoneyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoneyRequestRepository extends JpaRepository<MoneyRequest, Long> {
    
    // Find requests by requester
    List<MoneyRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    
    // Find requests by receiver
    List<MoneyRequest> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    
    // Find pending requests for a user (both sent and received)
    @Query("SELECT mr FROM MoneyRequest mr WHERE (mr.requester.id = :userId OR mr.receiver.id = :userId) AND mr.status = 'PENDING' ORDER BY mr.createdAt DESC")
    List<MoneyRequest> findPendingRequestsByUserId(@Param("userId") Long userId);
    
    // Find sent requests by user
    @Query("SELECT mr FROM MoneyRequest mr WHERE mr.requester.id = :userId ORDER BY mr.createdAt DESC")
    List<MoneyRequest> findSentRequestsByUserId(@Param("userId") Long userId);
    
    // Find received requests by user
    @Query("SELECT mr FROM MoneyRequest mr WHERE mr.receiver.id = :userId ORDER BY mr.createdAt DESC")
    List<MoneyRequest> findReceivedRequestsByUserId(@Param("userId") Long userId);
    
    // Find expired requests
    @Query("SELECT mr FROM MoneyRequest mr WHERE mr.status = 'PENDING' AND mr.expiresAt < :now")
    List<MoneyRequest> findExpiredRequests(@Param("now") LocalDateTime now);
    
    // Count pending requests for user
    @Query("SELECT COUNT(mr) FROM MoneyRequest mr WHERE (mr.requester.id = :userId OR mr.receiver.id = :userId) AND mr.status = 'PENDING'")
    Long countPendingRequestsByUserId(@Param("userId") Long userId);
}