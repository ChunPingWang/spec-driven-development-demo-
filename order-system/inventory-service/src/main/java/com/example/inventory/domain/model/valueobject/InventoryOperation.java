package com.example.inventory.domain.model.valueobject;

/**
 * Enum representing inventory operation types.
 */
public enum InventoryOperation {
    /**
     * Reduce stock for an order.
     */
    DEDUCT,

    /**
     * Restore stock after order failure.
     */
    ROLLBACK
}
