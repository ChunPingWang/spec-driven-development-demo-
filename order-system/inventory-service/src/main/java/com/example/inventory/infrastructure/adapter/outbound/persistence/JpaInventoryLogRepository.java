package com.example.inventory.infrastructure.adapter.outbound.persistence;

import com.example.inventory.application.port.outbound.InventoryLogRepository;
import com.example.inventory.domain.model.valueobject.InventoryOperation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JPA implementation of InventoryLogRepository port.
 */
@Component
public class JpaInventoryLogRepository implements InventoryLogRepository {

    private final SpringDataInventoryLogRepository springDataRepository;

    public JpaInventoryLogRepository(SpringDataInventoryLogRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public InventoryLogEntry save(InventoryLogEntry entry) {
        InventoryLogJpaEntity entity = new InventoryLogJpaEntity();
        entity.setOrderId(entry.orderId());
        entity.setProductId(entry.productId());
        entity.setOperationType(entry.operationType().name());
        entity.setQuantity(entry.quantity());
        entity.setStatus(entry.status());
        entity.setCreatedAt(LocalDateTime.now());

        InventoryLogJpaEntity savedEntity = springDataRepository.save(entity);

        return new InventoryLogEntry(
                savedEntity.getId(),
                savedEntity.getOrderId(),
                savedEntity.getProductId(),
                InventoryOperation.valueOf(savedEntity.getOperationType()),
                savedEntity.getQuantity(),
                savedEntity.getStatus()
        );
    }

    @Override
    public Optional<InventoryLogEntry> findByOrderAndProductAndOperation(
            String orderId, String productId, InventoryOperation operationType) {
        return springDataRepository
                .findByOrderIdAndProductIdAndOperationType(orderId, productId, operationType.name())
                .map(entity -> new InventoryLogEntry(
                        entity.getId(),
                        entity.getOrderId(),
                        entity.getProductId(),
                        InventoryOperation.valueOf(entity.getOperationType()),
                        entity.getQuantity(),
                        entity.getStatus()
                ));
    }
}
