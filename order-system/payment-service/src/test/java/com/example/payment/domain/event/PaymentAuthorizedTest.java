package com.example.payment.domain.event;

import com.example.payment.domain.model.valueobject.PaymentId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentAuthorized 事件測試")
class PaymentAuthorizedTest {

    @Test
    @DisplayName("建立 PaymentAuthorized 事件應包含所有欄位")
    void of_shouldCreateEventWithAllFields() {
        PaymentId paymentId = PaymentId.generate();

        PaymentAuthorized event = PaymentAuthorized.of(paymentId, "AUTH-123");

        assertEquals(paymentId, event.paymentId());
        assertEquals("AUTH-123", event.authorizationCode());
        assertNotNull(event.occurredOn());
    }

    @Test
    @DisplayName("PaymentAuthorized 事件應正確比較相等")
    void equals_shouldWorkCorrectly() {
        PaymentId paymentId = PaymentId.of("PAY-001");
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);
        PaymentAuthorized event1 = new PaymentAuthorized(paymentId, "AUTH-123", time);
        PaymentAuthorized event2 = new PaymentAuthorized(paymentId, "AUTH-123", time);

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
