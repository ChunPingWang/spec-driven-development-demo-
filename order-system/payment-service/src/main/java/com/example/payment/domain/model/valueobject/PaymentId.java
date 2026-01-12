package com.example.payment.domain.model.valueobject;

import java.util.UUID;

/**
 * Value object representing a unique payment identifier.
 */
public record PaymentId(String value) {

    public PaymentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PaymentId cannot be null or blank");
        }
    }

    /**
     * Factory method to generate a new PaymentId.
     */
    public static PaymentId generate() {
        return new PaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    /**
     * Factory method to create PaymentId from existing value.
     */
    public static PaymentId of(String value) {
        return new PaymentId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
