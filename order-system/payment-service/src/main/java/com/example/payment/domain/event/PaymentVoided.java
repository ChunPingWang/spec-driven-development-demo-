package com.example.payment.domain.event;

import com.example.payment.domain.model.valueobject.PaymentId;

import java.time.LocalDateTime;

/**
 * Domain event raised when a payment is voided.
 */
public record PaymentVoided(
        PaymentId paymentId,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static PaymentVoided of(PaymentId paymentId) {
        return new PaymentVoided(paymentId, LocalDateTime.now());
    }
}
