package com.example.order.infrastructure.adapter.inbound.rest;

import com.example.order.application.command.CreateOrderCommand;
import com.example.order.application.dto.CreateOrderRequest;
import com.example.order.application.dto.CreateOrderResponse;
import com.example.order.application.port.inbound.CreateOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for order commands.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderCommandController {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandController.class);

    private final CreateOrderUseCase createOrderUseCase;

    public OrderCommandController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order and processes payment and inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "422", description = "Order processing failed")
    })
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        // Generate idempotency key if not provided
        String key = idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString();
        log.info("Received create order request with idempotency key: {}", key);

        // Map request to command
        CreateOrderCommand command = new CreateOrderCommand(
                key,
                request.buyer().name(),
                request.buyer().email(),
                request.orderItem().productId(),
                request.orderItem().productName(),
                request.orderItem().quantity(),
                request.payment().amount(),
                request.payment().currency(),
                request.payment().method(),
                request.payment().cardNumber(),
                request.payment().expiryDate(),
                request.payment().cvv()
        );

        // Execute use case
        CreateOrderResponse response = createOrderUseCase.execute(command);

        // Return appropriate status based on result
        HttpStatus status = switch (response.status()) {
            case "COMPLETED" -> HttpStatus.CREATED;
            case "FAILED" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "ROLLBACK_COMPLETED" -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.ACCEPTED;
        };

        return ResponseEntity.status(status).body(response);
    }
}
