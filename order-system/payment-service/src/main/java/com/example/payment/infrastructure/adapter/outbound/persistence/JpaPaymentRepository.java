package com.example.payment.infrastructure.adapter.outbound.persistence;

import com.example.payment.application.port.outbound.PaymentRepository;
import com.example.payment.domain.model.aggregate.Payment;
import com.example.payment.domain.model.valueobject.PaymentId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA implementation of PaymentRepository port.
 */
@Component
public class JpaPaymentRepository implements PaymentRepository {

    private final SpringDataPaymentRepository springDataRepository;
    private final PaymentMapper mapper;

    public JpaPaymentRepository(SpringDataPaymentRepository springDataRepository, PaymentMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        Optional<PaymentJpaEntity> existingEntity = springDataRepository
                .findByPaymentId(payment.getPaymentId().value());

        PaymentJpaEntity entity;
        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            mapper.updateEntity(entity, payment);
        } else {
            entity = mapper.toEntity(payment);
        }

        PaymentJpaEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return springDataRepository.findByPaymentId(paymentId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return springDataRepository.findByOrderId(orderId)
                .map(mapper::toDomain);
    }
}
