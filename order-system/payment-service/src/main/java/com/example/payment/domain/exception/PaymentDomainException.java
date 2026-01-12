package com.example.payment.domain.exception;

/**
 * Exception thrown for payment domain violations.
 */
public class PaymentDomainException extends RuntimeException {

    public PaymentDomainException(String message) {
        super(message);
    }

    public PaymentDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PaymentDomainException invalidStateTransition(String from, String to) {
        return new PaymentDomainException(
                String.format("Invalid payment state transition from %s to %s", from, to));
    }

    public static PaymentDomainException authorizationFailed(String reason) {
        return new PaymentDomainException("Payment authorization failed: " + reason);
    }

    public static PaymentDomainException captureFailed(String reason) {
        return new PaymentDomainException("Payment capture failed: " + reason);
    }
}
