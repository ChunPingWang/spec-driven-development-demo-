package com.example.payment.domain.event;

import com.example.payment.domain.model.valueobject.PaymentId;

import java.time.LocalDateTime;

/**
 * Domain event raised when a payment is authorized.
 */
public record PaymentAuthorized(
        PaymentId paymentId,
        String authorizationCode,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static PaymentAuthorized of(PaymentId paymentId, String authorizationCode) {
        return new PaymentAuthorized(paymentId, authorizationCode, LocalDateTime.now());
    }
}
