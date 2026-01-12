package com.example.order.domain.event;

import com.example.order.domain.model.valueobject.Buyer;
import com.example.order.domain.model.valueobject.OrderId;
import com.example.order.domain.model.valueobject.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderCreated 事件測試")
class OrderCreatedTest {

    @Test
    @DisplayName("建立 OrderCreated 事件應包含所有欄位")
    void of_shouldCreateEventWithAllFields() {
        OrderId orderId = OrderId.generate();
        Buyer buyer = Buyer.of("張三", "zhang@example.com");
        OrderItem orderItem = OrderItem.of("PROD-001", "iPhone 17", 1);

        OrderCreated event = OrderCreated.of(orderId, buyer, orderItem);

        assertEquals(orderId, event.orderId());
        assertEquals(buyer, event.buyer());
        assertEquals(orderItem, event.orderItem());
        assertNotNull(event.occurredOn());
    }

    @Test
    @DisplayName("OrderCreated 事件應正確比較相等")
    void equals_shouldWorkCorrectly() {
        OrderId orderId = OrderId.of("ORD-12345678");
        Buyer buyer = Buyer.of("張三", "zhang@example.com");
        OrderItem orderItem = OrderItem.of("PROD-001", "iPhone 17", 1);
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);

        OrderCreated event1 = new OrderCreated(orderId, buyer, orderItem, time);
        OrderCreated event2 = new OrderCreated(orderId, buyer, orderItem, time);

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
