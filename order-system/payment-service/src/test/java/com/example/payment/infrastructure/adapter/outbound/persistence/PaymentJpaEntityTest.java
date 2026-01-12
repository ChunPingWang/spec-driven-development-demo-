package com.example.payment.infrastructure.adapter.outbound.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentJpaEntity 測試")
class PaymentJpaEntityTest {

    @Test
    @DisplayName("應正確設定和取得所有欄位")
    void shouldSetAndGetAllFields() {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        LocalDateTime now = LocalDateTime.now();

        entity.setId(1L);
        entity.setPaymentId("PAY-001");
        entity.setOrderId("ORD-001");
        entity.setAmount(new BigDecimal("35900"));
        entity.setCurrency("TWD");
        entity.setCardLastFour("1111");
        entity.setStatus("AUTHORIZED");
        entity.setAuthorizationCode("AUTH-123");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertEquals(1L, entity.getId());
        assertEquals("PAY-001", entity.getPaymentId());
        assertEquals("ORD-001", entity.getOrderId());
        assertEquals(new BigDecimal("35900"), entity.getAmount());
        assertEquals("TWD", entity.getCurrency());
        assertEquals("1111", entity.getCardLastFour());
        assertEquals("AUTHORIZED", entity.getStatus());
        assertEquals("AUTH-123", entity.getAuthorizationCode());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }
}
