package com.example.order.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderItem 值物件測試")
class OrderItemTest {

    @Test
    @DisplayName("建立有效的 OrderItem")
    void of_shouldCreateValidOrderItem() {
        OrderItem item = OrderItem.of("PROD-001", "iPhone 17 Pro Max", 2);

        assertEquals("PROD-001", item.productId());
        assertEquals("iPhone 17 Pro Max", item.productName());
        assertEquals(2, item.quantity());
    }

    @Test
    @DisplayName("空白 productId 應拋出例外")
    void of_shouldThrowExceptionForBlankProductId() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderItem.of("", "Product", 1));
        assertThrows(IllegalArgumentException.class,
                () -> OrderItem.of(null, "Product", 1));
    }

    @Test
    @DisplayName("空白 productName 應拋出例外")
    void of_shouldThrowExceptionForBlankProductName() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderItem.of("PROD-001", "", 1));
        assertThrows(IllegalArgumentException.class,
                () -> OrderItem.of("PROD-001", null, 1));
    }

    @Test
    @DisplayName("零或負數數量應拋出例外")
    void of_shouldThrowExceptionForInvalidQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderItem.of("PROD-001", "Product", 0));
        assertThrows(IllegalArgumentException.class,
                () -> OrderItem.of("PROD-001", "Product", -1));
    }

    @Test
    @DisplayName("數量為 1 應有效")
    void of_shouldAcceptQuantityOfOne() {
        OrderItem item = OrderItem.of("PROD-001", "Product", 1);
        assertEquals(1, item.quantity());
    }
}
