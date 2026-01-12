package com.example.inventory.application.command;

/**
 * Command to deduct stock for an order.
 */
public record DeductStockCommand(
        String orderId,
        String productId,
        int quantity
) {}
