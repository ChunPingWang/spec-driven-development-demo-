package com.example.inventory.infrastructure.adapter.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataInventoryLogRepository extends JpaRepository<InventoryLogJpaEntity, Long> {

    Optional<InventoryLogJpaEntity> findByOrderIdAndProductIdAndOperationType(
            String orderId, String productId, String operationType);
}
