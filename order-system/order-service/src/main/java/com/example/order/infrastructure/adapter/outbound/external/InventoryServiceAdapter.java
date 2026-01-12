package com.example.order.infrastructure.adapter.outbound.external;

import com.example.order.application.port.outbound.InventoryServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP adapter for inventory service communication.
 */
@Component
public class InventoryServiceAdapter implements InventoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceAdapter.class);

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public InventoryServiceAdapter(
            RestTemplate restTemplate,
            @Value("${services.inventory.url}") String inventoryServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    @Override
    public DeductionResult deductStock(String orderId, String productId, int quantity) {
        log.info("Calling inventory service to deduct stock: productId={}, quantity={}",
                productId, quantity);

        try {
            Map<String, Object> request = Map.of(
                    "orderId", orderId,
                    "productId", productId,
                    "quantity", quantity
            );

            ResponseEntity<DeductResponse> response = restTemplate.postForEntity(
                    inventoryServiceUrl + "/api/v1/inventory/deduct",
                    request,
                    DeductResponse.class
            );

            DeductResponse body = response.getBody();
            if (body == null) {
                return DeductionResult.failure("Empty response from inventory service");
            }

            if (body.success()) {
                return DeductionResult.success(body.remainingStock());
            } else {
                return DeductionResult.failure(body.message());
            }
        } catch (RestClientException e) {
            log.error("Inventory service deduction failed", e);
            return DeductionResult.failure("Inventory service unavailable: " + e.getMessage());
        }
    }

    @Override
    public RollbackResult rollbackStock(String orderId, String productId, int quantity) {
        log.info("Calling inventory service to rollback stock: productId={}, quantity={}",
                productId, quantity);

        try {
            Map<String, Object> request = Map.of(
                    "orderId", orderId,
                    "productId", productId,
                    "quantity", quantity
            );

            ResponseEntity<RollbackResponse> response = restTemplate.postForEntity(
                    inventoryServiceUrl + "/api/v1/inventory/rollback",
                    request,
                    RollbackResponse.class
            );

            RollbackResponse body = response.getBody();
            if (body == null) {
                return RollbackResult.failure("Empty response from inventory service");
            }

            if (body.success()) {
                return RollbackResult.success(body.currentStock());
            } else {
                return RollbackResult.failure(body.message());
            }
        } catch (RestClientException e) {
            log.error("Inventory service rollback failed", e);
            return RollbackResult.failure("Inventory service unavailable: " + e.getMessage());
        }
    }

    // Response DTOs for inventory service
    private record DeductResponse(
            String productId,
            boolean success,
            String message,
            int remainingStock
    ) {}

    private record RollbackResponse(
            String productId,
            boolean success,
            String message,
            int currentStock
    ) {}
}
