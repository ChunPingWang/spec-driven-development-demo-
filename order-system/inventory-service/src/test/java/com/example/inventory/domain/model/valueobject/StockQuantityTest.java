package com.example.inventory.domain.model.valueobject;

import com.example.inventory.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockQuantity 值物件測試")
class StockQuantityTest {

    @Test
    @DisplayName("建立有效的 StockQuantity")
    void of_shouldCreateValidStockQuantity() {
        StockQuantity quantity = StockQuantity.of(10);

        assertEquals(10, quantity.value());
    }

    @Test
    @DisplayName("零庫存應有效")
    void of_shouldAcceptZeroQuantity() {
        StockQuantity quantity = StockQuantity.of(0);

        assertEquals(0, quantity.value());
    }

    @Test
    @DisplayName("負數庫存應拋出例外")
    void of_shouldThrowExceptionForNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> StockQuantity.of(-1));
    }

    @Test
    @DisplayName("扣減庫存應返回新的 StockQuantity")
    void deduct_shouldReturnNewStockQuantity() {
        StockQuantity quantity = StockQuantity.of(10);

        StockQuantity result = quantity.deduct(3);

        assertEquals(7, result.value());
        assertEquals(10, quantity.value()); // 原物件不變
    }

    @Test
    @DisplayName("扣減後負數應拋出例外")
    void deduct_shouldThrowExceptionWhenResultIsNegative() {
        StockQuantity quantity = StockQuantity.of(5);

        assertThrows(InsufficientStockException.class,
                () -> quantity.deduct(6));
    }

    @Test
    @DisplayName("增加庫存應返回新的 StockQuantity")
    void add_shouldReturnNewStockQuantity() {
        StockQuantity quantity = StockQuantity.of(10);

        StockQuantity result = quantity.add(5);

        assertEquals(15, result.value());
        assertEquals(10, quantity.value()); // 原物件不變
    }

    @Test
    @DisplayName("檢查庫存充足")
    void hasSufficientStock_shouldReturnCorrectResult() {
        StockQuantity quantity = StockQuantity.of(10);

        assertTrue(quantity.hasSufficientStock(10));
        assertTrue(quantity.hasSufficientStock(5));
        assertFalse(quantity.hasSufficientStock(11));
    }
}
