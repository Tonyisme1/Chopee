package com.chopee.repository;

import com.chopee.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByGroupOrderCode(String groupOrderCode);
    Optional<Payment> findByTransactionNo(String transactionNo);
}

