package com.example.payment.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentId 值物件測試")
class PaymentIdTest {

    @Test
    @DisplayName("生成的 PaymentId 應以 PAY- 開頭")
    void generate_shouldCreatePaymentIdWithCorrectPrefix() {
        PaymentId paymentId = PaymentId.generate();

        assertNotNull(paymentId);
        assertTrue(paymentId.value().startsWith("PAY-"));
    }

    @Test
    @DisplayName("從字串建立 PaymentId")
    void of_shouldCreatePaymentIdFromString() {
        String value = "PAY-12345678";
        PaymentId paymentId = PaymentId.of(value);

        assertEquals(value, paymentId.value());
    }

    @Test
    @DisplayName("空值應拋出例外")
    void of_shouldThrowExceptionForNullValue() {
        assertThrows(IllegalArgumentException.class, () -> PaymentId.of(null));
    }

    @Test
    @DisplayName("空白字串應拋出例外")
    void of_shouldThrowExceptionForBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> PaymentId.of(""));
    }
}
