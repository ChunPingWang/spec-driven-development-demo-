package com.example.order.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentInfo 值物件測試")
class PaymentInfoTest {

    @Test
    @DisplayName("建立有效的 PaymentInfo")
    void of_shouldCreateValidPaymentInfo() {
        PaymentInfo info = PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "123");

        assertEquals("CREDIT_CARD", info.method());
        assertEquals("4111111111111111", info.cardNumber());
        assertEquals("12/26", info.expiryDate());
        assertEquals("123", info.cvv());
    }

    @Test
    @DisplayName("空白 method 應拋出例外")
    void of_shouldThrowExceptionForBlankMethod() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentInfo.of("", "4111111111111111", "12/26", "123"));
    }

    @Test
    @DisplayName("空白 cardNumber 應拋出例外")
    void of_shouldThrowExceptionForBlankCardNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentInfo.of("CREDIT_CARD", "", "12/26", "123"));
    }

    @Test
    @DisplayName("無效的到期日格式應拋出例外")
    void of_shouldThrowExceptionForInvalidExpiryDate() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentInfo.of("CREDIT_CARD", "4111111111111111", "1226", "123"));
        assertThrows(IllegalArgumentException.class,
                () -> PaymentInfo.of("CREDIT_CARD", "4111111111111111", "13/26", "123"));
    }

    @Test
    @DisplayName("無效的 CVV 應拋出例外")
    void of_shouldThrowExceptionForInvalidCvv() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "12"));
        assertThrows(IllegalArgumentException.class,
                () -> PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "12345"));
    }

    @Test
    @DisplayName("3 位數 CVV 應有效")
    void of_shouldAcceptThreeDigitCvv() {
        PaymentInfo info = PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "123");
        assertEquals("123", info.cvv());
    }

    @Test
    @DisplayName("4 位數 CVV 應有效（AMEX）")
    void of_shouldAcceptFourDigitCvv() {
        PaymentInfo info = PaymentInfo.of("CREDIT_CARD", "4111111111111111", "12/26", "1234");
        assertEquals("1234", info.cvv());
    }
}
