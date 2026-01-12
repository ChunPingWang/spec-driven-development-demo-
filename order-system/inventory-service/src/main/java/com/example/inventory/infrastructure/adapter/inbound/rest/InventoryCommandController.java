package com.example.inventory.infrastructure.adapter.inbound.rest;

import com.example.inventory.application.command.DeductStockCommand;
import com.example.inventory.application.command.RollbackStockCommand;
import com.example.inventory.application.port.inbound.DeductStockUseCase;
import com.example.inventory.application.port.inbound.RollbackStockUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for inventory commands.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryCommandController {

    private static final Logger log = LoggerFactory.getLogger(InventoryCommandController.class);

    private final DeductStockUseCase deductStockUseCase;
    private final RollbackStockUseCase rollbackStockUseCase;

    public InventoryCommandController(
            DeductStockUseCase deductStockUseCase,
            RollbackStockUseCase rollbackStockUseCase
    ) {
        this.deductStockUseCase = deductStockUseCase;
        this.rollbackStockUseCase = rollbackStockUseCase;
    }

    @PostMapping("/deduct")
    @Operation(summary = "Deduct stock", description = "Deducts stock for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deduction processed"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<DeductResponse> deductStock(@Valid @RequestBody DeductRequest request) {
        log.info("Received deduct request: productId={}, quantity={}",
                request.productId(), request.quantity());

        DeductStockCommand command = new DeductStockCommand(
                request.orderId(),
                request.productId(),
                request.quantity()
        );

        DeductStockUseCase.DeductResult result = deductStockUseCase.execute(command);

        return ResponseEntity.ok(new DeductResponse(
                request.productId(),
                result.success(),
                result.message(),
                result.remainingStock()
        ));
    }

    @PostMapping("/rollback")
    @Operation(summary = "Rollback stock", description = "Rolls back stock for a failed order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rollback processed"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<RollbackResponse> rollbackStock(@Valid @RequestBody RollbackRequest request) {
        log.info("Received rollback request: productId={}, quantity={}",
                request.productId(), request.quantity());

        RollbackStockCommand command = new RollbackStockCommand(
                request.orderId(),
                request.productId(),
                request.quantity()
        );

        RollbackStockUseCase.RollbackResult result = rollbackStockUseCase.execute(command);

        return ResponseEntity.ok(new RollbackResponse(
                request.productId(),
                result.success(),
                result.message(),
                result.currentStock()
        ));
    }

    // Request/Response DTOs
    public record DeductRequest(
            @NotBlank String orderId,
            @NotBlank String productId,
            @Positive int quantity
    ) {}

    public record DeductResponse(
            String productId,
            boolean success,
            String message,
            int remainingStock
    ) {}

    public record RollbackRequest(
            @NotBlank String orderId,
            @NotBlank String productId,
            @Positive int quantity
    ) {}

    public record RollbackResponse(
            String productId,
            boolean success,
            String message,
            int currentStock
    ) {}
}
