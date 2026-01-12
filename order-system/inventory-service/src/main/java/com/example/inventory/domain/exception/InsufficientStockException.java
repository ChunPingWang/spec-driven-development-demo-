package com.example.inventory.domain.exception;

/**
 * Exception thrown when there is insufficient stock for an operation.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String productId, int requested, int available) {
        super(String.format(
                "Insufficient stock for product %s: requested=%d, available=%d",
                productId, requested, available));
    }
}
