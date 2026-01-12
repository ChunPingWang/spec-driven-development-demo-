package com.example.inventory.domain.model.valueobject;

/**
 * Value object representing a unique product identifier.
 */
public record ProductId(String value) {

    public ProductId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ProductId cannot be null or blank");
        }
    }

    /**
     * Factory method to create ProductId.
     */
    public static ProductId of(String value) {
        return new ProductId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
