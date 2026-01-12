package com.example.order.application.query;

import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrderQueryHandler 測試")
class GetOrderQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    private GetOrderQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetOrderQueryHandler(orderRepository);
    }

    private Order createOrder(String orderId) {
        Order order = Order.create(
                "IDEMP-001",
                Buyer.of("王小明", "wang@example.com"),
                OrderItem.of("IPHONE-17", "iPhone 17 Pro Max", 1),
                Money.of(new BigDecimal("39900"), "TWD"),
                PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "123")
        );
        // Use reflection to set orderId for testing
        try {
            var field = Order.class.getDeclaredField("orderId");
            field.setAccessible(true);
            field.set(order, OrderId.of(orderId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    @Test
    @DisplayName("成功查詢訂單")
    void execute_shouldReturnOrderWhenFound() {
        // Arrange
        String orderId = "ORD-A1234567";
        Order order = createOrder(orderId);
        order.markPaymentAuthorized("PAY-001");
        order.markInventoryDeducted();
        order.complete();

        when(orderRepository.findById(any(OrderId.class)))
                .thenReturn(Optional.of(order));

        GetOrderQuery query = new GetOrderQuery(orderId);

        // Act
        Optional<OrderReadModel> result = handler.execute(query);

        // Assert
        assertTrue(result.isPresent());
        OrderReadModel readModel = result.get();

        assertEquals(orderId, readModel.orderId());
        assertEquals("COMPLETED", readModel.status());
        assertEquals("王小明", readModel.buyer().name());
        assertEquals("wang@example.com", readModel.buyer().email());
        assertEquals("IPHONE-17", readModel.orderItem().productId());
        assertEquals("iPhone 17 Pro Max", readModel.orderItem().productName());
        assertEquals(1, readModel.orderItem().quantity());
        assertEquals(new BigDecimal("39900"), readModel.totalAmount().amount());
        assertEquals("TWD", readModel.totalAmount().currency());
        assertNotNull(readModel.createdAt());
    }

    @Test
    @DisplayName("訂單不存在應返回空")
    void execute_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(orderRepository.findById(any(OrderId.class)))
                .thenReturn(Optional.empty());

        GetOrderQuery query = new GetOrderQuery("ORD-NOTFOUND");

        // Act
        Optional<OrderReadModel> result = handler.execute(query);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查詢失敗狀態的訂單")
    void execute_shouldReturnFailedOrderStatus() {
        // Arrange
        String orderId = "ORD-B2345678";
        Order order = createOrder(orderId);
        order.fail("Payment declined");

        when(orderRepository.findById(any(OrderId.class)))
                .thenReturn(Optional.of(order));

        GetOrderQuery query = new GetOrderQuery(orderId);

        // Act
        Optional<OrderReadModel> result = handler.execute(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("FAILED", result.get().status());
    }

    @Test
    @DisplayName("查詢回滾狀態的訂單")
    void execute_shouldReturnRollbackStatus() {
        // Arrange
        String orderId = "ORD-C3456789";
        Order order = createOrder(orderId);
        order.markPaymentAuthorized("PAY-001");
        order.markRolledBack("Inventory insufficient");

        when(orderRepository.findById(any(OrderId.class)))
                .thenReturn(Optional.of(order));

        GetOrderQuery query = new GetOrderQuery(orderId);

        // Act
        Optional<OrderReadModel> result = handler.execute(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("ROLLBACK_COMPLETED", result.get().status());
    }

    @Test
    @DisplayName("應正確轉換訂單為讀取模型")
    void execute_shouldConvertOrderToReadModelCorrectly() {
        // Arrange
        String orderId = "ORD-D4567890";
        Order order = createOrder(orderId);
        order.markPaymentAuthorized("PAY-123");

        when(orderRepository.findById(any(OrderId.class)))
                .thenReturn(Optional.of(order));

        GetOrderQuery query = new GetOrderQuery(orderId);

        // Act
        Optional<OrderReadModel> result = handler.execute(query);

        // Assert
        assertTrue(result.isPresent());
        OrderReadModel readModel = result.get();
        assertEquals("PAY-123", readModel.paymentId());
    }
}
