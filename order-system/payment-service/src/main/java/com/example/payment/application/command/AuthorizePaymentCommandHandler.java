package com.example.payment.application.command;

import com.example.payment.application.port.inbound.AuthorizePaymentUseCase;
import com.example.payment.application.port.outbound.AcquirerPort;
import com.example.payment.application.port.outbound.PaymentRepository;
import com.example.payment.domain.model.aggregate.Payment;
import com.example.payment.domain.model.valueobject.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command handler for payment authorization.
 */
@Service
@Transactional
public class AuthorizePaymentCommandHandler implements AuthorizePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final AcquirerPort acquirerPort;

    public AuthorizePaymentCommandHandler(
            PaymentRepository paymentRepository,
            AcquirerPort acquirerPort
    ) {
        this.paymentRepository = paymentRepository;
        this.acquirerPort = acquirerPort;
    }

    @Override
    public AuthorizeResult execute(AuthorizePaymentCommand command) {
        // Create payment aggregate
        Money money = Money.of(command.amount(), command.currency());
        Payment payment = Payment.create(
                command.orderId(),
                money,
                command.cardNumber(),
                command.expiryDate()
        );

        // Call acquirer for authorization
        AcquirerPort.AuthorizationResponse response = acquirerPort.authorize(
                command.amount(),
                command.currency(),
                command.cardNumber(),
                command.expiryDate(),
                command.cvv()
        );

        if (response.approved()) {
            payment.authorize(response.authorizationCode());
            paymentRepository.save(payment);
            return new AuthorizeResult(
                    payment.getPaymentId().value(),
                    true,
                    "Payment authorized"
            );
        } else {
            payment.failAuthorization(response.declineReason());
            paymentRepository.save(payment);
            return new AuthorizeResult(
                    payment.getPaymentId().value(),
                    false,
                    response.declineReason()
            );
        }
    }
}
