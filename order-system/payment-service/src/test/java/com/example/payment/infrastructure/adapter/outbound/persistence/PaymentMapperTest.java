package com.example.payment.infrastructure.adapter.outbound.persistence;

import com.example.payment.domain.model.aggregate.Payment;
import com.example.payment.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentMapper 測試")
class PaymentMapperTest {

    private PaymentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentMapper();
    }

    @Test
    @DisplayName("toEntity 應正確轉換 Payment 到 JPA Entity")
    void toEntity_shouldConvertPaymentToEntity() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-001", money, "4111111111111111", "12/26");

        PaymentJpaEntity entity = mapper.toEntity(payment);

        assertEquals(payment.getPaymentId().value(), entity.getPaymentId());
        assertEquals("ORD-001", entity.getOrderId());
        assertEquals(new BigDecimal("35900"), entity.getAmount());
        assertEquals("TWD", entity.getCurrency());
        assertEquals("1111", entity.getCardLastFour());
        assertEquals("PENDING", entity.getStatus());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("toDomain 應正確轉換 JPA Entity 到 Payment")
    void toDomain_shouldConvertEntityToPayment() {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setPaymentId("PAY-001");
        entity.setOrderId("ORD-001");
        entity.setAmount(new BigDecimal("35900"));
        entity.setCurrency("TWD");
        entity.setCardLastFour("1111");
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Payment payment = mapper.toDomain(entity);

        assertEquals("PAY-001", payment.getPaymentId().value());
        assertEquals("ORD-001", payment.getOrderId());
        assertEquals(new BigDecimal("35900"), payment.getMoney().amount());
        assertEquals("TWD", payment.getMoney().currency());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    @DisplayName("updateEntity 應正確更新 Entity")
    void updateEntity_shouldUpdateEntityFromPayment() {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setPaymentId("PAY-001");
        entity.setOrderId("ORD-001");
        entity.setAmount(new BigDecimal("35900"));
        entity.setCurrency("TWD");
        entity.setCardLastFour("1111");
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Payment payment = Payment.reconstitute(
                PaymentId.of("PAY-001"),
                "ORD-001",
                Money.of(new BigDecimal("35900"), "TWD"),
                CardInfo.of("1111", "12/26"),
                PaymentStatus.AUTHORIZED,
                "AUTH-123",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        mapper.updateEntity(entity, payment);

        assertEquals("AUTHORIZED", entity.getStatus());
        assertEquals("AUTH-123", entity.getAuthorizationCode());
    }
}
