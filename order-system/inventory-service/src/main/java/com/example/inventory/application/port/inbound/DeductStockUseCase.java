package com.example.inventory.application.port.inbound;

import com.example.inventory.application.command.DeductStockCommand;

/**
 * Use case for deducting stock.
 */
public interface DeductStockUseCase {

    /**
     * Deduct stock for an order.
     * @param command The deduct stock command
     * @return The deduction result
     */
    DeductResult execute(DeductStockCommand command);

    record DeductResult(
            String productId,
            boolean success,
            String message,
            int remainingStock
    ) {}
}
