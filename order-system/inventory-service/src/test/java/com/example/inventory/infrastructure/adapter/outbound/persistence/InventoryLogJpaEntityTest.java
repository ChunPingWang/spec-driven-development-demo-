package com.example.inventory.infrastructure.adapter.outbound.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryLogJpaEntity 測試")
class InventoryLogJpaEntityTest {

    @Test
    @DisplayName("應正確設定和取得所有欄位")
    void shouldSetAndGetAllFields() {
        InventoryLogJpaEntity entity = new InventoryLogJpaEntity();
        LocalDateTime now = LocalDateTime.now();

        entity.setId(1L);
        entity.setOrderId("ORD-001");
        entity.setProductId("PROD-001");
        entity.setOperationType("DEDUCT");
        entity.setQuantity(10);
        entity.setStatus("COMPLETED");
        entity.setCreatedAt(now);

        assertEquals(1L, entity.getId());
        assertEquals("ORD-001", entity.getOrderId());
        assertEquals("PROD-001", entity.getProductId());
        assertEquals("DEDUCT", entity.getOperationType());
        assertEquals(10, entity.getQuantity());
        assertEquals("COMPLETED", entity.getStatus());
        assertEquals(now, entity.getCreatedAt());
    }
}
