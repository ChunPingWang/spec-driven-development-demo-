package com.example.payment.domain.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentDomainException 測試")
class PaymentDomainExceptionTest {

    @Test
    @DisplayName("使用訊息建構例外")
    void constructor_withMessage_shouldSetMessage() {
        PaymentDomainException exception = new PaymentDomainException("支付錯誤");

        assertEquals("支付錯誤", exception.getMessage());
    }

    @Test
    @DisplayName("使用訊息和原因建構例外")
    void constructor_withMessageAndCause_shouldSetBoth() {
        RuntimeException cause = new RuntimeException("原始錯誤");
        PaymentDomainException exception = new PaymentDomainException("支付錯誤", cause);

        assertEquals("支付錯誤", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("invalidStateTransition 應產生正確訊息")
    void invalidStateTransition_shouldFormatMessage() {
        PaymentDomainException exception = PaymentDomainException.invalidStateTransition("PENDING", "CAPTURED");

        assertTrue(exception.getMessage().contains("PENDING"));
        assertTrue(exception.getMessage().contains("CAPTURED"));
    }

    @Test
    @DisplayName("authorizationFailed 應產生正確訊息")
    void authorizationFailed_shouldFormatMessage() {
        PaymentDomainException exception = PaymentDomainException.authorizationFailed("餘額不足");

        assertTrue(exception.getMessage().contains("餘額不足"));
        assertTrue(exception.getMessage().contains("authorization"));
    }

    @Test
    @DisplayName("captureFailed 應產生正確訊息")
    void captureFailed_shouldFormatMessage() {
        PaymentDomainException exception = PaymentDomainException.captureFailed("網路錯誤");

        assertTrue(exception.getMessage().contains("網路錯誤"));
        assertTrue(exception.getMessage().contains("capture"));
    }

    @Test
    @DisplayName("例外應繼承 RuntimeException")
    void exception_shouldBeRuntimeException() {
        PaymentDomainException exception = new PaymentDomainException("test");

        assertInstanceOf(RuntimeException.class, exception);
    }
}
