package com.example.order.domain.event;

import com.example.order.domain.model.valueobject.OrderId;

import java.time.LocalDateTime;

/**
 * Domain event raised when an order fails (early failure, no compensation needed).
 */
public record OrderFailed(
        OrderId orderId,
        String reason,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static OrderFailed of(OrderId orderId, String reason) {
        return new OrderFailed(orderId, reason, LocalDateTime.now());
    }
}
