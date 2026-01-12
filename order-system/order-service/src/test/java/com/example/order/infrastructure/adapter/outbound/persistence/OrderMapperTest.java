package com.example.order.infrastructure.adapter.outbound.persistence;

import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderMapper 測試")
class OrderMapperTest {

    private OrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderMapper();
    }

    @Test
    @DisplayName("toEntity 應正確轉換 Order 到 JPA Entity")
    void toEntity_shouldConvertOrderToEntity() {
        Order order = Order.create(
                "IDEM-001",
                Buyer.of("張三", "zhang@example.com"),
                OrderItem.of("PROD-001", "iPhone 17", 1),
                Money.of(new BigDecimal("35900"), "TWD"),
                PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "123")
        );

        OrderJpaEntity entity = mapper.toEntity(order);

        assertEquals(order.getOrderId().value(), entity.getOrderId());
        assertEquals("IDEM-001", entity.getIdempotencyKey());
        assertEquals("張三", entity.getBuyerName());
        assertEquals("zhang@example.com", entity.getBuyerEmail());
        assertEquals("PROD-001", entity.getProductId());
        assertEquals("iPhone 17", entity.getProductName());
        assertEquals(1, entity.getQuantity());
        assertEquals(new BigDecimal("35900"), entity.getAmount());
        assertEquals("TWD", entity.getCurrency());
        assertEquals("CREDIT_CARD", entity.getPaymentMethod());
        assertEquals("1111", entity.getCardLastFour());
        assertEquals("CREATED", entity.getStatus());
    }

    @Test
    @DisplayName("toDomain 應正確轉換 JPA Entity 到 Order")
    void toDomain_shouldConvertEntityToOrder() {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setOrderId("ORD-12345678");
        entity.setIdempotencyKey("IDEM-001");
        entity.setBuyerName("張三");
        entity.setBuyerEmail("zhang@example.com");
        entity.setProductId("PROD-001");
        entity.setProductName("iPhone 17");
        entity.setQuantity(1);
        entity.setAmount(new BigDecimal("35900"));
        entity.setCurrency("TWD");
        entity.setPaymentMethod("CREDIT_CARD");
        entity.setCardLastFour("1111");
        entity.setStatus("CREATED");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Order order = mapper.toDomain(entity);

        assertEquals("ORD-12345678", order.getOrderId().value());
        assertEquals("IDEM-001", order.getIdempotencyKey());
        assertEquals("張三", order.getBuyer().name());
        assertEquals("zhang@example.com", order.getBuyer().email());
        assertEquals("PROD-001", order.getOrderItem().productId());
        assertEquals(1, order.getOrderItem().quantity());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    @DisplayName("updateEntity 應正確更新 Entity")
    void updateEntity_shouldUpdateEntityFromOrder() {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setOrderId("ORD-12345678");
        entity.setIdempotencyKey("IDEM-001");
        entity.setBuyerName("張三");
        entity.setBuyerEmail("zhang@example.com");
        entity.setProductId("PROD-001");
        entity.setProductName("iPhone 17");
        entity.setQuantity(1);
        entity.setAmount(new BigDecimal("35900"));
        entity.setCurrency("TWD");
        entity.setStatus("CREATED");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Order order = Order.reconstitute(
                OrderId.of("ORD-12345678"),
                "IDEM-001",
                Buyer.of("張三", "zhang@example.com"),
                OrderItem.of("PROD-001", "iPhone 17", 1),
                Money.of(new BigDecimal("35900"), "TWD"),
                PaymentInfo.of("CREDIT_CARD", "************1111", "12/99", "000"),
                OrderStatus.PAYMENT_AUTHORIZED,
                "PAY-001",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        mapper.updateEntity(entity, order);

        assertEquals("PAYMENT_AUTHORIZED", entity.getStatus());
        assertEquals("PAY-001", entity.getPaymentId());
    }
}
