package com.example.payment.application.command;

import java.math.BigDecimal;

/**
 * Command to authorize a payment.
 */
public record AuthorizePaymentCommand(
        String orderId,
        BigDecimal amount,
        String currency,
        String cardNumber,
        String expiryDate,
        String cvv
) {}
