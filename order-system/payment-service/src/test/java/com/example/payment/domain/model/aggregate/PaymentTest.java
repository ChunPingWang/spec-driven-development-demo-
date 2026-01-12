package com.example.payment.domain.model.aggregate;

import com.example.payment.domain.event.PaymentAuthorized;
import com.example.payment.domain.event.PaymentCaptured;
import com.example.payment.domain.event.PaymentVoided;
import com.example.payment.domain.exception.PaymentDomainException;
import com.example.payment.domain.model.valueobject.Money;
import com.example.payment.domain.model.valueobject.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment 聚合根測試")
class PaymentTest {

    @Test
    @DisplayName("建立 Payment 應設定初始狀態為 PENDING")
    void create_shouldSetStatusToPending() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNotNull(payment.getPaymentId());
        assertEquals("ORD-123", payment.getOrderId());
    }

    @Test
    @DisplayName("授權成功應轉換狀態為 AUTHORIZED")
    void authorize_shouldTransitionToAuthorized() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");

        payment.authorize("AUTH-CODE-123");

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
        assertEquals("AUTH-CODE-123", payment.getAuthorizationCode());
    }

    @Test
    @DisplayName("授權應產生 PaymentAuthorized 事件")
    void authorize_shouldRaisePaymentAuthorizedEvent() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");

        payment.authorize("AUTH-CODE-123");

        assertTrue(payment.getDomainEvents().stream()
                .anyMatch(e -> e instanceof PaymentAuthorized));
    }

    @Test
    @DisplayName("從非 PENDING 狀態授權應失敗")
    void authorize_shouldFailFromWrongState() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");
        payment.authorize("AUTH-CODE-123");

        assertThrows(PaymentDomainException.class,
                () -> payment.authorize("AUTH-CODE-456"));
    }

    @Test
    @DisplayName("授權失敗應轉換狀態為 FAILED")
    void failAuthorization_shouldTransitionToFailed() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");

        payment.failAuthorization("Insufficient funds");

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    @DisplayName("請款成功應轉換狀態為 CAPTURED")
    void capture_shouldTransitionToCaptured() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");
        payment.authorize("AUTH-CODE-123");

        payment.capture();

        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
    }

    @Test
    @DisplayName("請款應產生 PaymentCaptured 事件")
    void capture_shouldRaisePaymentCapturedEvent() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");
        payment.authorize("AUTH-CODE-123");
        payment.clearDomainEvents();

        payment.capture();

        assertTrue(payment.getDomainEvents().stream()
                .anyMatch(e -> e instanceof PaymentCaptured));
    }

    @Test
    @DisplayName("從非 AUTHORIZED 狀態請款應失敗")
    void capture_shouldFailFromWrongState() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");

        assertThrows(PaymentDomainException.class, payment::capture);
    }

    @Test
    @DisplayName("請款失敗應保持 AUTHORIZED 狀態")
    void failCapture_shouldStayInAuthorizedState() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");
        payment.authorize("AUTH-CODE-123");

        payment.failCapture("Capture error");

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
    }

    @Test
    @DisplayName("取消授權應轉換狀態為 VOIDED")
    void voidPayment_shouldTransitionToVoided() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");
        payment.authorize("AUTH-CODE-123");

        payment.voidPayment();

        assertEquals(PaymentStatus.VOIDED, payment.getStatus());
        assertNull(payment.getAuthorizationCode());
    }

    @Test
    @DisplayName("取消授權應產生 PaymentVoided 事件")
    void voidPayment_shouldRaisePaymentVoidedEvent() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");
        payment.authorize("AUTH-CODE-123");
        payment.clearDomainEvents();

        payment.voidPayment();

        assertTrue(payment.getDomainEvents().stream()
                .anyMatch(e -> e instanceof PaymentVoided));
    }

    @Test
    @DisplayName("從非 AUTHORIZED 狀態取消授權應失敗")
    void voidPayment_shouldFailFromWrongState() {
        Money money = Money.of(new BigDecimal("35900"), "TWD");
        Payment payment = Payment.create("ORD-123", money, "4111111111111111", "12/26");

        assertThrows(PaymentDomainException.class, payment::voidPayment);
    }
}
