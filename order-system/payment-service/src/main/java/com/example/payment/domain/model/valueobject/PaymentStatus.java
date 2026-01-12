package com.example.payment.domain.model.valueobject;

/**
 * Enum representing the possible states of a payment.
 */
public enum PaymentStatus {
    /**
     * Initial state - payment created but not processed.
     */
    PENDING,

    /**
     * Pre-authorization successful - funds are held.
     */
    AUTHORIZED,

    /**
     * Payment confirmed - funds transferred.
     */
    CAPTURED,

    /**
     * Authorization failed.
     */
    FAILED,

    /**
     * Authorization cancelled/voided.
     */
    VOIDED
}
