package com.example.order.application.dto;

import java.math.BigDecimal;

/**
 * Request DTO for payment service calls.
 */
public record PaymentRequest(
        String orderId,
        BigDecimal amount,
        String currency,
        String cardNumber,
        String expiryDate,
        String cvv
) {}
