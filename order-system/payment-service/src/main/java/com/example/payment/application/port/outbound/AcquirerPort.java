package com.example.payment.application.port.outbound;

import java.math.BigDecimal;

/**
 * Port for external payment acquirer/gateway operations.
 */
public interface AcquirerPort {

    /**
     * Result of an acquirer authorization request.
     */
    record AuthorizationResponse(
            boolean approved,
            String authorizationCode,
            String declineReason
    ) {
        public static AuthorizationResponse approved(String authorizationCode) {
            return new AuthorizationResponse(true, authorizationCode, null);
        }

        public static AuthorizationResponse declined(String reason) {
            return new AuthorizationResponse(false, null, reason);
        }
    }

    /**
     * Result of an acquirer capture request.
     */
    record CaptureResponse(
            boolean succeeded,
            String failureReason
    ) {
        public static CaptureResponse success() {
            return new CaptureResponse(true, null);
        }

        public static CaptureResponse failure(String reason) {
            return new CaptureResponse(false, reason);
        }
    }

    /**
     * Result of an acquirer void request.
     */
    record VoidResponse(
            boolean succeeded,
            String failureReason
    ) {
        public static VoidResponse success() {
            return new VoidResponse(true, null);
        }

        public static VoidResponse failure(String reason) {
            return new VoidResponse(false, reason);
        }
    }

    /**
     * Request payment authorization from acquirer.
     */
    AuthorizationResponse authorize(
            BigDecimal amount,
            String currency,
            String cardNumber,
            String expiryDate,
            String cvv
    );

    /**
     * Request capture of authorized payment.
     */
    CaptureResponse capture(String authorizationCode, BigDecimal amount);

    /**
     * Request void of authorized payment.
     */
    VoidResponse voidAuthorization(String authorizationCode);
}
