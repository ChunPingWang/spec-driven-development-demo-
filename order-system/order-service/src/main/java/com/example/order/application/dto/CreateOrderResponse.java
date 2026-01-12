package com.example.order.application.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for order creation.
 */
public record CreateOrderResponse(
        String orderId,
        String status,
        String message,
        LocalDateTime createdAt
) {
    public static CreateOrderResponse success(String orderId, LocalDateTime createdAt) {
        return new CreateOrderResponse(orderId, "COMPLETED", "訂購成功", createdAt);
    }

    public static CreateOrderResponse paymentFailed(String orderId, LocalDateTime createdAt) {
        return new CreateOrderResponse(orderId, "FAILED", "支付失敗", createdAt);
    }

    public static CreateOrderResponse inventoryFailed(String orderId, LocalDateTime createdAt) {
        return new CreateOrderResponse(orderId, "ROLLBACK_COMPLETED", "庫存扣減失敗", createdAt);
    }

    public static CreateOrderResponse captureFailed(String orderId, LocalDateTime createdAt) {
        return new CreateOrderResponse(orderId, "ROLLBACK_COMPLETED", "支付確認失敗", createdAt);
    }

    public static CreateOrderResponse fromExisting(String orderId, String status, LocalDateTime createdAt) {
        String message = switch (status) {
            case "COMPLETED" -> "訂購成功";
            case "FAILED" -> "支付失敗";
            case "ROLLBACK_COMPLETED" -> "訂單已回滾";
            default -> "訂單處理中";
        };
        return new CreateOrderResponse(orderId, status, message, createdAt);
    }
}
