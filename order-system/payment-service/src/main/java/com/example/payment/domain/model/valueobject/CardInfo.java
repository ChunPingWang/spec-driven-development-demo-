package com.example.payment.domain.model.valueobject;

/**
 * Value object representing masked card information for storage.
 */
public record CardInfo(String lastFour, String expiryDate) {

    public CardInfo {
        if (lastFour == null || !lastFour.matches("^[0-9]{4}$")) {
            throw new IllegalArgumentException("Last four must be exactly 4 digits");
        }
        if (expiryDate == null || !expiryDate.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            throw new IllegalArgumentException("Expiry date must be in MM/YY format");
        }
    }

    /**
     * Factory method to create CardInfo from full card number.
     */
    public static CardInfo fromCardNumber(String cardNumber, String expiryDate) {
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return new CardInfo(lastFour, expiryDate);
    }

    /**
     * Factory method to create CardInfo.
     */
    public static CardInfo of(String lastFour, String expiryDate) {
        return new CardInfo(lastFour, expiryDate);
    }

    /**
     * Returns masked card representation.
     */
    public String getMaskedCard() {
        return "**** **** **** " + lastFour;
    }
}
