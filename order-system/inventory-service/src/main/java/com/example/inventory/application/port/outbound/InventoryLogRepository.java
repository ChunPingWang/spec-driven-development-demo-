package com.example.inventory.application.port.outbound;

import com.example.inventory.domain.model.valueobject.InventoryOperation;

import java.util.Optional;

/**
 * Port for inventory log persistence operations.
 */
public interface InventoryLogRepository {

    /**
     * Inventory log entry for tracking operations.
     */
    record InventoryLogEntry(
            Long id,
            String orderId,
            String productId,
            InventoryOperation operationType,
            int quantity,
            String status
    ) {
        public static InventoryLogEntry success(String orderId, String productId,
                                                 InventoryOperation operation, int quantity) {
            return new InventoryLogEntry(null, orderId, productId, operation, quantity, "SUCCESS");
        }

        public static InventoryLogEntry failed(String orderId, String productId,
                                                InventoryOperation operation, int quantity) {
            return new InventoryLogEntry(null, orderId, productId, operation, quantity, "FAILED");
        }
    }

    /**
     * Save an inventory log entry.
     */
    InventoryLogEntry save(InventoryLogEntry entry);

    /**
     * Find a log entry by order, product, and operation type.
     * Used for idempotency checking.
     */
    Optional<InventoryLogEntry> findByOrderAndProductAndOperation(
            String orderId,
            String productId,
            InventoryOperation operationType
    );
}
