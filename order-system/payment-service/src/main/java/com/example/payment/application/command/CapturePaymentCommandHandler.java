package com.example.payment.application.command;

import com.example.payment.application.port.inbound.CapturePaymentUseCase;
import com.example.payment.application.port.outbound.AcquirerPort;
import com.example.payment.application.port.outbound.PaymentRepository;
import com.example.payment.domain.model.aggregate.Payment;
import com.example.payment.domain.model.valueobject.PaymentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command handler for payment capture.
 */
@Service
@Transactional
public class CapturePaymentCommandHandler implements CapturePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final AcquirerPort acquirerPort;

    public CapturePaymentCommandHandler(
            PaymentRepository paymentRepository,
            AcquirerPort acquirerPort
    ) {
        this.paymentRepository = paymentRepository;
        this.acquirerPort = acquirerPort;
    }

    @Override
    public CaptureResult execute(CapturePaymentCommand command) {
        PaymentId paymentId = PaymentId.of(command.paymentId());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + command.paymentId()));

        // Call acquirer for capture
        AcquirerPort.CaptureResponse response = acquirerPort.capture(
                payment.getAuthorizationCode(),
                payment.getMoney().amount()
        );

        if (response.succeeded()) {
            payment.capture();
            paymentRepository.save(payment);
            return new CaptureResult(
                    payment.getPaymentId().value(),
                    true,
                    "Payment captured"
            );
        } else {
            payment.failCapture(response.failureReason());
            paymentRepository.save(payment);
            return new CaptureResult(
                    payment.getPaymentId().value(),
                    false,
                    response.failureReason()
            );
        }
    }
}
