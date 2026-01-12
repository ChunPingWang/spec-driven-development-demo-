package com.example.order.infrastructure.adapter.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, Long> {

    Optional<OrderJpaEntity> findByOrderId(String orderId);

    Optional<OrderJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
