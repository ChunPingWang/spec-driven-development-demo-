package com.example.payment.application.command;

/**
 * Command to void an authorized payment.
 */
public record VoidPaymentCommand(
        String paymentId
) {}
