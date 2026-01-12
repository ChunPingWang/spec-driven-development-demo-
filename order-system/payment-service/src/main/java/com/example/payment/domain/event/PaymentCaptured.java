package com.example.payment.domain.event;

import com.example.payment.domain.model.valueobject.PaymentId;

import java.time.LocalDateTime;

/**
 * Domain event raised when a payment is captured.
 */
public record PaymentCaptured(
        PaymentId paymentId,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static PaymentCaptured of(PaymentId paymentId) {
        return new PaymentCaptured(paymentId, LocalDateTime.now());
    }
}
