package com.example.order.domain.model.valueobject;

/**
 * Value object representing payment information for order processing.
 * Card details are used for authorization only and not persisted.
 */
public record PaymentInfo(String method, String cardNumber, String expiryDate, String cvv) {

    public PaymentInfo {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("Payment method cannot be null or blank");
        }
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new IllegalArgumentException("Card number cannot be null or blank");
        }
        if (expiryDate == null || !expiryDate.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            throw new IllegalArgumentException("Expiry date must be in MM/YY format");
        }
        if (cvv == null || !cvv.matches("^[0-9]{3,4}$")) {
            throw new IllegalArgumentException("CVV must be 3 or 4 digits");
        }
    }

    /**
     * Factory method to create PaymentInfo.
     */
    public static PaymentInfo of(String method, String cardNumber, String expiryDate, String cvv) {
        return new PaymentInfo(method, cardNumber, expiryDate, cvv);
    }

    /**
     * Returns the last four digits of the card number for masked storage.
     */
    public String getLastFour() {
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
