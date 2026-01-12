package com.example.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request DTO for creating an order.
 */
public record CreateOrderRequest(
        @Valid @NotNull BuyerDto buyer,
        @Valid @NotNull OrderItemDto orderItem,
        @Valid @NotNull PaymentDto payment
) {
    public record BuyerDto(
            @NotBlank String name,
            @Email @NotBlank String email
    ) {}

    public record OrderItemDto(
            @NotBlank String productId,
            @NotBlank String productName,
            @Positive int quantity
    ) {}

    public record PaymentDto(
            @NotBlank String method,
            @NotNull @DecimalMin("0") BigDecimal amount,
            @Pattern(regexp = "^[A-Z]{3}$") String currency,
            @NotBlank String cardNumber,
            @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$") String expiryDate,
            @Pattern(regexp = "^[0-9]{3,4}$") String cvv
    ) {}
}
