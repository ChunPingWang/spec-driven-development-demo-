package com.example.payment.infrastructure.adapter.outbound.persistence;

import com.example.payment.domain.model.aggregate.Payment;
import com.example.payment.domain.model.valueobject.*;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Payment domain model and JPA entity.
 */
@Component
public class PaymentMapper {

    /**
     * Convert domain Payment to JPA entity.
     */
    public PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setPaymentId(payment.getPaymentId().value());
        entity.setOrderId(payment.getOrderId());
        entity.setAmount(payment.getMoney().amount());
        entity.setCurrency(payment.getMoney().currency());
        entity.setCardLastFour(payment.getCardInfo().lastFour());
        entity.setStatus(payment.getStatus().name());
        entity.setAuthorizationCode(payment.getAuthorizationCode());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    /**
     * Convert JPA entity to domain Payment.
     */
    public Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
                PaymentId.of(entity.getPaymentId()),
                entity.getOrderId(),
                Money.of(entity.getAmount(), entity.getCurrency()),
                CardInfo.of(entity.getCardLastFour(), "12/99"), // Expiry not stored
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getAuthorizationCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Update existing entity from domain Payment.
     */
    public void updateEntity(PaymentJpaEntity entity, Payment payment) {
        entity.setStatus(payment.getStatus().name());
        entity.setAuthorizationCode(payment.getAuthorizationCode());
        entity.setUpdatedAt(payment.getUpdatedAt());
    }
}
