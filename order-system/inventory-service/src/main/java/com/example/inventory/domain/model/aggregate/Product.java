package com.example.inventory.domain.model.aggregate;

import com.example.inventory.domain.event.*;
import com.example.inventory.domain.exception.InsufficientStockException;
import com.example.inventory.domain.model.valueobject.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Product Aggregate Root - manages product stock levels.
 */
public class Product {

    private ProductId productId;
    private String productName;
    private StockQuantity stockQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Private constructor for factory method
    private Product() {
    }

    /**
     * Factory method to create a new Product.
     */
    public static Product create(
            String productId,
            String productName,
            int initialStock
    ) {
        Product product = new Product();
        product.productId = ProductId.of(productId);
        product.productName = productName;
        product.stockQuantity = StockQuantity.of(initialStock);
        product.createdAt = LocalDateTime.now();
        product.updatedAt = product.createdAt;
        return product;
    }

    /**
     * Reconstitute a Product from persistence.
     */
    public static Product reconstitute(
            ProductId productId,
            String productName,
            StockQuantity stockQuantity,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Product product = new Product();
        product.productId = productId;
        product.productName = productName;
        product.stockQuantity = stockQuantity;
        product.createdAt = createdAt;
        product.updatedAt = updatedAt;
        return product;
    }

    /**
     * Deduct stock for an order.
     * @throws InsufficientStockException if not enough stock available
     */
    public void deductStock(String orderId, int quantity) {
        if (!stockQuantity.hasSufficientStock(quantity)) {
            throw new InsufficientStockException(
                    productId.value(), quantity, stockQuantity.value());
        }
        this.stockQuantity = stockQuantity.deduct(quantity);
        this.updatedAt = LocalDateTime.now();
        this.domainEvents.add(StockDeducted.of(
                this.productId, orderId, quantity, stockQuantity.value()));
    }

    /**
     * Rollback stock for a failed order.
     */
    public void rollbackStock(String orderId, int quantity) {
        this.stockQuantity = stockQuantity.add(quantity);
        this.updatedAt = LocalDateTime.now();
        this.domainEvents.add(StockRolledBack.of(
                this.productId, orderId, quantity, stockQuantity.value()));
    }

    /**
     * Check if sufficient stock is available.
     */
    public boolean hasSufficientStock(int quantity) {
        return stockQuantity.hasSufficientStock(quantity);
    }

    // Getters
    public ProductId getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public StockQuantity getStockQuantity() {
        return stockQuantity;
    }

    public int getCurrentStock() {
        return stockQuantity.value();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
