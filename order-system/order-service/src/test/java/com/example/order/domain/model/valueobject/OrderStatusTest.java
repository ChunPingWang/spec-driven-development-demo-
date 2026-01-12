package com.example.order.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderStatus 列舉測試")
class OrderStatusTest {

    @Test
    @DisplayName("應有 CREATED 狀態")
    void shouldHaveCreatedStatus() {
        assertEquals("CREATED", OrderStatus.CREATED.name());
    }

    @Test
    @DisplayName("應有 PAYMENT_AUTHORIZED 狀態")
    void shouldHavePaymentAuthorizedStatus() {
        assertEquals("PAYMENT_AUTHORIZED", OrderStatus.PAYMENT_AUTHORIZED.name());
    }

    @Test
    @DisplayName("應有 INVENTORY_DEDUCTED 狀態")
    void shouldHaveInventoryDeductedStatus() {
        assertEquals("INVENTORY_DEDUCTED", OrderStatus.INVENTORY_DEDUCTED.name());
    }

    @Test
    @DisplayName("應有 COMPLETED 狀態")
    void shouldHaveCompletedStatus() {
        assertEquals("COMPLETED", OrderStatus.COMPLETED.name());
    }

    @Test
    @DisplayName("應有 FAILED 狀態")
    void shouldHaveFailedStatus() {
        assertEquals("FAILED", OrderStatus.FAILED.name());
    }

    @Test
    @DisplayName("應有 ROLLBACK_COMPLETED 狀態")
    void shouldHaveRollbackCompletedStatus() {
        assertEquals("ROLLBACK_COMPLETED", OrderStatus.ROLLBACK_COMPLETED.name());
    }

    @Test
    @DisplayName("應有六種狀態")
    void shouldHaveSixStatuses() {
        assertEquals(6, OrderStatus.values().length);
    }

    @Test
    @DisplayName("valueOf 應正確轉換字串")
    void valueOf_shouldConvertStringCorrectly() {
        assertEquals(OrderStatus.CREATED, OrderStatus.valueOf("CREATED"));
        assertEquals(OrderStatus.PAYMENT_AUTHORIZED, OrderStatus.valueOf("PAYMENT_AUTHORIZED"));
        assertEquals(OrderStatus.INVENTORY_DEDUCTED, OrderStatus.valueOf("INVENTORY_DEDUCTED"));
        assertEquals(OrderStatus.COMPLETED, OrderStatus.valueOf("COMPLETED"));
        assertEquals(OrderStatus.FAILED, OrderStatus.valueOf("FAILED"));
        assertEquals(OrderStatus.ROLLBACK_COMPLETED, OrderStatus.valueOf("ROLLBACK_COMPLETED"));
    }
}
