package com.example.order.domain.model.valueobject;

import java.util.UUID;

/**
 * Value object representing a unique order identifier.
 * Format: "ORD-" + 8 uppercase alphanumeric characters
 */
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId cannot be null or blank");
        }
        if (!value.matches("^ORD-[A-Z0-9]{8}$")) {
            throw new IllegalArgumentException("OrderId must match format ORD-XXXXXXXX");
        }
    }

    /**
     * Factory method to generate a new OrderId.
     */
    public static OrderId generate() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return new OrderId("ORD-" + uuid);
    }

    /**
     * Factory method to create OrderId from existing value.
     */
    public static OrderId of(String value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
