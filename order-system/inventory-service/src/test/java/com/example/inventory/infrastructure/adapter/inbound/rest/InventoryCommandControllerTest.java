package com.example.inventory.infrastructure.adapter.inbound.rest;

import com.example.inventory.application.command.DeductStockCommand;
import com.example.inventory.application.command.RollbackStockCommand;
import com.example.inventory.application.port.inbound.DeductStockUseCase;
import com.example.inventory.application.port.inbound.RollbackStockUseCase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryCommandController 測試")
class InventoryCommandControllerTest {

    @Mock
    private DeductStockUseCase deductStockUseCase;

    @Mock
    private RollbackStockUseCase rollbackStockUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        InventoryCommandController controller = new InventoryCommandController(
                deductStockUseCase, rollbackStockUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("扣減庫存成功應返回 200")
    void deductStock_success_shouldReturn200() throws Exception {
        when(deductStockUseCase.execute(any(DeductStockCommand.class)))
                .thenReturn(new DeductStockUseCase.DeductResult("PROD-001", true, "Stock deducted", 95));

        String request = """
            {
                "orderId": "ORD-001",
                "productId": "PROD-001",
                "quantity": 5
            }
            """;

        mockMvc.perform(post("/api/v1/inventory/deduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.remainingStock").value(95));
    }

    @Test
    @DisplayName("扣減庫存失敗應返回失敗結果")
    void deductStock_failure_shouldReturnFailure() throws Exception {
        when(deductStockUseCase.execute(any(DeductStockCommand.class)))
                .thenReturn(new DeductStockUseCase.DeductResult("PROD-001", false, "Insufficient stock", 10));

        String request = """
            {
                "orderId": "ORD-001",
                "productId": "PROD-001",
                "quantity": 15
            }
            """;

        mockMvc.perform(post("/api/v1/inventory/deduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Insufficient stock"));
    }

    @Test
    @DisplayName("回滾庫存成功應返回 200")
    void rollbackStock_success_shouldReturn200() throws Exception {
        when(rollbackStockUseCase.execute(any(RollbackStockCommand.class)))
                .thenReturn(new RollbackStockUseCase.RollbackResult("PROD-001", true, "Stock rolled back", 100));

        String request = """
            {
                "orderId": "ORD-001",
                "productId": "PROD-001",
                "quantity": 5
            }
            """;

        mockMvc.perform(post("/api/v1/inventory/rollback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.currentStock").value(100));
    }

    @Test
    @DisplayName("回滾庫存失敗應返回失敗結果")
    void rollbackStock_failure_shouldReturnFailure() throws Exception {
        when(rollbackStockUseCase.execute(any(RollbackStockCommand.class)))
                .thenReturn(new RollbackStockUseCase.RollbackResult("PROD-999", false, "Product not found", 0));

        String request = """
            {
                "orderId": "ORD-001",
                "productId": "PROD-999",
                "quantity": 5
            }
            """;

        mockMvc.perform(post("/api/v1/inventory/rollback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}
