package com.example.inventory.domain.event;

import com.example.inventory.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockRolledBack 事件測試")
class StockRolledBackTest {

    @Test
    @DisplayName("建立 StockRolledBack 事件應包含所有欄位")
    void of_shouldCreateEventWithAllFields() {
        ProductId productId = ProductId.of("PROD-001");

        StockRolledBack event = StockRolledBack.of(productId, "ORD-001", 5, 100);

        assertEquals(productId, event.productId());
        assertEquals("ORD-001", event.orderId());
        assertEquals(5, event.quantity());
        assertEquals(100, event.newStock());
        assertNotNull(event.occurredOn());
    }

    @Test
    @DisplayName("StockRolledBack 事件應正確比較相等")
    void equals_shouldWorkCorrectly() {
        ProductId productId = ProductId.of("PROD-001");
        StockRolledBack event1 = new StockRolledBack(productId, "ORD-001", 5, 100,
                java.time.LocalDateTime.of(2024, 1, 1, 12, 0));
        StockRolledBack event2 = new StockRolledBack(productId, "ORD-001", 5, 100,
                java.time.LocalDateTime.of(2024, 1, 1, 12, 0));

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
