package com.example.payment.application.port.outbound;

import com.example.payment.domain.model.aggregate.Payment;
import com.example.payment.domain.model.valueobject.PaymentId;

import java.util.Optional;

/**
 * Port for payment persistence operations.
 */
public interface PaymentRepository {

    /**
     * Save a payment.
     */
    Payment save(Payment payment);

    /**
     * Find a payment by its ID.
     */
    Optional<Payment> findById(PaymentId paymentId);

    /**
     * Find a payment by order ID.
     */
    Optional<Payment> findByOrderId(String orderId);
}
