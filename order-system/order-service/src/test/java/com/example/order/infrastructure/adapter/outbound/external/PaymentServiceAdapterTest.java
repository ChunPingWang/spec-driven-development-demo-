package com.example.order.infrastructure.adapter.outbound.external;

import com.example.order.application.port.outbound.PaymentServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceAdapter 測試")
class PaymentServiceAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private PaymentServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PaymentServiceAdapter(restTemplate, "http://payment-service:8080");
    }

    @Test
    @DisplayName("授權應呼叫正確的端點")
    void authorize_shouldCallCorrectEndpoint() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        adapter.authorize(
                "ORD-001", new BigDecimal("35900"), "TWD",
                "4111111111111111", "12/26", "123"
        );

        verify(restTemplate).postForEntity(
                eq("http://payment-service:8080/api/v1/payments/authorize"),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("授權服務不可用應返回失敗結果")
    void authorize_serviceUnavailable_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        PaymentServicePort.AuthorizationResult result = adapter.authorize(
                "ORD-001", new BigDecimal("35900"), "TWD",
                "4111111111111111", "12/26", "123"
        );

        assertFalse(result.success());
        assertTrue(result.message().contains("unavailable"));
    }

    @Test
    @DisplayName("授權空回應應返回失敗結果")
    void authorize_emptyResponse_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        PaymentServicePort.AuthorizationResult result = adapter.authorize(
                "ORD-001", new BigDecimal("35900"), "TWD",
                "4111111111111111", "12/26", "123"
        );

        assertFalse(result.success());
        assertTrue(result.message().contains("Empty response"));
    }

    @Test
    @DisplayName("請款服務不可用應返回失敗結果")
    void capture_serviceUnavailable_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        PaymentServicePort.CaptureResult result = adapter.capture("PAY-001");

        assertFalse(result.succeeded());
        assertTrue(result.message().contains("unavailable"));
    }

    @Test
    @DisplayName("請款空回應應返回失敗結果")
    void capture_emptyResponse_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        PaymentServicePort.CaptureResult result = adapter.capture("PAY-001");

        assertFalse(result.succeeded());
        assertTrue(result.message().contains("Empty response"));
    }

    @Test
    @DisplayName("取消授權服務不可用應返回失敗結果")
    void voidPayment_serviceUnavailable_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        PaymentServicePort.VoidResult result = adapter.voidPayment("PAY-001");

        assertFalse(result.succeeded());
        assertTrue(result.message().contains("unavailable"));
    }

    @Test
    @DisplayName("取消授權空回應應返回失敗結果")
    void voidPayment_emptyResponse_shouldReturnFailure() {
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        PaymentServicePort.VoidResult result = adapter.voidPayment("PAY-001");

        assertFalse(result.succeeded());
        assertTrue(result.message().contains("Empty response"));
    }
}
