package com.example.inventory.domain.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InsufficientStockException 測試")
class InsufficientStockExceptionTest {

    @Test
    @DisplayName("使用訊息建構例外")
    void constructor_withMessage_shouldSetMessage() {
        InsufficientStockException exception = new InsufficientStockException("庫存不足");

        assertEquals("庫存不足", exception.getMessage());
    }

    @Test
    @DisplayName("使用詳細參數建構例外")
    void constructor_withDetails_shouldFormatMessage() {
        InsufficientStockException exception =
                new InsufficientStockException("PROD-001", 10, 5);

        assertTrue(exception.getMessage().contains("PROD-001"));
        assertTrue(exception.getMessage().contains("10"));
        assertTrue(exception.getMessage().contains("5"));
    }

    @Test
    @DisplayName("例外應繼承 RuntimeException")
    void exception_shouldBeRuntimeException() {
        InsufficientStockException exception = new InsufficientStockException("test");

        assertInstanceOf(RuntimeException.class, exception);
    }
}
