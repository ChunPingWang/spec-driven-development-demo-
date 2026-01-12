package com.example.order.domain.event;

import com.example.order.domain.model.valueobject.Buyer;
import com.example.order.domain.model.valueobject.OrderId;
import com.example.order.domain.model.valueobject.OrderItem;

import java.time.LocalDateTime;

/**
 * Domain event raised when an order is created.
 */
public record OrderCreated(
        OrderId orderId,
        Buyer buyer,
        OrderItem orderItem,
        LocalDateTime occurredOn
) implements DomainEvent {

    public static OrderCreated of(OrderId orderId, Buyer buyer, OrderItem orderItem) {
        return new OrderCreated(orderId, buyer, orderItem, LocalDateTime.now());
    }
}
