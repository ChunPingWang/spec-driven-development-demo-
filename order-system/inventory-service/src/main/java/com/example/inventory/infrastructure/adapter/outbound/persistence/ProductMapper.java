package com.example.inventory.infrastructure.adapter.outbound.persistence;

import com.example.inventory.domain.model.aggregate.Product;
import com.example.inventory.domain.model.valueobject.*;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Product domain model and JPA entity.
 */
@Component
public class ProductMapper {

    /**
     * Convert domain Product to JPA entity.
     */
    public ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setProductId(product.getProductId().value());
        entity.setProductName(product.getProductName());
        entity.setStockQuantity(product.getCurrentStock());
        entity.setCreatedAt(product.getCreatedAt());
        entity.setUpdatedAt(product.getUpdatedAt());
        return entity;
    }

    /**
     * Convert JPA entity to domain Product.
     */
    public Product toDomain(ProductJpaEntity entity) {
        return Product.reconstitute(
                ProductId.of(entity.getProductId()),
                entity.getProductName(),
                StockQuantity.of(entity.getStockQuantity()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Update existing entity from domain Product.
     */
    public void updateEntity(ProductJpaEntity entity, Product product) {
        entity.setStockQuantity(product.getCurrentStock());
        entity.setUpdatedAt(product.getUpdatedAt());
    }
}
