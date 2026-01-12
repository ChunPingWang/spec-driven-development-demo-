package com.example.payment.application.command;

import com.example.payment.application.port.inbound.VoidPaymentUseCase;
import com.example.payment.application.port.outbound.AcquirerPort;
import com.example.payment.application.port.outbound.PaymentRepository;
import com.example.payment.domain.model.aggregate.Payment;
import com.example.payment.domain.model.valueobject.Money;
import com.example.payment.domain.model.valueobject.PaymentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoidPaymentCommandHandler 測試")
class VoidPaymentCommandHandlerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AcquirerPort acquirerPort;

    private VoidPaymentCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new VoidPaymentCommandHandler(paymentRepository, acquirerPort);
    }

    private Payment createAuthorizedPayment(String paymentId) {
        Payment payment = Payment.create(
                "ORD-001",
                Money.of(new BigDecimal("999.99"), "TWD"),
                "4111111111111111",
                "12/26"
        );
        payment.authorize("AUTH-123");
        // Use reflection to set paymentId for testing
        try {
            var field = Payment.class.getDeclaredField("paymentId");
            field.setAccessible(true);
            field.set(payment, PaymentId.of(paymentId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return payment;
    }

    @Test
    @DisplayName("成功作廢授權")
    void execute_shouldVoidPaymentSuccessfully() {
        // Arrange
        String paymentId = "PAY-001";
        Payment payment = createAuthorizedPayment(paymentId);

        when(paymentRepository.findById(any(PaymentId.class)))
                .thenReturn(Optional.of(payment));
        when(acquirerPort.voidAuthorization(any()))
                .thenReturn(AcquirerPort.VoidResponse.success());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        VoidPaymentCommand command = new VoidPaymentCommand(paymentId);

        // Act
        VoidPaymentUseCase.VoidResult result = handler.execute(command);

        // Assert
        assertTrue(result.voided());
        assertEquals("Payment voided", result.message());
        assertEquals(paymentId, result.paymentId());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals("VOIDED", paymentCaptor.getValue().getStatus().name());
    }

    @Test
    @DisplayName("作廢失敗應返回失敗結果")
    void execute_shouldReturnFailureWhenVoidFails() {
        // Arrange
        String paymentId = "PAY-001";
        Payment payment = createAuthorizedPayment(paymentId);

        when(paymentRepository.findById(any(PaymentId.class)))
                .thenReturn(Optional.of(payment));
        when(acquirerPort.voidAuthorization(any()))
                .thenReturn(AcquirerPort.VoidResponse.failure("Void failed"));

        VoidPaymentCommand command = new VoidPaymentCommand(paymentId);

        // Act
        VoidPaymentUseCase.VoidResult result = handler.execute(command);

        // Assert
        assertFalse(result.voided());
        assertEquals("Void failed", result.message());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("付款不存在應拋出例外")
    void execute_shouldThrowExceptionWhenPaymentNotFound() {
        // Arrange
        when(paymentRepository.findById(any(PaymentId.class)))
                .thenReturn(Optional.empty());

        VoidPaymentCommand command = new VoidPaymentCommand("NOT-EXIST");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("應正確傳遞參數給收單行")
    void execute_shouldPassCorrectParametersToAcquirer() {
        // Arrange
        String paymentId = "PAY-001";
        Payment payment = createAuthorizedPayment(paymentId);

        when(paymentRepository.findById(any(PaymentId.class)))
                .thenReturn(Optional.of(payment));
        when(acquirerPort.voidAuthorization(any()))
                .thenReturn(AcquirerPort.VoidResponse.success());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        VoidPaymentCommand command = new VoidPaymentCommand(paymentId);

        // Act
        handler.execute(command);

        // Assert
        verify(acquirerPort).voidAuthorization(eq("AUTH-123"));
    }
}
