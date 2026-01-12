package com.example.order.domain.event;

import com.example.order.domain.model.valueobject.OrderId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderFailed 事件測試")
class OrderFailedTest {

    @Test
    @DisplayName("建立 OrderFailed 事件應包含所有欄位")
    void of_shouldCreateEventWithAllFields() {
        OrderId orderId = OrderId.generate();

        OrderFailed event = OrderFailed.of(orderId, "支付失敗");

        assertEquals(orderId, event.orderId());
        assertEquals("支付失敗", event.reason());
        assertNotNull(event.occurredOn());
    }

    @Test
    @DisplayName("OrderFailed 事件應正確比較相等")
    void equals_shouldWorkCorrectly() {
        OrderId orderId = OrderId.of("ORD-12345678");
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);

        OrderFailed event1 = new OrderFailed(orderId, "支付失敗", time);
        OrderFailed event2 = new OrderFailed(orderId, "支付失敗", time);

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
