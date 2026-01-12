package com.example.inventory.infrastructure.adapter.outbound.persistence;

import com.example.inventory.domain.model.aggregate.Product;
import com.example.inventory.domain.model.valueobject.ProductId;
import com.example.inventory.domain.model.valueobject.StockQuantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductMapper 測試")
class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    @DisplayName("toEntity 應正確轉換 Product 到 JPA Entity")
    void toEntity_shouldConvertProductToEntity() {
        Product product = Product.create("PROD-001", "iPhone 17", 100);

        ProductJpaEntity entity = mapper.toEntity(product);

        assertEquals("PROD-001", entity.getProductId());
        assertEquals("iPhone 17", entity.getProductName());
        assertEquals(100, entity.getStockQuantity());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("toDomain 應正確轉換 JPA Entity 到 Product")
    void toDomain_shouldConvertEntityToProduct() {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setProductId("PROD-001");
        entity.setProductName("iPhone 17");
        entity.setStockQuantity(100);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Product product = mapper.toDomain(entity);

        assertEquals("PROD-001", product.getProductId().value());
        assertEquals("iPhone 17", product.getProductName());
        assertEquals(100, product.getCurrentStock());
    }

    @Test
    @DisplayName("updateEntity 應正確更新 Entity")
    void updateEntity_shouldUpdateEntityFromProduct() {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setProductId("PROD-001");
        entity.setProductName("iPhone 17");
        entity.setStockQuantity(100);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Product product = Product.reconstitute(
                ProductId.of("PROD-001"),
                "iPhone 17",
                StockQuantity.of(80),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        mapper.updateEntity(entity, product);

        assertEquals(80, entity.getStockQuantity());
    }
}
