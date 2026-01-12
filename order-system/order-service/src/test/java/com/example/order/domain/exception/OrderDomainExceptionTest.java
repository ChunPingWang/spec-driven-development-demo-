package com.example.order.domain.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderDomainException 測試")
class OrderDomainExceptionTest {

    @Test
    @DisplayName("使用訊息建構例外")
    void constructor_withMessage_shouldSetMessage() {
        OrderDomainException exception = new OrderDomainException("訂單錯誤");

        assertEquals("訂單錯誤", exception.getMessage());
    }

    @Test
    @DisplayName("使用訊息和原因建構例外")
    void constructor_withMessageAndCause_shouldSetBoth() {
        RuntimeException cause = new RuntimeException("原始錯誤");
        OrderDomainException exception = new OrderDomainException("訂單錯誤", cause);

        assertEquals("訂單錯誤", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("invalidStateTransition 應產生正確訊息")
    void invalidStateTransition_shouldFormatMessage() {
        OrderDomainException exception = OrderDomainException.invalidStateTransition("CREATED", "COMPLETED");

        assertTrue(exception.getMessage().contains("CREATED"));
        assertTrue(exception.getMessage().contains("COMPLETED"));
    }

    @Test
    @DisplayName("例外應繼承 RuntimeException")
    void exception_shouldBeRuntimeException() {
        OrderDomainException exception = new OrderDomainException("test");

        assertInstanceOf(RuntimeException.class, exception);
    }
}
