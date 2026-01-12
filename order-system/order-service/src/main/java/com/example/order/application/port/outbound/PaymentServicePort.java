package com.example.order.application.port.outbound;

import java.math.BigDecimal;

/**
 * Port for payment service operations.
 */
public interface PaymentServicePort {

    /**
     * Result of a payment authorization.
     */
    record AuthorizationResult(
            boolean success,
            String paymentId,
            String authorizationCode,
            String message
    ) {
        public static AuthorizationResult success(String paymentId, String authorizationCode) {
            return new AuthorizationResult(true, paymentId, authorizationCode, "Payment authorized");
        }

        public static AuthorizationResult failure(String message) {
            return new AuthorizationResult(false, null, null, message);
        }
    }

    /**
     * Result of a payment capture.
     */
    record CaptureResult(
            boolean succeeded,
            String message
    ) {
        public static CaptureResult success() {
            return new CaptureResult(true, "Payment captured");
        }

        public static CaptureResult failure(String message) {
            return new CaptureResult(false, message);
        }
    }

    /**
     * Result of a payment void.
     */
    record VoidResult(
            boolean succeeded,
            String message
    ) {
        public static VoidResult success() {
            return new VoidResult(true, "Payment voided");
        }

        public static VoidResult failure(String message) {
            return new VoidResult(false, message);
        }
    }

    /**
     * Authorize a payment.
     */
    AuthorizationResult authorize(
            String orderId,
            BigDecimal amount,
            String currency,
            String cardNumber,
            String expiryDate,
            String cvv
    );

    /**
     * Capture an authorized payment.
     */
    CaptureResult capture(String paymentId);

    /**
     * Void an authorized payment.
     */
    VoidResult voidPayment(String paymentId);
}
