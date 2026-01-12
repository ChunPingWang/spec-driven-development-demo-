package com.example.inventory.application.command;

import com.example.inventory.application.port.inbound.RollbackStockUseCase;
import com.example.inventory.application.port.outbound.ProductRepository;
import com.example.inventory.domain.model.aggregate.Product;
import com.example.inventory.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RollbackStockCommandHandler 測試")
class RollbackStockCommandHandlerTest {

    @Mock
    private ProductRepository productRepository;

    private RollbackStockCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RollbackStockCommandHandler(productRepository);
    }

    @Test
    @DisplayName("成功回滾庫存")
    void execute_shouldRollbackStockSuccessfully() {
        // Arrange
        Product product = Product.create("IPHONE-17", "iPhone 17 Pro Max", 7);
        when(productRepository.findByIdForUpdate(any(ProductId.class)))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        RollbackStockCommand command = new RollbackStockCommand("ORD-001", "IPHONE-17", 3);

        // Act
        RollbackStockUseCase.RollbackResult result = handler.execute(command);

        // Assert
        assertTrue(result.success());
        assertEquals(10, result.currentStock());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("商品不存在應拋出例外")
    void execute_shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        when(productRepository.findByIdForUpdate(any(ProductId.class)))
                .thenReturn(Optional.empty());

        RollbackStockCommand command = new RollbackStockCommand("ORD-001", "NOT-EXIST", 1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> handler.execute(command));
    }
}
