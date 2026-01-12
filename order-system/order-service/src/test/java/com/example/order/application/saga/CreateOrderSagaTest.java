package com.example.order.application.saga;

import com.example.order.application.port.outbound.InventoryServicePort;
import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.application.port.outbound.PaymentServicePort;
import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderSaga 測試")
class CreateOrderSagaTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentServicePort paymentServicePort;
    @Mock
    private InventoryServicePort inventoryServicePort;

    private CreateOrderSaga saga;
    private Order order;

    @BeforeEach
    void setUp() {
        saga = new CreateOrderSaga(orderRepository, paymentServicePort, inventoryServicePort);

        Buyer buyer = Buyer.of("王小明", "ming@example.com");
        OrderItem orderItem = OrderItem.of("IPHONE-17", "iPhone 17 Pro Max", 1);
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        PaymentInfo paymentInfo = PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "123");

        order = Order.create("idem-key-001", buyer, orderItem, money, paymentInfo);

        when(orderRepository.save(any(Order.class))).thenReturn(order);
    }

    @Test
    @DisplayName("成功案例：完整 SAGA 流程")
    void execute_shouldCompleteSuccessfully() {
        // Arrange
        when(paymentServicePort.authorize(anyString(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(PaymentServicePort.AuthorizationResult.success("PAY-123", "AUTH-456"));
        when(inventoryServicePort.deductStock(anyString(), anyString(), anyInt()))
                .thenReturn(InventoryServicePort.DeductionResult.success(9));
        when(paymentServicePort.capture(anyString()))
                .thenReturn(PaymentServicePort.CaptureResult.success());

        // Act
        CreateOrderSaga.SagaResult result = saga.execute(order);

        // Assert
        assertTrue(result.succeeded());
        assertEquals(CreateOrderSaga.SagaStatus.COMPLETED, result.status());
        verify(orderRepository, atLeast(3)).save(any(Order.class));
    }

    @Test
    @DisplayName("支付授權失敗：訂單標記為 FAILED")
    void execute_shouldFailWhenPaymentAuthorizationFails() {
        // Arrange
        when(paymentServicePort.authorize(anyString(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(PaymentServicePort.AuthorizationResult.failure("Insufficient funds"));

        // Act
        CreateOrderSaga.SagaResult result = saga.execute(order);

        // Assert
        assertFalse(result.succeeded());
        assertEquals(CreateOrderSaga.SagaStatus.PAYMENT_FAILED, result.status());
        verify(inventoryServicePort, never()).deductStock(anyString(), anyString(), anyInt());
        verify(paymentServicePort, never()).capture(anyString());
    }

    @Test
    @DisplayName("庫存扣減失敗：應取消支付授權")
    void execute_shouldVoidPaymentWhenInventoryDeductionFails() {
        // Arrange
        when(paymentServicePort.authorize(anyString(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(PaymentServicePort.AuthorizationResult.success("PAY-123", "AUTH-456"));
        when(inventoryServicePort.deductStock(anyString(), anyString(), anyInt()))
                .thenReturn(InventoryServicePort.DeductionResult.failure("Insufficient stock"));
        when(paymentServicePort.voidPayment(anyString()))
                .thenReturn(PaymentServicePort.VoidResult.success());

        // Act
        CreateOrderSaga.SagaResult result = saga.execute(order);

        // Assert
        assertFalse(result.succeeded());
        assertEquals(CreateOrderSaga.SagaStatus.INVENTORY_FAILED, result.status());
        verify(paymentServicePort).voidPayment("PAY-123");
        verify(paymentServicePort, never()).capture(anyString());
    }

    @Test
    @DisplayName("支付請款失敗：應回滾庫存並取消支付授權")
    void execute_shouldRollbackInventoryAndVoidPaymentWhenCaptureFails() {
        // Arrange
        when(paymentServicePort.authorize(anyString(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(PaymentServicePort.AuthorizationResult.success("PAY-123", "AUTH-456"));
        when(inventoryServicePort.deductStock(anyString(), anyString(), anyInt()))
                .thenReturn(InventoryServicePort.DeductionResult.success(9));
        when(paymentServicePort.capture(anyString()))
                .thenReturn(PaymentServicePort.CaptureResult.failure("Capture failed"));
        when(inventoryServicePort.rollbackStock(anyString(), anyString(), anyInt()))
                .thenReturn(InventoryServicePort.RollbackResult.success(10));
        when(paymentServicePort.voidPayment(anyString()))
                .thenReturn(PaymentServicePort.VoidResult.success());

        // Act
        CreateOrderSaga.SagaResult result = saga.execute(order);

        // Assert
        assertFalse(result.succeeded());
        assertEquals(CreateOrderSaga.SagaStatus.CAPTURE_FAILED, result.status());
        verify(inventoryServicePort).rollbackStock(anyString(), eq("IPHONE-17"), eq(1));
        verify(paymentServicePort).voidPayment("PAY-123");
    }

    @Test
    @DisplayName("補償失敗不應影響主流程結果")
    void execute_shouldHandleCompensationFailureGracefully() {
        // Arrange
        when(paymentServicePort.authorize(anyString(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(PaymentServicePort.AuthorizationResult.success("PAY-123", "AUTH-456"));
        when(inventoryServicePort.deductStock(anyString(), anyString(), anyInt()))
                .thenReturn(InventoryServicePort.DeductionResult.failure("Insufficient stock"));
        when(paymentServicePort.voidPayment(anyString()))
                .thenReturn(PaymentServicePort.VoidResult.failure("Void failed"));

        // Act
        CreateOrderSaga.SagaResult result = saga.execute(order);

        // Assert
        assertFalse(result.succeeded());
        assertEquals(CreateOrderSaga.SagaStatus.INVENTORY_FAILED, result.status());
    }
}
