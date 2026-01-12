package com.example.payment.domain.event;

import com.example.payment.domain.model.valueobject.PaymentId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentVoided 事件測試")
class PaymentVoidedTest {

    @Test
    @DisplayName("建立 PaymentVoided 事件應包含所有欄位")
    void of_shouldCreateEventWithAllFields() {
        PaymentId paymentId = PaymentId.generate();

        PaymentVoided event = PaymentVoided.of(paymentId);

        assertEquals(paymentId, event.paymentId());
        assertNotNull(event.occurredOn());
    }

    @Test
    @DisplayName("PaymentVoided 事件應正確比較相等")
    void equals_shouldWorkCorrectly() {
        PaymentId paymentId = PaymentId.of("PAY-001");
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);
        PaymentVoided event1 = new PaymentVoided(paymentId, time);
        PaymentVoided event2 = new PaymentVoided(paymentId, time);

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
