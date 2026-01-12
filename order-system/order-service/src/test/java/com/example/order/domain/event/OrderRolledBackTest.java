package com.example.order.domain.event;

import com.example.order.domain.model.valueobject.OrderId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderRolledBack 事件測試")
class OrderRolledBackTest {

    @Test
    @DisplayName("建立 OrderRolledBack 事件應包含所有欄位")
    void of_shouldCreateEventWithAllFields() {
        OrderId orderId = OrderId.generate();

        OrderRolledBack event = OrderRolledBack.of(orderId, "庫存不足");

        assertEquals(orderId, event.orderId());
        assertEquals("庫存不足", event.reason());
        assertNotNull(event.occurredOn());
    }

    @Test
    @DisplayName("OrderRolledBack 事件應正確比較相等")
    void equals_shouldWorkCorrectly() {
        OrderId orderId = OrderId.of("ORD-12345678");
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);

        OrderRolledBack event1 = new OrderRolledBack(orderId, "庫存不足", time);
        OrderRolledBack event2 = new OrderRolledBack(orderId, "庫存不足", time);

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
