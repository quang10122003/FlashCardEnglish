package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByOrderId(String orderId);

    List<PaymentTransaction> findAllByUserIdAndTransactionStatus(Long userId, String transactionStatus);
}