package com.example.payment.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentStatus 列舉測試")
class PaymentStatusTest {

    @Test
    @DisplayName("應有 PENDING 狀態")
    void shouldHavePendingStatus() {
        assertEquals("PENDING", PaymentStatus.PENDING.name());
    }

    @Test
    @DisplayName("應有 AUTHORIZED 狀態")
    void shouldHaveAuthorizedStatus() {
        assertEquals("AUTHORIZED", PaymentStatus.AUTHORIZED.name());
    }

    @Test
    @DisplayName("應有 CAPTURED 狀態")
    void shouldHaveCapturedStatus() {
        assertEquals("CAPTURED", PaymentStatus.CAPTURED.name());
    }

    @Test
    @DisplayName("應有 FAILED 狀態")
    void shouldHaveFailedStatus() {
        assertEquals("FAILED", PaymentStatus.FAILED.name());
    }

    @Test
    @DisplayName("應有 VOIDED 狀態")
    void shouldHaveVoidedStatus() {
        assertEquals("VOIDED", PaymentStatus.VOIDED.name());
    }

    @Test
    @DisplayName("應有五種狀態")
    void shouldHaveFiveStatuses() {
        assertEquals(5, PaymentStatus.values().length);
    }

    @Test
    @DisplayName("valueOf 應正確轉換字串")
    void valueOf_shouldConvertStringCorrectly() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
        assertEquals(PaymentStatus.AUTHORIZED, PaymentStatus.valueOf("AUTHORIZED"));
        assertEquals(PaymentStatus.CAPTURED, PaymentStatus.valueOf("CAPTURED"));
        assertEquals(PaymentStatus.FAILED, PaymentStatus.valueOf("FAILED"));
        assertEquals(PaymentStatus.VOIDED, PaymentStatus.valueOf("VOIDED"));
    }
}
