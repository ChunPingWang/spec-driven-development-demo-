package com.example.inventory.application.port.outbound;

import com.example.inventory.domain.model.aggregate.Product;
import com.example.inventory.domain.model.valueobject.ProductId;

import java.util.Optional;

/**
 * Port for product persistence operations.
 */
public interface ProductRepository {

    /**
     * Save a product.
     */
    Product save(Product product);

    /**
     * Find a product by its ID.
     */
    Optional<Product> findById(ProductId productId);

    /**
     * Find a product by its ID with pessimistic lock for stock operations.
     */
    Optional<Product> findByIdForUpdate(ProductId productId);
}
