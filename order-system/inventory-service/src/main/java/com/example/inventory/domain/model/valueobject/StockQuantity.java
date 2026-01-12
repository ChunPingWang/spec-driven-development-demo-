package com.example.inventory.domain.model.valueobject;

import com.example.inventory.domain.exception.InsufficientStockException;

/**
 * Value object representing stock quantity.
 */
public record StockQuantity(int value) {

    public StockQuantity {
        if (value < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
    }

    /**
     * Factory method to create StockQuantity.
     */
    public static StockQuantity of(int value) {
        return new StockQuantity(value);
    }

    /**
     * Deduct quantity from stock.
     * @throws InsufficientStockException if deduction would result in negative stock
     */
    public StockQuantity deduct(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Deduction quantity must be positive");
        }
        if (this.value < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock: available=%d, requested=%d", this.value, quantity));
        }
        return new StockQuantity(this.value - quantity);
    }

    /**
     * Add quantity to stock.
     */
    public StockQuantity add(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Addition quantity must be positive");
        }
        return new StockQuantity(this.value + quantity);
    }

    /**
     * Check if there is sufficient stock.
     */
    public boolean hasSufficientStock(int quantity) {
        return this.value >= quantity;
    }
}
