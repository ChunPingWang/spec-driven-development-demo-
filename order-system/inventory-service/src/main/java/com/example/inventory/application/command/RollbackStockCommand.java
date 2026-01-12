package com.example.inventory.application.command;

/**
 * Command to rollback stock for a failed order.
 */
public record RollbackStockCommand(
        String orderId,
        String productId,
        int quantity
) {}
