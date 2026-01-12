package com.example.order.application.port.outbound;

/**
 * Port for inventory service operations.
 */
public interface InventoryServicePort {

    /**
     * Result of a stock deduction.
     */
    record DeductionResult(
            boolean success,
            int remainingStock,
            String message
    ) {
        public static DeductionResult success(int remainingStock) {
            return new DeductionResult(true, remainingStock, "Stock deducted");
        }

        public static DeductionResult failure(String message) {
            return new DeductionResult(false, 0, message);
        }
    }

    /**
     * Result of a stock rollback.
     */
    record RollbackResult(
            boolean success,
            int newStock,
            String message
    ) {
        public static RollbackResult success(int newStock) {
            return new RollbackResult(true, newStock, "Stock rolled back");
        }

        public static RollbackResult failure(String message) {
            return new RollbackResult(false, 0, message);
        }
    }

    /**
     * Deduct stock for an order.
     */
    DeductionResult deductStock(String orderId, String productId, int quantity);

    /**
     * Rollback stock for a failed order.
     */
    RollbackResult rollbackStock(String orderId, String productId, int quantity);
}
