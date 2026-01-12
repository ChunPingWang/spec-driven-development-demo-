package com.example.order.application.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateOrderResponse 測試")
class CreateOrderResponseTest {

    @Test
    @DisplayName("success 應建立成功響應")
    void success_shouldCreateSuccessResponse() {
        LocalDateTime now = LocalDateTime.now();

        CreateOrderResponse response = CreateOrderResponse.success("ORD-12345678", now);

        assertEquals("ORD-12345678", response.orderId());
        assertEquals("COMPLETED", response.status());
        assertEquals("訂購成功", response.message());
        assertEquals(now, response.createdAt());
    }

    @Test
    @DisplayName("paymentFailed 應建立支付失敗響應")
    void paymentFailed_shouldCreatePaymentFailedResponse() {
        LocalDateTime now = LocalDateTime.now();

        CreateOrderResponse response = CreateOrderResponse.paymentFailed("ORD-12345678", now);

        assertEquals("ORD-12345678", response.orderId());
        assertEquals("FAILED", response.status());
        assertEquals("支付失敗", response.message());
    }

    @Test
    @DisplayName("inventoryFailed 應建立庫存失敗響應")
    void inventoryFailed_shouldCreateInventoryFailedResponse() {
        LocalDateTime now = LocalDateTime.now();

        CreateOrderResponse response = CreateOrderResponse.inventoryFailed("ORD-12345678", now);

        assertEquals("ORD-12345678", response.orderId());
        assertEquals("ROLLBACK_COMPLETED", response.status());
        assertEquals("庫存扣減失敗", response.message());
    }

    @Test
    @DisplayName("captureFailed 應建立請款失敗響應")
    void captureFailed_shouldCreateCaptureFailedResponse() {
        LocalDateTime now = LocalDateTime.now();

        CreateOrderResponse response = CreateOrderResponse.captureFailed("ORD-12345678", now);

        assertEquals("ORD-12345678", response.orderId());
        assertEquals("ROLLBACK_COMPLETED", response.status());
        assertEquals("支付確認失敗", response.message());
    }

    @Test
    @DisplayName("fromExisting 處理 COMPLETED 狀態")
    void fromExisting_shouldHandleCompletedStatus() {
        LocalDateTime now = LocalDateTime.now();

        CreateOrderResponse response = CreateOrderResponse.fromExisting("ORD-12345678", "COMPLETED", now);

        assertEquals("訂購成功", response.message());
    }

    @Test
    @DisplayName("fromExisting 處理 FAILED 狀態")
    void fromExisting_shouldHandleFailedStatus() {
        LocalDateTime now = LocalDateTime.now();

        CreateOrderResponse response = CreateOrderResponse.fromExisting("ORD-12345678", "FAILED", now);

        assertEquals("支付失敗", response.message());
    }

    @Test
    @DisplayName("fromExisting 處理 ROLLBACK_COMPLETED 狀態")
    void fromExisting_shouldHandleRollbackCompletedStatus() {
        LocalDateTime now = LocalDateTime.now();

        CreateOrderResponse response = CreateOrderResponse.fromExisting("ORD-12345678", "ROLLBACK_COMPLETED", now);

        assertEquals("訂單已回滾", response.message());
    }

    @Test
    @DisplayName("fromExisting 處理其他狀態")
    void fromExisting_shouldHandleOtherStatus() {
        LocalDateTime now = LocalDateTime.now();

        CreateOrderResponse response = CreateOrderResponse.fromExisting("ORD-12345678", "PROCESSING", now);

        assertEquals("訂單處理中", response.message());
    }
}
