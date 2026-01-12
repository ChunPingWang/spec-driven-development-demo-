package com.example.order.domain.model.valueobject;

/**
 * Enum representing the possible states of an order.
 */
public enum OrderStatus {
    /**
     * Initial state - order has been created but not processed.
     */
    CREATED,

    /**
     * Payment pre-authorization successful.
     */
    PAYMENT_AUTHORIZED,

    /**
     * Inventory has been deducted/reserved.
     */
    INVENTORY_DEDUCTED,

    /**
     * Order completed successfully - payment captured.
     */
    COMPLETED,

    /**
     * Order failed early (e.g., payment auth failed) - no compensation needed.
     */
    FAILED,

    /**
     * Compensation executed after partial failure.
     */
    ROLLBACK_COMPLETED
}
