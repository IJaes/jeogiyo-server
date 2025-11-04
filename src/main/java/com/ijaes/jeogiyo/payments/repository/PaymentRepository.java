package com.ijaes.jeogiyo.payments.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ijaes.jeogiyo.payments.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
	Optional<Object> findByPaymentKey(String paymentKey);
}
