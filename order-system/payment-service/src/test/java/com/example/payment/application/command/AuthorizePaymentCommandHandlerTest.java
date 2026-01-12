package com.example.payment.application.command;

import com.example.payment.application.port.inbound.AuthorizePaymentUseCase;
import com.example.payment.application.port.outbound.AcquirerPort;
import com.example.payment.application.port.outbound.PaymentRepository;
import com.example.payment.domain.model.aggregate.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizePaymentCommandHandler 測試")
class AuthorizePaymentCommandHandlerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AcquirerPort acquirerPort;

    private AuthorizePaymentCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AuthorizePaymentCommandHandler(paymentRepository, acquirerPort);
    }

    @Test
    @DisplayName("成功授權付款")
    void execute_shouldAuthorizePaymentSuccessfully() {
        // Arrange
        AuthorizePaymentCommand command = new AuthorizePaymentCommand(
                "ORD-001",
                new BigDecimal("999.99"),
                "TWD",
                "4111111111111111",
                "12/26",
                "123"
        );

        when(acquirerPort.authorize(any(), any(), any(), any(), any()))
                .thenReturn(AcquirerPort.AuthorizationResponse.approved("AUTH-123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        AuthorizePaymentUseCase.AuthorizeResult result = handler.execute(command);

        // Assert
        assertTrue(result.authorized());
        assertEquals("Payment authorized", result.message());
        assertNotNull(result.paymentId());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals("AUTHORIZED", paymentCaptor.getValue().getStatus().name());
    }

    @Test
    @DisplayName("授權被拒絕應返回失敗")
    void execute_shouldReturnFailureWhenDeclined() {
        // Arrange
        AuthorizePaymentCommand command = new AuthorizePaymentCommand(
                "ORD-001",
                new BigDecimal("999.99"),
                "TWD",
                "4111111111111111",
                "12/26",
                "123"
        );

        when(acquirerPort.authorize(any(), any(), any(), any(), any()))
                .thenReturn(AcquirerPort.AuthorizationResponse.declined("Insufficient funds"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        AuthorizePaymentUseCase.AuthorizeResult result = handler.execute(command);

        // Assert
        assertFalse(result.authorized());
        assertEquals("Insufficient funds", result.message());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals("FAILED", paymentCaptor.getValue().getStatus().name());
    }

    @Test
    @DisplayName("應正確傳遞參數給收單行")
    void execute_shouldPassCorrectParametersToAcquirer() {
        // Arrange
        AuthorizePaymentCommand command = new AuthorizePaymentCommand(
                "ORD-001",
                new BigDecimal("1500.00"),
                "USD",
                "5500000000000004",
                "01/27",
                "456"
        );

        when(acquirerPort.authorize(any(), any(), any(), any(), any()))
                .thenReturn(AcquirerPort.AuthorizationResponse.approved("AUTH-456"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        handler.execute(command);

        // Assert
        verify(acquirerPort).authorize(
                eq(new BigDecimal("1500.00")),
                eq("USD"),
                eq("5500000000000004"),
                eq("01/27"),
                eq("456")
        );
    }
}
