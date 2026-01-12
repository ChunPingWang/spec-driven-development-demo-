package com.example.order.domain.model.valueobject;

import java.math.BigDecimal;

/**
 * Value object representing monetary amount with currency.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        if (currency == null || !currency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Currency must be a 3-letter ISO code");
        }
    }

    /**
     * Factory method to create Money.
     */
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    /**
     * Factory method to create Money from long value (for convenience).
     */
    public static Money of(long amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Checks if this money has the same currency as another.
     */
    public boolean hasSameCurrency(Money other) {
        return this.currency.equals(other.currency);
    }
}
