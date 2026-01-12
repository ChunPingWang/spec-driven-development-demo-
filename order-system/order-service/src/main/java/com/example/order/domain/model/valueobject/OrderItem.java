package com.example.order.domain.model.valueobject;

/**
 * Value object representing an item in an order.
 */
public record OrderItem(String productId, String productName, int quantity) {

    public OrderItem {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    /**
     * Factory method to create an OrderItem.
     */
    public static OrderItem of(String productId, String productName, int quantity) {
        return new OrderItem(productId, productName, quantity);
    }
}
