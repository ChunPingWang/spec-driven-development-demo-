package com.example.order.application.dto;

/**
 * Response DTO from payment service calls.
 */
public record PaymentResponse(
        String paymentId,
        String status,
        String message
) {
    public boolean isSuccessful() {
        return "AUTHORIZED".equals(status) || "CAPTURED".equals(status);
    }
}
