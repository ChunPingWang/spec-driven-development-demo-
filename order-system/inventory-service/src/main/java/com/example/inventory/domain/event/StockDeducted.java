package com.example.inventory.domain.event;

import com.example.inventory.domain.model.valueobject.ProductId;

import java.time.LocalDateTime;

/**
 * Domain event raised when stock is successfully deducted.
 */
public record StockDeducted(
        ProductId productId,
        String orderId,
        int quantity,
        int remainingStock,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static StockDeducted of(ProductId productId, String orderId, int quantity, int remainingStock) {
        return new StockDeducted(productId, orderId, quantity, remainingStock, LocalDateTime.now());
    }
}
