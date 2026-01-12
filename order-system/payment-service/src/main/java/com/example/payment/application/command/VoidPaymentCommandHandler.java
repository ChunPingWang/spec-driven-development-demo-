package com.example.payment.application.command;

import com.example.payment.application.port.inbound.VoidPaymentUseCase;
import com.example.payment.application.port.outbound.AcquirerPort;
import com.example.payment.application.port.outbound.PaymentRepository;
import com.example.payment.domain.model.aggregate.Payment;
import com.example.payment.domain.model.valueobject.PaymentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command handler for voiding payments.
 */
@Service
@Transactional
public class VoidPaymentCommandHandler implements VoidPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final AcquirerPort acquirerPort;

    public VoidPaymentCommandHandler(
            PaymentRepository paymentRepository,
            AcquirerPort acquirerPort
    ) {
        this.paymentRepository = paymentRepository;
        this.acquirerPort = acquirerPort;
    }

    @Override
    public VoidResult execute(VoidPaymentCommand command) {
        PaymentId paymentId = PaymentId.of(command.paymentId());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + command.paymentId()));

        // Call acquirer for void
        AcquirerPort.VoidResponse response = acquirerPort.voidAuthorization(
                payment.getAuthorizationCode()
        );

        if (response.succeeded()) {
            payment.voidPayment();
            paymentRepository.save(payment);
            return new VoidResult(
                    payment.getPaymentId().value(),
                    true,
                    "Payment voided"
            );
        } else {
            return new VoidResult(
                    payment.getPaymentId().value(),
                    false,
                    response.failureReason()
            );
        }
    }
}
