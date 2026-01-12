package com.example.inventory.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryOperation 列舉測試")
class InventoryOperationTest {

    @Test
    @DisplayName("應有 DEDUCT 操作類型")
    void shouldHaveDeductOperation() {
        assertEquals("DEDUCT", InventoryOperation.DEDUCT.name());
    }

    @Test
    @DisplayName("應有 ROLLBACK 操作類型")
    void shouldHaveRollbackOperation() {
        assertEquals("ROLLBACK", InventoryOperation.ROLLBACK.name());
    }

    @Test
    @DisplayName("應有兩種操作類型")
    void shouldHaveTwoOperations() {
        assertEquals(2, InventoryOperation.values().length);
    }

    @Test
    @DisplayName("valueOf 應正確轉換字串")
    void valueOf_shouldConvertStringCorrectly() {
        assertEquals(InventoryOperation.DEDUCT, InventoryOperation.valueOf("DEDUCT"));
        assertEquals(InventoryOperation.ROLLBACK, InventoryOperation.valueOf("ROLLBACK"));
    }
}
