package com.example.order.application.command;

import java.math.BigDecimal;

/**
 * Command to create a new order.
 */
public record CreateOrderCommand(
        String idempotencyKey,
        String buyerName,
        String buyerEmail,
        String productId,
        String productName,
        int quantity,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String cardNumber,
        String expiryDate,
        String cvv
) {
    public CreateOrderCommand {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
    }
}
