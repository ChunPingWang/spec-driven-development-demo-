package com.example.inventory.infrastructure.adapter.outbound.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductJpaEntity 測試")
class ProductJpaEntityTest {

    @Test
    @DisplayName("應正確設定和取得所有欄位")
    void shouldSetAndGetAllFields() {
        ProductJpaEntity entity = new ProductJpaEntity();
        LocalDateTime now = LocalDateTime.now();

        entity.setId(1L);
        entity.setProductId("PROD-001");
        entity.setProductName("iPhone 17");
        entity.setStockQuantity(100);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertEquals(1L, entity.getId());
        assertEquals("PROD-001", entity.getProductId());
        assertEquals("iPhone 17", entity.getProductName());
        assertEquals(100, entity.getStockQuantity());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }
}
