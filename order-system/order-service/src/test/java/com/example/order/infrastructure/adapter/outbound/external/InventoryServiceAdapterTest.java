package com.example.order.infrastructure.adapter.outbound.external;

import com.example.order.application.port.outbound.InventoryServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceAdapter 測試")
class InventoryServiceAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private InventoryServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryServiceAdapter(restTemplate, "http://inventory-service:8080");
    }

    @Test
    @DisplayName("扣減庫存服務不可用應返回失敗結果")
    void deductStock_serviceUnavailable_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        InventoryServicePort.DeductionResult result = adapter.deductStock("ORD-001", "PROD-001", 5);

        assertFalse(result.success());
        assertTrue(result.message().contains("unavailable"));
    }

    @Test
    @DisplayName("扣減庫存空回應應返回失敗結果")
    void deductStock_emptyResponse_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        InventoryServicePort.DeductionResult result = adapter.deductStock("ORD-001", "PROD-001", 5);

        assertFalse(result.success());
        assertTrue(result.message().contains("Empty response"));
    }

    @Test
    @DisplayName("回滾庫存服務不可用應返回失敗結果")
    void rollbackStock_serviceUnavailable_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        InventoryServicePort.RollbackResult result = adapter.rollbackStock("ORD-001", "PROD-001", 5);

        assertFalse(result.success());
        assertTrue(result.message().contains("unavailable"));
    }

    @Test
    @DisplayName("回滾庫存空回應應返回失敗結果")
    void rollbackStock_emptyResponse_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        InventoryServicePort.RollbackResult result = adapter.rollbackStock("ORD-001", "PROD-001", 5);

        assertFalse(result.success());
        assertTrue(result.message().contains("Empty response"));
    }

    @Test
    @DisplayName("扣減庫存應呼叫正確的端點")
    void deductStock_shouldCallCorrectEndpoint() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        adapter.deductStock("ORD-001", "PROD-001", 5);

        verify(restTemplate).postForEntity(
                eq("http://inventory-service:8080/api/v1/inventory/deduct"),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("回滾庫存應呼叫正確的端點")
    void rollbackStock_shouldCallCorrectEndpoint() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        adapter.rollbackStock("ORD-001", "PROD-001", 5);

        verify(restTemplate).postForEntity(
                eq("http://inventory-service:8080/api/v1/inventory/rollback"),
                any(),
                any()
        );
    }
}
