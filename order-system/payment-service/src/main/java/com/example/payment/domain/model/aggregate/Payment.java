package com.example.payment.domain.model.aggregate;

import com.example.payment.domain.event.*;
import com.example.payment.domain.exception.PaymentDomainException;
import com.example.payment.domain.model.valueobject.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Payment Aggregate Root - manages payment lifecycle with two-phase commit.
 */
public class Payment {

    private PaymentId paymentId;
    private String orderId;
    private Money money;
    private CardInfo cardInfo;
    private PaymentStatus status;
    private String authorizationCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Private constructor for factory method
    private Payment() {
    }

    /**
     * Factory method to create a new Payment.
     */
    public static Payment create(
            String orderId,
            Money money,
            String cardNumber,
            String expiryDate
    ) {
        Payment payment = new Payment();
        payment.paymentId = PaymentId.generate();
        payment.orderId = orderId;
        payment.money = money;
        payment.cardInfo = CardInfo.fromCardNumber(cardNumber, expiryDate);
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = LocalDateTime.now();
        payment.updatedAt = payment.createdAt;
        return payment;
    }

    /**
     * Reconstitute a Payment from persistence.
     */
    public static Payment reconstitute(
            PaymentId paymentId,
            String orderId,
            Money money,
            CardInfo cardInfo,
            PaymentStatus status,
            String authorizationCode,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Payment payment = new Payment();
        payment.paymentId = paymentId;
        payment.orderId = orderId;
        payment.money = money;
        payment.cardInfo = cardInfo;
        payment.status = status;
        payment.authorizationCode = authorizationCode;
        payment.createdAt = createdAt;
        payment.updatedAt = updatedAt;
        return payment;
    }

    /**
     * Authorize the payment.
     * Valid transition: PENDING -> AUTHORIZED
     */
    public void authorize(String authorizationCode) {
        validateStateTransition(PaymentStatus.PENDING, PaymentStatus.AUTHORIZED);
        this.authorizationCode = authorizationCode;
        this.status = PaymentStatus.AUTHORIZED;
        this.updatedAt = LocalDateTime.now();
        this.domainEvents.add(PaymentAuthorized.of(this.paymentId, authorizationCode));
    }

    /**
     * Mark authorization as failed.
     * Valid transition: PENDING -> FAILED
     */
    public void failAuthorization(String reason) {
        validateStateTransition(PaymentStatus.PENDING, PaymentStatus.FAILED);
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Capture the payment.
     * Valid transition: AUTHORIZED -> CAPTURED
     */
    public void capture() {
        validateStateTransition(PaymentStatus.AUTHORIZED, PaymentStatus.CAPTURED);
        this.status = PaymentStatus.CAPTURED;
        this.updatedAt = LocalDateTime.now();
        this.domainEvents.add(PaymentCaptured.of(this.paymentId));
    }

    /**
     * Mark capture as failed (stays in AUTHORIZED state for void).
     */
    public void failCapture(String reason) {
        if (this.status != PaymentStatus.AUTHORIZED) {
            throw PaymentDomainException.invalidStateTransition(
                    this.status.name(), "CAPTURE_FAILED");
        }
        // Status remains AUTHORIZED - can be voided
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Void the payment.
     * Valid transition: AUTHORIZED -> VOIDED
     */
    public void voidPayment() {
        validateStateTransition(PaymentStatus.AUTHORIZED, PaymentStatus.VOIDED);
        this.status = PaymentStatus.VOIDED;
        this.authorizationCode = null;
        this.updatedAt = LocalDateTime.now();
        this.domainEvents.add(PaymentVoided.of(this.paymentId));
    }

    private void validateStateTransition(PaymentStatus expectedCurrent, PaymentStatus target) {
        if (this.status != expectedCurrent) {
            throw PaymentDomainException.invalidStateTransition(this.status.name(), target.name());
        }
    }

    // Getters
    public PaymentId getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Money getMoney() {
        return money;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
