package com.example.order.infrastructure.adapter.inbound.rest;

import com.example.order.application.exception.OrderNotFoundException;
import com.example.order.application.port.inbound.GetOrderUseCase;
import com.example.order.application.query.GetOrderQuery;
import com.example.order.application.query.OrderReadModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for order queries.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderQueryController {

    private static final Logger log = LoggerFactory.getLogger(OrderQueryController.class);

    private final GetOrderUseCase getOrderUseCase;

    public OrderQueryController(GetOrderUseCase getOrderUseCase) {
        this.getOrderUseCase = getOrderUseCase;
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves order details by order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderReadModel> getOrder(@PathVariable String orderId) {
        log.info("Received get order request for: {}", orderId);

        GetOrderQuery query = new GetOrderQuery(orderId);
        return getOrderUseCase.execute(query)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
