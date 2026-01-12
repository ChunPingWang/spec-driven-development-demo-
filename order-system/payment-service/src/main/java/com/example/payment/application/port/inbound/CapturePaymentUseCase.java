package com.example.payment.application.port.inbound;

import com.example.payment.application.command.CapturePaymentCommand;

/**
 * Use case for capturing authorized payments.
 */
public interface CapturePaymentUseCase {

    /**
     * Capture an authorized payment.
     * @param command The capture command
     * @return The capture result
     */
    CaptureResult execute(CapturePaymentCommand command);

    record CaptureResult(
            String paymentId,
            boolean captured,
            String message
    ) {}
}
