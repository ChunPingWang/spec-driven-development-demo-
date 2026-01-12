package com.example.inventory.application.command;

import com.example.inventory.application.port.inbound.RollbackStockUseCase;
import com.example.inventory.application.port.outbound.ProductRepository;
import com.example.inventory.domain.model.aggregate.Product;
import com.example.inventory.domain.model.valueobject.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command handler for rolling back stock.
 */
@Service
@Transactional
public class RollbackStockCommandHandler implements RollbackStockUseCase {

    private final ProductRepository productRepository;

    public RollbackStockCommandHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public RollbackResult execute(RollbackStockCommand command) {
        ProductId productId = ProductId.of(command.productId());

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found: " + command.productId()));

        product.rollbackStock(command.orderId(), command.quantity());
        productRepository.save(product);

        return new RollbackResult(
                command.productId(),
                true,
                "Stock rolled back successfully",
                product.getCurrentStock()
        );
    }
}
