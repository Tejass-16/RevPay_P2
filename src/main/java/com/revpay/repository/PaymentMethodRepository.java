package com.revpay.repository;

import com.revpay.entity.PaymentMethod;
import com.revpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByUserId(Long userId);
    Optional<PaymentMethod> findByUserIdAndIsDefault(Long userId, boolean isDefault);
    List<PaymentMethod> findByUserIdAndIsActive(Long userId, boolean isActive);
    void deleteByUserId(Long userId);
}
