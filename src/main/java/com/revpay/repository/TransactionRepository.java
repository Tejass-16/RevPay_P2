package com.revpay.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.revpay.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.sender LEFT JOIN FETCH t.receiver WHERE t.sender.id = :senderId ORDER BY t.createdAt DESC")
    List<Transaction> findBySenderIdOrderByCreatedAtDesc(@Param("senderId") Long senderId);
    
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.sender LEFT JOIN FETCH t.receiver WHERE t.receiver.id = :receiverId ORDER BY t.createdAt DESC")
    List<Transaction> findByReceiverIdOrderByCreatedAtDesc(@Param("receiverId") Long receiverId);
    
    @Query("SELECT t FROM Transaction t WHERE (t.sender.id = :userId OR t.receiver.id = :userId) ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Transaction t WHERE (t.sender.id = :userId OR t.receiver.id = :userId) AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.transactionType = :type AND (t.sender.id = :userId OR t.receiver.id = :userId) ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndTransactionType(@Param("userId") Long userId, @Param("type") String type);
    
    @Query("SELECT t FROM Transaction t WHERE (t.sender.id = :userId OR t.receiver.id = :userId) AND t.createdAt >= :startDate ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT t FROM Transaction t WHERE (t.sender.id = :senderId OR t.receiver.id = :receiverId) AND t.createdAt >= :startDate ORDER BY t.createdAt DESC")
    List<Transaction> findBySenderIdOrReceiverIdAndCreatedAtAfter(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId, @Param("startDate") LocalDateTime startDate);
}
