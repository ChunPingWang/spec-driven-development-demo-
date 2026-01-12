package com.example.inventory.domain.event;

import com.example.inventory.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockDeducted 事件測試")
class StockDeductedTest {

    @Test
    @DisplayName("建立 StockDeducted 事件應包含所有欄位")
    void of_shouldCreateEventWithAllFields() {
        ProductId productId = ProductId.of("PROD-001");

        StockDeducted event = StockDeducted.of(productId, "ORD-001", 5, 95);

        assertEquals(productId, event.productId());
        assertEquals("ORD-001", event.orderId());
        assertEquals(5, event.quantity());
        assertEquals(95, event.remainingStock());
        assertNotNull(event.occurredOn());
    }

    @Test
    @DisplayName("StockDeducted 事件應正確比較相等")
    void equals_shouldWorkCorrectly() {
        ProductId productId = ProductId.of("PROD-001");
        StockDeducted event1 = new StockDeducted(productId, "ORD-001", 5, 95,
                java.time.LocalDateTime.of(2024, 1, 1, 12, 0));
        StockDeducted event2 = new StockDeducted(productId, "ORD-001", 5, 95,
                java.time.LocalDateTime.of(2024, 1, 1, 12, 0));

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
