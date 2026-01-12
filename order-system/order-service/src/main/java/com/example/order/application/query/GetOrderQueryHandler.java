package com.example.order.application.query;

import com.example.order.application.port.inbound.GetOrderUseCase;
import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.OrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Query handler for getting order details.
 */
@Service
@Transactional(readOnly = true)
public class GetOrderQueryHandler implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderQueryHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Optional<OrderReadModel> execute(GetOrderQuery query) {
        OrderId orderId = OrderId.of(query.orderId());

        return orderRepository.findById(orderId)
                .map(this::toReadModel);
    }

    private OrderReadModel toReadModel(Order order) {
        return new OrderReadModel(
                order.getOrderId().value(),
                order.getStatus().name(),
                new OrderReadModel.BuyerInfo(
                        order.getBuyer().name(),
                        order.getBuyer().email()
                ),
                new OrderReadModel.OrderItemInfo(
                        order.getOrderItem().productId(),
                        order.getOrderItem().productName(),
                        order.getOrderItem().quantity()
                ),
                new OrderReadModel.MoneyInfo(
                        order.getMoney().amount(),
                        order.getMoney().currency()
                ),
                order.getPaymentId(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
