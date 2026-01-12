package com.example.payment.application.port.inbound;

import com.example.payment.application.command.VoidPaymentCommand;

/**
 * Use case for voiding authorized payments.
 */
public interface VoidPaymentUseCase {

    /**
     * Void an authorized payment (compensation).
     * @param command The void command
     * @return The void result
     */
    VoidResult execute(VoidPaymentCommand command);

    record VoidResult(
            String paymentId,
            boolean voided,
            String message
    ) {}
}
