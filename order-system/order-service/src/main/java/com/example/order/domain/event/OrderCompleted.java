package com.example.order.domain.event;

import com.example.order.domain.model.valueobject.OrderId;

import java.time.LocalDateTime;

/**
 * Domain event raised when an order is completed successfully.
 */
public record OrderCompleted(
        OrderId orderId,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static OrderCompleted of(OrderId orderId) {
        return new OrderCompleted(orderId, LocalDateTime.now());
    }
}
