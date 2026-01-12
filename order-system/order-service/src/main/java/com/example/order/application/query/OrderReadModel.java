package com.example.order.application.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read model for order queries.
 */
public record OrderReadModel(
        String orderId,
        String status,
        BuyerInfo buyer,
        OrderItemInfo orderItem,
        MoneyInfo totalAmount,
        String paymentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record BuyerInfo(
            String name,
            String email
    ) {}

    public record OrderItemInfo(
            String productId,
            String productName,
            int quantity
    ) {}

    public record MoneyInfo(
            BigDecimal amount,
            String currency
    ) {}
}
