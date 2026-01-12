package com.example.inventory.infrastructure.adapter.outbound.persistence;

import com.example.inventory.application.port.outbound.ProductRepository;
import com.example.inventory.domain.model.aggregate.Product;
import com.example.inventory.domain.model.valueobject.ProductId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA implementation of ProductRepository port.
 */
@Component
public class JpaProductRepository implements ProductRepository {

    private final SpringDataProductRepository springDataRepository;
    private final ProductMapper mapper;

    public JpaProductRepository(SpringDataProductRepository springDataRepository, ProductMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        Optional<ProductJpaEntity> existingEntity = springDataRepository
                .findByProductId(product.getProductId().value());

        ProductJpaEntity entity;
        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            mapper.updateEntity(entity, product);
        } else {
            entity = mapper.toEntity(product);
        }

        ProductJpaEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return springDataRepository.findByProductId(productId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByIdForUpdate(ProductId productId) {
        return springDataRepository.findByProductIdForUpdate(productId.value())
                .map(mapper::toDomain);
    }
}
