package com.revpay.repository;

import com.revpay.entity.SocialPayment;
import com.revpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialPaymentRepository extends JpaRepository<SocialPayment, Long> {
    
    // Find payments by user
    List<SocialPayment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find payments by platform
    List<SocialPayment> findByPlatformOrderByCreatedAtDesc(SocialPayment.PaymentPlatform platform);
    
    // Find payments by status
    List<SocialPayment> findByStatusOrderByCreatedAtDesc(SocialPayment.PaymentStatus status);
    
    // Count payments by user and status
    @Query("SELECT COUNT(sp) FROM SocialPayment sp WHERE sp.user.id = :userId AND sp.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") SocialPayment.PaymentStatus status);
    
    // Find completed payments for user
    @Query("SELECT sp FROM SocialPayment sp WHERE sp.user.id = :userId AND sp.status = 'COMPLETED' ORDER BY sp.createdAt DESC")
    List<SocialPayment> findCompletedPaymentsByUserId(@Param("userId") Long userId);
}
