package com.example.order.application.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateOrderRequest 測試")
class CreateOrderRequestTest {

    @Test
    @DisplayName("建立有效的 CreateOrderRequest")
    void createValidRequest() {
        CreateOrderRequest.BuyerDto buyer = new CreateOrderRequest.BuyerDto("張三", "zhang@example.com");
        CreateOrderRequest.OrderItemDto orderItem = new CreateOrderRequest.OrderItemDto("PROD-001", "iPhone 17", 1);
        CreateOrderRequest.PaymentDto payment = new CreateOrderRequest.PaymentDto(
                "CREDIT_CARD", new BigDecimal("35900"), "TWD",
                "4111111111111111", "12/26", "123"
        );

        CreateOrderRequest request = new CreateOrderRequest(buyer, orderItem, payment);

        assertEquals("張三", request.buyer().name());
        assertEquals("zhang@example.com", request.buyer().email());
        assertEquals("PROD-001", request.orderItem().productId());
        assertEquals("iPhone 17", request.orderItem().productName());
        assertEquals(1, request.orderItem().quantity());
        assertEquals("CREDIT_CARD", request.payment().method());
        assertEquals(new BigDecimal("35900"), request.payment().amount());
        assertEquals("TWD", request.payment().currency());
    }

    @Test
    @DisplayName("BuyerDto 應正確建立")
    void buyerDto_shouldCreateCorrectly() {
        CreateOrderRequest.BuyerDto buyer = new CreateOrderRequest.BuyerDto("李四", "li@example.com");

        assertEquals("李四", buyer.name());
        assertEquals("li@example.com", buyer.email());
    }

    @Test
    @DisplayName("OrderItemDto 應正確建立")
    void orderItemDto_shouldCreateCorrectly() {
        CreateOrderRequest.OrderItemDto orderItem = new CreateOrderRequest.OrderItemDto("PROD-002", "MacBook Pro", 2);

        assertEquals("PROD-002", orderItem.productId());
        assertEquals("MacBook Pro", orderItem.productName());
        assertEquals(2, orderItem.quantity());
    }

    @Test
    @DisplayName("PaymentDto 應正確建立")
    void paymentDto_shouldCreateCorrectly() {
        CreateOrderRequest.PaymentDto payment = new CreateOrderRequest.PaymentDto(
                "DEBIT_CARD", new BigDecimal("50000"), "USD",
                "5500000000000004", "01/27", "456"
        );

        assertEquals("DEBIT_CARD", payment.method());
        assertEquals(new BigDecimal("50000"), payment.amount());
        assertEquals("USD", payment.currency());
        assertEquals("5500000000000004", payment.cardNumber());
        assertEquals("01/27", payment.expiryDate());
        assertEquals("456", payment.cvv());
    }
}
