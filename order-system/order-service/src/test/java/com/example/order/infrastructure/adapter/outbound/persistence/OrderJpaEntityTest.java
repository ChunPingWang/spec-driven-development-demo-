package com.example.order.infrastructure.adapter.outbound.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderJpaEntity 測試")
class OrderJpaEntityTest {

    @Test
    @DisplayName("應正確設定和取得所有欄位")
    void shouldSetAndGetAllFields() {
        OrderJpaEntity entity = new OrderJpaEntity();
        LocalDateTime now = LocalDateTime.now();

        entity.setId(1L);
        entity.setOrderId("ORD-001");
        entity.setIdempotencyKey("IDEM-001");
        entity.setBuyerName("張三");
        entity.setBuyerEmail("zhang@example.com");
        entity.setProductId("PROD-001");
        entity.setProductName("iPhone 17");
        entity.setQuantity(2);
        entity.setAmount(new BigDecimal("71800"));
        entity.setCurrency("TWD");
        entity.setPaymentMethod("CREDIT_CARD");
        entity.setCardLastFour("1111");
        entity.setStatus("COMPLETED");
        entity.setPaymentId("PAY-001");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertEquals(1L, entity.getId());
        assertEquals("ORD-001", entity.getOrderId());
        assertEquals("IDEM-001", entity.getIdempotencyKey());
        assertEquals("張三", entity.getBuyerName());
        assertEquals("zhang@example.com", entity.getBuyerEmail());
        assertEquals("PROD-001", entity.getProductId());
        assertEquals("iPhone 17", entity.getProductName());
        assertEquals(2, entity.getQuantity());
        assertEquals(new BigDecimal("71800"), entity.getAmount());
        assertEquals("TWD", entity.getCurrency());
        assertEquals("CREDIT_CARD", entity.getPaymentMethod());
        assertEquals("1111", entity.getCardLastFour());
        assertEquals("COMPLETED", entity.getStatus());
        assertEquals("PAY-001", entity.getPaymentId());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }
}
