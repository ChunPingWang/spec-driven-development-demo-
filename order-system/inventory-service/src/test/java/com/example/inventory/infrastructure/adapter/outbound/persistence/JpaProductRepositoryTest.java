package com.example.inventory.infrastructure.adapter.outbound.persistence;

import com.example.inventory.domain.model.aggregate.Product;
import com.example.inventory.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaProductRepository 測試")
class JpaProductRepositoryTest {

    @Mock
    private SpringDataProductRepository springDataRepository;

    @Mock
    private ProductMapper mapper;

    private JpaProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaProductRepository(springDataRepository, mapper);
    }

    @Test
    @DisplayName("儲存新產品應建立新實體")
    void save_newProduct_shouldCreateNewEntity() {
        Product product = Product.create("PROD-001", "iPhone 17", 100);

        ProductJpaEntity newEntity = new ProductJpaEntity();
        newEntity.setId(1L);
        newEntity.setProductId("PROD-001");
        newEntity.setProductName("iPhone 17");
        newEntity.setStockQuantity(100);
        newEntity.setCreatedAt(LocalDateTime.now());
        newEntity.setUpdatedAt(LocalDateTime.now());

        when(springDataRepository.findByProductId("PROD-001")).thenReturn(Optional.empty());
        when(mapper.toEntity(product)).thenReturn(newEntity);
        when(springDataRepository.save(newEntity)).thenReturn(newEntity);
        when(mapper.toDomain(newEntity)).thenReturn(product);

        Product result = repository.save(product);

        assertNotNull(result);
        verify(springDataRepository).findByProductId("PROD-001");
        verify(mapper).toEntity(product);
        verify(springDataRepository).save(newEntity);
        verify(mapper).toDomain(newEntity);
    }

    @Test
    @DisplayName("儲存已存在產品應更新實體")
    void save_existingProduct_shouldUpdateEntity() {
        Product product = Product.create("PROD-001", "iPhone 17", 90);

        ProductJpaEntity existingEntity = new ProductJpaEntity();
        existingEntity.setId(1L);
        existingEntity.setProductId("PROD-001");
        existingEntity.setProductName("iPhone 17");
        existingEntity.setStockQuantity(100);
        existingEntity.setCreatedAt(LocalDateTime.now());
        existingEntity.setUpdatedAt(LocalDateTime.now());

        when(springDataRepository.findByProductId("PROD-001")).thenReturn(Optional.of(existingEntity));
        when(springDataRepository.save(existingEntity)).thenReturn(existingEntity);
        when(mapper.toDomain(existingEntity)).thenReturn(product);

        Product result = repository.save(product);

        assertNotNull(result);
        verify(springDataRepository).findByProductId("PROD-001");
        verify(mapper).updateEntity(existingEntity, product);
        verify(springDataRepository).save(existingEntity);
        verify(mapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("依 ID 查詢產品存在時應返回產品")
    void findById_whenExists_shouldReturnProduct() {
        ProductId productId = ProductId.of("PROD-001");
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setProductId("PROD-001");

        Product expectedProduct = Product.create("PROD-001", "iPhone 17", 100);

        when(springDataRepository.findByProductId("PROD-001")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expectedProduct);

        Optional<Product> result = repository.findById(productId);

        assertTrue(result.isPresent());
        assertEquals(expectedProduct, result.get());
    }

    @Test
    @DisplayName("依 ID 查詢產品不存在時應返回空")
    void findById_whenNotExists_shouldReturnEmpty() {
        ProductId productId = ProductId.of("PROD-999");

        when(springDataRepository.findByProductId("PROD-999")).thenReturn(Optional.empty());

        Optional<Product> result = repository.findById(productId);

        assertTrue(result.isEmpty());
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("悲觀鎖查詢產品存在時應返回產品")
    void findByIdForUpdate_whenExists_shouldReturnProduct() {
        ProductId productId = ProductId.of("PROD-001");
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setProductId("PROD-001");

        Product expectedProduct = Product.create("PROD-001", "iPhone 17", 100);

        when(springDataRepository.findByProductIdForUpdate("PROD-001")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expectedProduct);

        Optional<Product> result = repository.findByIdForUpdate(productId);

        assertTrue(result.isPresent());
        assertEquals(expectedProduct, result.get());
    }

    @Test
    @DisplayName("悲觀鎖查詢產品不存在時應返回空")
    void findByIdForUpdate_whenNotExists_shouldReturnEmpty() {
        ProductId productId = ProductId.of("PROD-999");

        when(springDataRepository.findByProductIdForUpdate("PROD-999")).thenReturn(Optional.empty());

        Optional<Product> result = repository.findByIdForUpdate(productId);

        assertTrue(result.isEmpty());
        verify(mapper, never()).toDomain(any());
    }
}
