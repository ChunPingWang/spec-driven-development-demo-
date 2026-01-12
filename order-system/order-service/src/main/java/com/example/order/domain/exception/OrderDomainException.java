package com.example.order.domain.exception;

/**
 * Exception thrown for order domain violations.
 */
public class OrderDomainException extends RuntimeException {

    public OrderDomainException(String message) {
        super(message);
    }

    public OrderDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public static OrderDomainException invalidStateTransition(String from, String to) {
        return new OrderDomainException(
                String.format("Invalid order state transition from %s to %s", from, to));
    }
}
