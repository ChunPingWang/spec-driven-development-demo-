package com.example.inventory.application.command;

import com.example.inventory.application.port.inbound.DeductStockUseCase;
import com.example.inventory.application.port.outbound.ProductRepository;
import com.example.inventory.domain.exception.InsufficientStockException;
import com.example.inventory.domain.model.aggregate.Product;
import com.example.inventory.domain.model.valueobject.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command handler for deducting stock.
 */
@Service
@Transactional
public class DeductStockCommandHandler implements DeductStockUseCase {

    private final ProductRepository productRepository;

    public DeductStockCommandHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public DeductResult execute(DeductStockCommand command) {
        ProductId productId = ProductId.of(command.productId());

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found: " + command.productId()));

        try {
            product.deductStock(command.orderId(), command.quantity());
            productRepository.save(product);
            return new DeductResult(
                    command.productId(),
                    true,
                    "Stock deducted successfully",
                    product.getCurrentStock()
            );
        } catch (InsufficientStockException e) {
            return new DeductResult(
                    command.productId(),
                    false,
                    e.getMessage(),
                    product.getCurrentStock()
            );
        }
    }
}
