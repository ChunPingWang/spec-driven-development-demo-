package com.example.order.infrastructure.adapter.inbound.rest;

import com.example.order.application.command.CreateOrderCommand;
import com.example.order.application.dto.CreateOrderResponse;
import com.example.order.application.port.inbound.CreateOrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCommandController 測試")
class OrderCommandControllerTest {

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        OrderCommandController controller = new OrderCommandController(createOrderUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("建立訂單成功應返回 201")
    void createOrder_success_shouldReturn201() throws Exception {
        when(createOrderUseCase.execute(any(CreateOrderCommand.class)))
                .thenReturn(CreateOrderResponse.success("ORD-12345678", LocalDateTime.now()));

        String request = """
            {
                "buyer": {
                    "name": "張三",
                    "email": "zhang@example.com"
                },
                "orderItem": {
                    "productId": "PROD-001",
                    "productName": "iPhone 17",
                    "quantity": 1
                },
                "payment": {
                    "method": "CREDIT_CARD",
                    "amount": 35900,
                    "currency": "TWD",
                    "cardNumber": "4111111111111111",
                    "expiryDate": "12/26",
                    "cvv": "123"
                }
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("建立訂單支付失敗應返回 422")
    void createOrder_paymentFailed_shouldReturn422() throws Exception {
        when(createOrderUseCase.execute(any(CreateOrderCommand.class)))
                .thenReturn(CreateOrderResponse.paymentFailed("ORD-12345678", LocalDateTime.now()));

        String request = """
            {
                "buyer": {
                    "name": "張三",
                    "email": "zhang@example.com"
                },
                "orderItem": {
                    "productId": "PROD-001",
                    "productName": "iPhone 17",
                    "quantity": 1
                },
                "payment": {
                    "method": "CREDIT_CARD",
                    "amount": 35900,
                    "currency": "TWD",
                    "cardNumber": "4111111111111111",
                    "expiryDate": "12/26",
                    "cvv": "123"
                }
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    @DisplayName("建立訂單庫存失敗應返回 422")
    void createOrder_inventoryFailed_shouldReturn422() throws Exception {
        when(createOrderUseCase.execute(any(CreateOrderCommand.class)))
                .thenReturn(CreateOrderResponse.inventoryFailed("ORD-12345678", LocalDateTime.now()));

        String request = """
            {
                "buyer": {
                    "name": "張三",
                    "email": "zhang@example.com"
                },
                "orderItem": {
                    "productId": "PROD-001",
                    "productName": "iPhone 17",
                    "quantity": 1
                },
                "payment": {
                    "method": "CREDIT_CARD",
                    "amount": 35900,
                    "currency": "TWD",
                    "cardNumber": "4111111111111111",
                    "expiryDate": "12/26",
                    "cvv": "123"
                }
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("ROLLBACK_COMPLETED"));
    }

    @Test
    @DisplayName("建立訂單帶幂等鍵應使用提供的鍵")
    void createOrder_withIdempotencyKey_shouldUseProvidedKey() throws Exception {
        when(createOrderUseCase.execute(any(CreateOrderCommand.class)))
                .thenReturn(CreateOrderResponse.success("ORD-12345678", LocalDateTime.now()));

        String request = """
            {
                "buyer": {
                    "name": "張三",
                    "email": "zhang@example.com"
                },
                "orderItem": {
                    "productId": "PROD-001",
                    "productName": "iPhone 17",
                    "quantity": 1
                },
                "payment": {
                    "method": "CREDIT_CARD",
                    "amount": 35900,
                    "currency": "TWD",
                    "cardNumber": "4111111111111111",
                    "expiryDate": "12/26",
                    "cvv": "123"
                }
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "IDEM-12345")
                        .content(request))
                .andExpect(status().isCreated());
    }
}
