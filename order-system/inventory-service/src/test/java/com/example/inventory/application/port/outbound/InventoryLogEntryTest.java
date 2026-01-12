package com.example.inventory.application.port.outbound;

import com.example.inventory.application.port.outbound.InventoryLogRepository.InventoryLogEntry;
import com.example.inventory.domain.model.valueobject.InventoryOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryLogEntry 測試")
class InventoryLogEntryTest {

    @Test
    @DisplayName("建立成功日誌應設定正確狀態")
    void success_shouldCreateEntryWithSuccessStatus() {
        InventoryLogEntry entry = InventoryLogEntry.success(
                "ORD-12345678", "PROD-001", InventoryOperation.DEDUCT, 5
        );

        assertNull(entry.id());
        assertEquals("ORD-12345678", entry.orderId());
        assertEquals("PROD-001", entry.productId());
        assertEquals(InventoryOperation.DEDUCT, entry.operationType());
        assertEquals(5, entry.quantity());
        assertEquals("SUCCESS", entry.status());
    }

    @Test
    @DisplayName("建立失敗日誌應設定正確狀態")
    void failed_shouldCreateEntryWithFailedStatus() {
        InventoryLogEntry entry = InventoryLogEntry.failed(
                "ORD-12345678", "PROD-001", InventoryOperation.DEDUCT, 5
        );

        assertNull(entry.id());
        assertEquals("ORD-12345678", entry.orderId());
        assertEquals("PROD-001", entry.productId());
        assertEquals(InventoryOperation.DEDUCT, entry.operationType());
        assertEquals(5, entry.quantity());
        assertEquals("FAILED", entry.status());
    }

    @Test
    @DisplayName("建立回滾成功日誌")
    void success_rollbackOperation_shouldCreateCorrectEntry() {
        InventoryLogEntry entry = InventoryLogEntry.success(
                "ORD-12345678", "PROD-001", InventoryOperation.ROLLBACK, 10
        );

        assertEquals(InventoryOperation.ROLLBACK, entry.operationType());
        assertEquals("SUCCESS", entry.status());
        assertEquals(10, entry.quantity());
    }

    @Test
    @DisplayName("建立回滾失敗日誌")
    void failed_rollbackOperation_shouldCreateCorrectEntry() {
        InventoryLogEntry entry = InventoryLogEntry.failed(
                "ORD-12345678", "PROD-001", InventoryOperation.ROLLBACK, 10
        );

        assertEquals(InventoryOperation.ROLLBACK, entry.operationType());
        assertEquals("FAILED", entry.status());
    }

    @Test
    @DisplayName("完整建構子應正確建立日誌")
    void constructor_shouldCreateCompleteEntry() {
        InventoryLogEntry entry = new InventoryLogEntry(
                1L, "ORD-12345678", "PROD-001", InventoryOperation.DEDUCT, 5, "SUCCESS"
        );

        assertEquals(1L, entry.id());
        assertEquals("ORD-12345678", entry.orderId());
        assertEquals("PROD-001", entry.productId());
        assertEquals(InventoryOperation.DEDUCT, entry.operationType());
        assertEquals(5, entry.quantity());
        assertEquals("SUCCESS", entry.status());
    }
}
