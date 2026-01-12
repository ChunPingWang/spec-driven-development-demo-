package com.example.order.domain.model.valueobject;

/**
 * Value object representing buyer information.
 */
public record Buyer(String name, String email) {

    public Buyer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Buyer name cannot be null or blank");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Buyer email must be valid and contain @");
        }
    }

    /**
     * Factory method to create a Buyer.
     */
    public static Buyer of(String name, String email) {
        return new Buyer(name, email);
    }
}
