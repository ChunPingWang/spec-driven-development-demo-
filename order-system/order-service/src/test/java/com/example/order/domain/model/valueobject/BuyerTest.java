package com.example.order.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Buyer 值物件測試")
class BuyerTest {

    @Test
    @DisplayName("建立有效的 Buyer")
    void of_shouldCreateValidBuyer() {
        Buyer buyer = Buyer.of("王小明", "ming@example.com");

        assertEquals("王小明", buyer.name());
        assertEquals("ming@example.com", buyer.email());
    }

    @Test
    @DisplayName("空白名稱應拋出例外")
    void of_shouldThrowExceptionForBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> Buyer.of("", "ming@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> Buyer.of("   ", "ming@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> Buyer.of(null, "ming@example.com"));
    }

    @Test
    @DisplayName("無效的 Email 應拋出例外")
    void of_shouldThrowExceptionForInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> Buyer.of("王小明", "invalid-email"));
        assertThrows(IllegalArgumentException.class,
                () -> Buyer.of("王小明", ""));
        assertThrows(IllegalArgumentException.class,
                () -> Buyer.of("王小明", null));
    }

    @Test
    @DisplayName("Email 格式驗證")
    void of_shouldAcceptValidEmailFormats() {
        assertDoesNotThrow(() -> Buyer.of("Test", "test@example.com"));
        assertDoesNotThrow(() -> Buyer.of("Test", "test.user@example.co.uk"));
        assertDoesNotThrow(() -> Buyer.of("Test", "test+tag@example.com"));
    }
}
