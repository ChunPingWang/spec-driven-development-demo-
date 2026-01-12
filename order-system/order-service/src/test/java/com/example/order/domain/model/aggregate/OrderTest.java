package com.example.order.domain.model.aggregate;

import com.example.order.domain.event.OrderCompleted;
import com.example.order.domain.event.OrderCreated;
import com.example.order.domain.event.OrderFailed;
import com.example.order.domain.exception.OrderDomainException;
import com.example.order.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order 聚合根測試")
class OrderTest {

    private Buyer buyer;
    private OrderItem orderItem;
    private Money money;
    private PaymentInfo paymentInfo;

    @BeforeEach
    void setUp() {
        buyer = Buyer.of("王小明", "ming@example.com");
        orderItem = OrderItem.of("IPHONE-17", "iPhone 17 Pro Max", 1);
        money = Money.of(new BigDecimal("35900"), "TWD");
        paymentInfo = PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "123");
    }

    @Test
    @DisplayName("建立訂單應設定初始狀態為 CREATED")
    void create_shouldSetStatusToCreated() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);

        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertNotNull(order.getOrderId());
        assertEquals("idem-key-001", order.getIdempotencyKey());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    @DisplayName("建立訂單應產生 OrderCreated 事件")
    void create_shouldRaiseOrderCreatedEvent() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);

        assertEquals(1, order.getDomainEvents().size());
        assertInstanceOf(OrderCreated.class, order.getDomainEvents().get(0));
    }

    @Test
    @DisplayName("標記支付授權成功")
    void markPaymentAuthorized_shouldTransitionToPaymentAuthorized() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);

        order.markPaymentAuthorized("PAY-123");

        assertEquals(OrderStatus.PAYMENT_AUTHORIZED, order.getStatus());
        assertEquals("PAY-123", order.getPaymentId());
    }

    @Test
    @DisplayName("從非 CREATED 狀態標記支付授權應失敗")
    void markPaymentAuthorized_shouldFailFromWrongState() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);
        order.markPaymentAuthorized("PAY-123");

        assertThrows(OrderDomainException.class,
                () -> order.markPaymentAuthorized("PAY-456"));
    }

    @Test
    @DisplayName("標記庫存扣減成功")
    void markInventoryDeducted_shouldTransitionToInventoryDeducted() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);
        order.markPaymentAuthorized("PAY-123");

        order.markInventoryDeducted();

        assertEquals(OrderStatus.INVENTORY_DEDUCTED, order.getStatus());
    }

    @Test
    @DisplayName("從非 PAYMENT_AUTHORIZED 狀態標記庫存扣減應失敗")
    void markInventoryDeducted_shouldFailFromWrongState() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);

        assertThrows(OrderDomainException.class, order::markInventoryDeducted);
    }

    @Test
    @DisplayName("完成訂單")
    void complete_shouldTransitionToCompleted() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);
        order.markPaymentAuthorized("PAY-123");
        order.markInventoryDeducted();

        order.complete();

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("完成訂單應產生 OrderCompleted 事件")
    void complete_shouldRaiseOrderCompletedEvent() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);
        order.clearDomainEvents();
        order.markPaymentAuthorized("PAY-123");
        order.markInventoryDeducted();

        order.complete();

        assertTrue(order.getDomainEvents().stream()
                .anyMatch(e -> e instanceof OrderCompleted));
    }

    @Test
    @DisplayName("標記訂單失敗")
    void fail_shouldTransitionToFailed() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);

        order.fail("Payment declined");

        assertEquals(OrderStatus.FAILED, order.getStatus());
    }

    @Test
    @DisplayName("標記訂單失敗應產生 OrderFailed 事件")
    void fail_shouldRaiseOrderFailedEvent() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);
        order.clearDomainEvents();

        order.fail("Payment declined");

        assertTrue(order.getDomainEvents().stream()
                .anyMatch(e -> e instanceof OrderFailed));
    }

    @Test
    @DisplayName("從 PAYMENT_AUTHORIZED 狀態回滾")
    void markRolledBack_shouldTransitionFromPaymentAuthorized() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);
        order.markPaymentAuthorized("PAY-123");

        order.markRolledBack("Inventory deduction failed");

        assertEquals(OrderStatus.ROLLBACK_COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("從 INVENTORY_DEDUCTED 狀態回滾")
    void markRolledBack_shouldTransitionFromInventoryDeducted() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);
        order.markPaymentAuthorized("PAY-123");
        order.markInventoryDeducted();

        order.markRolledBack("Capture failed");

        assertEquals(OrderStatus.ROLLBACK_COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("從 CREATED 狀態回滾應失敗")
    void markRolledBack_shouldFailFromCreatedState() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);

        assertThrows(OrderDomainException.class,
                () -> order.markRolledBack("Some reason"));
    }

    @Test
    @DisplayName("清除領域事件")
    void clearDomainEvents_shouldRemoveAllEvents() {
        Order order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);

        assertFalse(order.getDomainEvents().isEmpty());
        order.clearDomainEvents();
        assertTrue(order.getDomainEvents().isEmpty());
    }
}
