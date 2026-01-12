package com.example.order.application.query;

/**
 * Query to get order details.
 */
public record GetOrderQuery(
        String orderId
) {
    public GetOrderQuery {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }
    }
}
