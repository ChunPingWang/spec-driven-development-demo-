package com.example.inventory.domain.model.aggregate;

import com.example.inventory.domain.event.StockDeducted;
import com.example.inventory.domain.event.StockRolledBack;
import com.example.inventory.domain.exception.InsufficientStockException;
import com.example.inventory.domain.model.valueobject.ProductId;
import com.example.inventory.domain.model.valueobject.StockQuantity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product 聚合根測試")
class ProductTest {

    @Test
    @DisplayName("建立 Product 應設定正確的初始值")
    void create_shouldSetInitialValues() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 10);

        assertEquals("IPHONE-17", product.getProductId().value());
        assertEquals("iPhone 17 Pro Max", product.getProductName());
        assertEquals(10, product.getCurrentStock());
        assertNotNull(product.getCreatedAt());
    }

    @Test
    @DisplayName("扣減庫存應減少庫存數量")
    void deductStock_shouldDecreaseStockQuantity() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 10);

        product.deductStock("ORD-001", 3);

        assertEquals(7, product.getCurrentStock());
    }

    @Test
    @DisplayName("扣減庫存應產生 StockDeducted 事件")
    void deductStock_shouldRaiseStockDeductedEvent() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 10);

        product.deductStock("ORD-001", 3);

        assertTrue(product.getDomainEvents().stream()
                .anyMatch(e -> e instanceof StockDeducted));
    }

    @Test
    @DisplayName("庫存不足應拋出 InsufficientStockException")
    void deductStock_shouldThrowExceptionWhenInsufficientStock() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 10);

        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> product.deductStock("ORD-001", 15));

        assertTrue(exception.getMessage().contains("IPHONE-17"));
    }

    @Test
    @DisplayName("回滾庫存應增加庫存數量")
    void rollbackStock_shouldIncreaseStockQuantity() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 7);

        product.rollbackStock("ORD-001", 3);

        assertEquals(10, product.getCurrentStock());
    }

    @Test
    @DisplayName("回滾庫存應產生 StockRolledBack 事件")
    void rollbackStock_shouldRaiseStockRolledBackEvent() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 7);

        product.rollbackStock("ORD-001", 3);

        assertTrue(product.getDomainEvents().stream()
                .anyMatch(e -> e instanceof StockRolledBack));
    }

    @Test
    @DisplayName("檢查庫存充足應返回 true")
    void hasSufficientStock_shouldReturnTrueWhenStockIsSufficient() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 10);

        assertTrue(product.hasSufficientStock(10));
        assertTrue(product.hasSufficientStock(5));
    }

    @Test
    @DisplayName("檢查庫存不足應返回 false")
    void hasSufficientStock_shouldReturnFalseWhenStockIsInsufficient() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 10);

        assertFalse(product.hasSufficientStock(11));
    }

    @Test
    @DisplayName("扣減全部庫存應成功")
    void deductStock_shouldSucceedWhenDeductingAllStock() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 10);

        product.deductStock("ORD-001", 10);

        assertEquals(0, product.getCurrentStock());
    }

    @Test
    @DisplayName("從 reconstitute 重建 Product")
    void reconstitute_shouldRecreateProduct() {
        ProductId productId = ProductId.of("IPHONE-17");
        StockQuantity stockQuantity = StockQuantity.of(10);
        LocalDateTime now = LocalDateTime.now();

        Product product = Product.reconstitute(productId, "iPhone 17 Pro Max", stockQuantity, now, now);

        assertEquals("IPHONE-17", product.getProductId().value());
        assertEquals(10, product.getCurrentStock());
    }

    @Test
    @DisplayName("清除領域事件")
    void clearDomainEvents_shouldRemoveAllEvents() {
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 10);
        product.deductStock("ORD-001", 1);

        assertFalse(product.getDomainEvents().isEmpty());
        product.clearDomainEvents();
        assertTrue(product.getDomainEvents().isEmpty());
    }
}
