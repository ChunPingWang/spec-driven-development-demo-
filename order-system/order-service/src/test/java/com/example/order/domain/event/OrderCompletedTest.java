package com.example.order.domain.event;

import com.example.order.domain.model.valueobject.OrderId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderCompleted 事件測試")
class OrderCompletedTest {

    @Test
    @DisplayName("建立 OrderCompleted 事件應包含所有欄位")
    void of_shouldCreateEventWithAllFields() {
        OrderId orderId = OrderId.generate();

        OrderCompleted event = OrderCompleted.of(orderId);

        assertEquals(orderId, event.orderId());
        assertNotNull(event.occurredOn());
    }

    @Test
    @DisplayName("OrderCompleted 事件應正確比較相等")
    void equals_shouldWorkCorrectly() {
        OrderId orderId = OrderId.of("ORD-12345678");
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);

        OrderCompleted event1 = new OrderCompleted(orderId, time);
        OrderCompleted event2 = new OrderCompleted(orderId, time);

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
