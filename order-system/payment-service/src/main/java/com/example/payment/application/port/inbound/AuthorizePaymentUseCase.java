package com.example.payment.application.port.inbound;

import com.example.payment.application.command.AuthorizePaymentCommand;

/**
 * Use case for authorizing payments.
 */
public interface AuthorizePaymentUseCase {

    /**
     * Authorize a payment for an order.
     * @param command The authorization command
     * @return The authorization result
     */
    AuthorizeResult execute(AuthorizePaymentCommand command);

    record AuthorizeResult(
            String paymentId,
            boolean authorized,
            String message
    ) {}
}
