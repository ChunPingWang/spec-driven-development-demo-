package com.example.order.application.dto;

/**
 * Response DTO from inventory service calls.
 */
public record InventoryResponse(
        String productId,
        boolean success,
        String message,
        int remainingStock
) {
    public boolean isSuccessful() {
        return success;
    }
}
