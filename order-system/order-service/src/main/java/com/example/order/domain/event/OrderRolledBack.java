package com.example.order.domain.event;

import com.example.order.domain.model.valueobject.OrderId;

import java.time.LocalDateTime;

/**
 * Domain event raised when an order is rolled back after compensation.
 */
public record OrderRolledBack(
        OrderId orderId,
        String reason,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static OrderRolledBack of(OrderId orderId, String reason) {
        return new OrderRolledBack(orderId, reason, LocalDateTime.now());
    }
}
