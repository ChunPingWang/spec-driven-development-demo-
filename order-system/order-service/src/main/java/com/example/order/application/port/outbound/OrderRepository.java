package com.example.order.application.port.outbound;

import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.OrderId;

import java.util.Optional;

/**
 * Port for order persistence operations.
 */
public interface OrderRepository {

    /**
     * Save an order.
     */
    Order save(Order order);

    /**
     * Find an order by its ID.
     */
    Optional<Order> findById(OrderId orderId);

    /**
     * Find an order by idempotency key.
     */
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
