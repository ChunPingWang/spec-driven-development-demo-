package com.example.inventory.domain.event;

import com.example.inventory.domain.model.valueobject.ProductId;

import java.time.LocalDateTime;

/**
 * Domain event raised when stock is rolled back.
 */
public record StockRolledBack(
        ProductId productId,
        String orderId,
        int quantity,
        int newStock,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static StockRolledBack of(ProductId productId, String orderId, int quantity, int newStock) {
        return new StockRolledBack(productId, orderId, quantity, newStock, LocalDateTime.now());
    }
}
