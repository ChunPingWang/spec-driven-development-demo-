package com.example.order.application.port.inbound;

import com.example.order.application.command.CreateOrderCommand;
import com.example.order.application.dto.CreateOrderResponse;

/**
 * Use case for creating orders.
 */
public interface CreateOrderUseCase {

    /**
     * Create a new order and execute the order SAGA.
     * @param command The create order command
     * @return The response with order ID and status
     */
    CreateOrderResponse execute(CreateOrderCommand command);
}
