package com.example.payment.application.command;

/**
 * Command to capture (finalize) an authorized payment.
 */
public record CapturePaymentCommand(
        String paymentId
) {}
