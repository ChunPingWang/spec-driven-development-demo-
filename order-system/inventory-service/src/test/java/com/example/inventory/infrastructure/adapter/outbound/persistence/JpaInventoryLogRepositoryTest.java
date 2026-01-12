package com.example.inventory.infrastructure.adapter.outbound.persistence;

import com.example.inventory.application.port.outbound.InventoryLogRepository.InventoryLogEntry;
import com.example.inventory.domain.model.valueobject.InventoryOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaInventoryLogRepository 測試")
class JpaInventoryLogRepositoryTest {

    @Mock
    private SpringDataInventoryLogRepository springDataRepository;

    private JpaInventoryLogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaInventoryLogRepository(springDataRepository);
    }

    @Test
    @DisplayName("儲存庫存日誌應建立實體並返回完整記錄")
    void save_shouldCreateEntityAndReturnEntry() {
        InventoryLogEntry entry = new InventoryLogEntry(
                null, "ORD-12345678", "PROD-001", InventoryOperation.DEDUCT, 5, "SUCCESS"
        );

        InventoryLogJpaEntity savedEntity = new InventoryLogJpaEntity();
        savedEntity.setId(1L);
        savedEntity.setOrderId("ORD-12345678");
        savedEntity.setProductId("PROD-001");
        savedEntity.setOperationType("DEDUCT");
        savedEntity.setQuantity(5);
        savedEntity.setStatus("SUCCESS");
        savedEntity.setCreatedAt(LocalDateTime.now());

        when(springDataRepository.save(any(InventoryLogJpaEntity.class))).thenReturn(savedEntity);

        InventoryLogEntry result = repository.save(entry);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ORD-12345678", result.orderId());
        assertEquals("PROD-001", result.productId());
        assertEquals(InventoryOperation.DEDUCT, result.operationType());
        assertEquals(5, result.quantity());
        assertEquals("SUCCESS", result.status());

        ArgumentCaptor<InventoryLogJpaEntity> captor = ArgumentCaptor.forClass(InventoryLogJpaEntity.class);
        verify(springDataRepository).save(captor.capture());

        InventoryLogJpaEntity capturedEntity = captor.getValue();
        assertEquals("ORD-12345678", capturedEntity.getOrderId());
        assertEquals("PROD-001", capturedEntity.getProductId());
        assertEquals("DEDUCT", capturedEntity.getOperationType());
    }

    @Test
    @DisplayName("依訂單、產品、操作類型查詢存在時應返回日誌")
    void findByOrderAndProductAndOperation_whenExists_shouldReturnEntry() {
        InventoryLogJpaEntity entity = new InventoryLogJpaEntity();
        entity.setId(1L);
        entity.setOrderId("ORD-12345678");
        entity.setProductId("PROD-001");
        entity.setOperationType("DEDUCT");
        entity.setQuantity(5);
        entity.setStatus("SUCCESS");

        when(springDataRepository.findByOrderIdAndProductIdAndOperationType(
                "ORD-12345678", "PROD-001", "DEDUCT"))
                .thenReturn(Optional.of(entity));

        Optional<InventoryLogEntry> result = repository.findByOrderAndProductAndOperation(
                "ORD-12345678", "PROD-001", InventoryOperation.DEDUCT);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
        assertEquals("ORD-12345678", result.get().orderId());
        assertEquals("PROD-001", result.get().productId());
        assertEquals(InventoryOperation.DEDUCT, result.get().operationType());
    }

    @Test
    @DisplayName("依訂單、產品、操作類型查詢不存在時應返回空")
    void findByOrderAndProductAndOperation_whenNotExists_shouldReturnEmpty() {
        when(springDataRepository.findByOrderIdAndProductIdAndOperationType(
                "ORD-99999999", "PROD-999", "DEDUCT"))
                .thenReturn(Optional.empty());

        Optional<InventoryLogEntry> result = repository.findByOrderAndProductAndOperation(
                "ORD-99999999", "PROD-999", InventoryOperation.DEDUCT);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("儲存回滾日誌應正確設定操作類型")
    void save_rollbackOperation_shouldSetCorrectType() {
        InventoryLogEntry entry = new InventoryLogEntry(
                null, "ORD-12345678", "PROD-001", InventoryOperation.ROLLBACK, 5, "SUCCESS"
        );

        InventoryLogJpaEntity savedEntity = new InventoryLogJpaEntity();
        savedEntity.setId(2L);
        savedEntity.setOrderId("ORD-12345678");
        savedEntity.setProductId("PROD-001");
        savedEntity.setOperationType("ROLLBACK");
        savedEntity.setQuantity(5);
        savedEntity.setStatus("SUCCESS");

        when(springDataRepository.save(any(InventoryLogJpaEntity.class))).thenReturn(savedEntity);

        InventoryLogEntry result = repository.save(entry);

        assertEquals(InventoryOperation.ROLLBACK, result.operationType());

        ArgumentCaptor<InventoryLogJpaEntity> captor = ArgumentCaptor.forClass(InventoryLogJpaEntity.class);
        verify(springDataRepository).save(captor.capture());
        assertEquals("ROLLBACK", captor.getValue().getOperationType());
    }
}
