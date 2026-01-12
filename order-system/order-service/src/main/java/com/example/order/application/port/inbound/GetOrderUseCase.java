package com.example.order.application.port.inbound;

import com.example.order.application.query.GetOrderQuery;
import com.example.order.application.query.OrderReadModel;

import java.util.Optional;

/**
 * Use case for querying orders.
 */
public interface GetOrderUseCase {

    /**
     * Get order details by ID.
     * @param query The get order query
     * @return The order read model if found
     */
    Optional<OrderReadModel> execute(GetOrderQuery query);
}
