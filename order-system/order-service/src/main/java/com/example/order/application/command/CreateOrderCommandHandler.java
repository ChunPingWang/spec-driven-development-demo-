package com.example.order.application.command;

import com.example.order.application.dto.CreateOrderResponse;
import com.example.order.application.port.inbound.CreateOrderUseCase;
import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.application.saga.CreateOrderSaga;
import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Command handler for creating orders.
 * Implements idempotency check and delegates to SAGA orchestrator.
 */
@Service
@Transactional
public class CreateOrderCommandHandler implements CreateOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderCommandHandler.class);

    private final OrderRepository orderRepository;
    private final CreateOrderSaga createOrderSaga;

    public CreateOrderCommandHandler(
            OrderRepository orderRepository,
            CreateOrderSaga createOrderSaga
    ) {
        this.orderRepository = orderRepository;
        this.createOrderSaga = createOrderSaga;
    }

    @Override
    public CreateOrderResponse execute(CreateOrderCommand command) {
        log.info("Processing create order command with idempotency key: {}",
                command.idempotencyKey());

        // Check for idempotency - return existing order if found
        Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(
                command.idempotencyKey());

        if (existingOrder.isPresent()) {
            Order order = existingOrder.get();
            log.info("Found existing order for idempotency key: {}, orderId: {}",
                    command.idempotencyKey(), order.getOrderId().value());
            return CreateOrderResponse.fromExisting(
                    order.getOrderId().value(),
                    order.getStatus().name(),
                    order.getCreatedAt()
            );
        }

        // Create domain value objects
        Buyer buyer = Buyer.of(command.buyerName(), command.buyerEmail());
        OrderItem orderItem = OrderItem.of(
                command.productId(),
                command.productName(),
                command.quantity()
        );
        Money money = Money.of(command.amount(), command.currency());
        PaymentInfo paymentInfo = PaymentInfo.of(
                command.paymentMethod(),
                command.cardNumber(),
                command.expiryDate(),
                command.cvv()
        );

        // Create order aggregate
        Order order = Order.create(
                command.idempotencyKey(),
                buyer,
                orderItem,
                money,
                paymentInfo
        );

        // Save initial order state
        orderRepository.save(order);
        log.info("Created new order: {}", order.getOrderId().value());

        // Execute SAGA
        CreateOrderSaga.SagaResult result = createOrderSaga.execute(order);

        // Return appropriate response based on SAGA result
        return switch (result.status()) {
            case COMPLETED -> CreateOrderResponse.success(
                    order.getOrderId().value(),
                    order.getCreatedAt()
            );
            case PAYMENT_FAILED -> CreateOrderResponse.paymentFailed(
                    order.getOrderId().value(),
                    order.getCreatedAt()
            );
            case INVENTORY_FAILED -> CreateOrderResponse.inventoryFailed(
                    order.getOrderId().value(),
                    order.getCreatedAt()
            );
            case CAPTURE_FAILED -> CreateOrderResponse.captureFailed(
                    order.getOrderId().value(),
                    order.getCreatedAt()
            );
        };
    }
}
