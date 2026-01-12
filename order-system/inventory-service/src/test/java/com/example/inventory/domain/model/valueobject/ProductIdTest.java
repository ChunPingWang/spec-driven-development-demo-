package com.example.inventory.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductId 值物件測試")
class ProductIdTest {

    @Test
    @DisplayName("建立有效的 ProductId")
    void of_shouldCreateValidProductId() {
        ProductId productId = ProductId.of("IPHONE-17-PRO-MAX");

        assertEquals("IPHONE-17-PRO-MAX", productId.value());
    }

    @Test
    @DisplayName("空值應拋出例外")
    void of_shouldThrowExceptionForNullValue() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductId.of(null));
    }

    @Test
    @DisplayName("空白字串應拋出例外")
    void of_shouldThrowExceptionForBlankValue() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductId.of(""));
        assertThrows(IllegalArgumentException.class,
                () -> ProductId.of("   "));
    }
}
