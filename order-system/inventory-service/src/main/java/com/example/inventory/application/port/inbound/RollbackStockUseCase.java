package com.example.inventory.application.port.inbound;

import com.example.inventory.application.command.RollbackStockCommand;

/**
 * Use case for rolling back stock.
 */
public interface RollbackStockUseCase {

    /**
     * Rollback stock for a failed order.
     * @param command The rollback stock command
     * @return The rollback result
     */
    RollbackResult execute(RollbackStockCommand command);

    record RollbackResult(
            String productId,
            boolean success,
            String message,
            int currentStock
    ) {}
}
