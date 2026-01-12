package com.example.order.application.command;

import com.example.order.application.dto.CreateOrderResponse;
import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.application.saga.CreateOrderSaga;
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
@DisplayName("CreateOrderCommandHandler 測試")
class CreateOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CreateOrderSaga createOrderSaga;

    private CreateOrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateOrderCommandHandler(orderRepository, createOrderSaga);
    }

    private CreateOrderCommand createCommand(String idempotencyKey) {
        return new CreateOrderCommand(
                idempotencyKey,
                "王小明",
                "wang@example.com",
                "IPHONE-17",
                "iPhone 17 Pro Max",
                1,
                new BigDecimal("39900"),
                "TWD",
                "CREDIT_CARD",
                "4111111111111111",
                "12/26",
                "123"
        );
    }

    @Test
    @DisplayName("成功建立訂單")
    void execute_shouldCreateOrderSuccessfully() {
        // Arrange
        CreateOrderCommand command = createCommand("IDEMP-001");

        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(createOrderSaga.execute(any(Order.class)))
                .thenReturn(CreateOrderSaga.SagaResult.success());

        // Act
        CreateOrderResponse response = handler.execute(command);

        // Assert
        assertEquals("COMPLETED", response.status());
        assertEquals("訂購成功", response.message());
        assertNotNull(response.orderId());
        assertNotNull(response.createdAt());

        verify(orderRepository).save(any(Order.class));
        verify(createOrderSaga).execute(any(Order.class));
    }

    @Test
    @DisplayName("冪等性：重複請求應返回現有訂單")
    void execute_shouldReturnExistingOrderForDuplicateRequest() {
        // Arrange
        CreateOrderCommand command = createCommand("IDEMP-001");

        Order existingOrder = Order.create(
                "IDEMP-001",
                Buyer.of("王小明", "wang@example.com"),
                OrderItem.of("IPHONE-17", "iPhone 17 Pro Max", 1),
                Money.of(new BigDecimal("39900"), "TWD"),
                PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "123")
        );
        existingOrder.markPaymentAuthorized("PAY-001");
        existingOrder.markInventoryDeducted();
        existingOrder.complete();

        when(orderRepository.findByIdempotencyKey("IDEMP-001"))
                .thenReturn(Optional.of(existingOrder));

        // Act
        CreateOrderResponse response = handler.execute(command);

        // Assert
        assertEquals("COMPLETED", response.status());
        assertEquals("訂購成功", response.message());

        verify(orderRepository, never()).save(any());
        verify(createOrderSaga, never()).execute(any());
    }

    @Test
    @DisplayName("支付失敗應返回支付失敗狀態")
    void execute_shouldReturnPaymentFailedWhenPaymentFails() {
        // Arrange
        CreateOrderCommand command = createCommand("IDEMP-002");

        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(createOrderSaga.execute(any(Order.class)))
                .thenReturn(CreateOrderSaga.SagaResult.paymentFailed("Insufficient funds"));

        // Act
        CreateOrderResponse response = handler.execute(command);

        // Assert
        assertEquals("FAILED", response.status());
        assertEquals("支付失敗", response.message());
    }

    @Test
    @DisplayName("庫存不足應返回庫存失敗狀態")
    void execute_shouldReturnInventoryFailedWhenInventoryInsufficient() {
        // Arrange
        CreateOrderCommand command = createCommand("IDEMP-003");

        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(createOrderSaga.execute(any(Order.class)))
                .thenReturn(CreateOrderSaga.SagaResult.inventoryFailed("Insufficient stock"));

        // Act
        CreateOrderResponse response = handler.execute(command);

        // Assert
        assertEquals("ROLLBACK_COMPLETED", response.status());
        assertEquals("庫存扣減失敗", response.message());
    }

    @Test
    @DisplayName("請款失敗應返回請款失敗狀態")
    void execute_shouldReturnCaptureFailedWhenCaptureFails() {
        // Arrange
        CreateOrderCommand command = createCommand("IDEMP-004");

        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(createOrderSaga.execute(any(Order.class)))
                .thenReturn(CreateOrderSaga.SagaResult.captureFailed("Capture declined"));

        // Act
        CreateOrderResponse response = handler.execute(command);

        // Assert
        assertEquals("ROLLBACK_COMPLETED", response.status());
        assertEquals("支付確認失敗", response.message());
    }

    @Test
    @DisplayName("冪等性鍵為空應拋出例外")
    void execute_shouldThrowExceptionWhenIdempotencyKeyIsBlank() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> createCommand(""));

        assertThrows(IllegalArgumentException.class,
                () -> createCommand("   "));
    }

    @Test
    @DisplayName("應正確建立領域物件")
    void execute_shouldCreateDomainObjectsCorrectly() {
        // Arrange
        CreateOrderCommand command = createCommand("IDEMP-005");

        when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(createOrderSaga.execute(any(Order.class)))
                .thenReturn(CreateOrderSaga.SagaResult.success());

        // Act
        handler.execute(command);

        // Assert
        verify(orderRepository).save(argThat(order ->
                order.getBuyer().name().equals("王小明") &&
                order.getBuyer().email().equals("wang@example.com") &&
                order.getOrderItem().productId().equals("IPHONE-17") &&
                order.getMoney().amount().compareTo(new BigDecimal("39900")) == 0 &&
                order.getMoney().currency().equals("TWD")
        ));
    }
}
