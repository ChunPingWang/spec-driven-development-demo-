package com.example.order.infrastructure.adapter.outbound.persistence;

import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.*;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Order domain model and JPA entity.
 */
@Component
public class OrderMapper {

    /**
     * Convert domain Order to JPA entity.
     */
    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setOrderId(order.getOrderId().value());
        entity.setIdempotencyKey(order.getIdempotencyKey());
        entity.setBuyerName(order.getBuyer().name());
        entity.setBuyerEmail(order.getBuyer().email());
        entity.setProductId(order.getOrderItem().productId());
        entity.setProductName(order.getOrderItem().productName());
        entity.setQuantity(order.getOrderItem().quantity());
        entity.setAmount(order.getMoney().amount());
        entity.setCurrency(order.getMoney().currency());
        entity.setPaymentMethod(order.getPaymentInfo().method());
        entity.setCardLastFour(order.getPaymentInfo().getLastFour());
        entity.setStatus(order.getStatus().name());
        entity.setPaymentId(order.getPaymentId());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        return entity;
    }

    /**
     * Convert JPA entity to domain Order.
     */
    public Order toDomain(OrderJpaEntity entity) {
        return Order.reconstitute(
                OrderId.of(entity.getOrderId()),
                entity.getIdempotencyKey(),
                Buyer.of(entity.getBuyerName(), entity.getBuyerEmail()),
                OrderItem.of(entity.getProductId(), entity.getProductName(), entity.getQuantity()),
                Money.of(entity.getAmount(), entity.getCurrency()),
                // PaymentInfo is reconstructed with masked data for retrieval
                PaymentInfo.of(
                        entity.getPaymentMethod(),
                        "************" + entity.getCardLastFour(),
                        "12/99", // Placeholder - not stored
                        "000"    // Placeholder - not stored
                ),
                OrderStatus.valueOf(entity.getStatus()),
                entity.getPaymentId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Update existing entity from domain Order.
     */
    public void updateEntity(OrderJpaEntity entity, Order order) {
        entity.setStatus(order.getStatus().name());
        entity.setPaymentId(order.getPaymentId());
        entity.setUpdatedAt(order.getUpdatedAt());
    }
}
