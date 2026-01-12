package com.example.order.application.dto;

/**
 * Request DTO for inventory service calls.
 */
public record InventoryRequest(
        String orderId,
        String productId,
        int quantity
) {}
