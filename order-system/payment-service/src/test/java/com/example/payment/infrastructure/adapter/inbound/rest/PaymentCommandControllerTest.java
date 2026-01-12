package com.example.payment.infrastructure.adapter.inbound.rest;

import com.example.payment.application.command.AuthorizePaymentCommand;
import com.example.payment.application.command.CapturePaymentCommand;
import com.example.payment.application.command.VoidPaymentCommand;
import com.example.payment.application.port.inbound.AuthorizePaymentUseCase;
import com.example.payment.application.port.inbound.CapturePaymentUseCase;
import com.example.payment.application.port.inbound.VoidPaymentUseCase;
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
@DisplayName("PaymentCommandController 測試")
class PaymentCommandControllerTest {

    @Mock
    private AuthorizePaymentUseCase authorizePaymentUseCase;

    @Mock
    private CapturePaymentUseCase capturePaymentUseCase;

    @Mock
    private VoidPaymentUseCase voidPaymentUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        PaymentCommandController controller = new PaymentCommandController(
                authorizePaymentUseCase, capturePaymentUseCase, voidPaymentUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("授權付款成功應返回 200")
    void authorize_success_shouldReturn200() throws Exception {
        when(authorizePaymentUseCase.execute(any(AuthorizePaymentCommand.class)))
                .thenReturn(new AuthorizePaymentUseCase.AuthorizeResult("PAY-001", true, "Authorized"));

        String request = """
            {
                "orderId": "ORD-001",
                "amount": 35900,
                "currency": "TWD",
                "cardNumber": "4111111111111111",
                "expiryDate": "12/26",
                "cvv": "123"
            }
            """;

        mockMvc.perform(post("/api/v1/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorized").value(true))
                .andExpect(jsonPath("$.paymentId").value("PAY-001"));
    }

    @Test
    @DisplayName("授權付款失敗應返回失敗結果")
    void authorize_failure_shouldReturnFailure() throws Exception {
        when(authorizePaymentUseCase.execute(any(AuthorizePaymentCommand.class)))
                .thenReturn(new AuthorizePaymentUseCase.AuthorizeResult(null, false, "Card declined"));

        String request = """
            {
                "orderId": "ORD-001",
                "amount": 35900,
                "currency": "TWD",
                "cardNumber": "4111111111111111",
                "expiryDate": "12/26",
                "cvv": "123"
            }
            """;

        mockMvc.perform(post("/api/v1/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorized").value(false))
                .andExpect(jsonPath("$.message").value("Card declined"));
    }

    @Test
    @DisplayName("請款成功應返回 200")
    void capture_success_shouldReturn200() throws Exception {
        when(capturePaymentUseCase.execute(any(CapturePaymentCommand.class)))
                .thenReturn(new CapturePaymentUseCase.CaptureResult("PAY-001", true, "Captured"));

        String request = """
            {
                "paymentId": "PAY-001"
            }
            """;

        mockMvc.perform(post("/api/v1/payments/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.captured").value(true));
    }

    @Test
    @DisplayName("請款失敗應返回失敗結果")
    void capture_failure_shouldReturnFailure() throws Exception {
        when(capturePaymentUseCase.execute(any(CapturePaymentCommand.class)))
                .thenReturn(new CapturePaymentUseCase.CaptureResult("PAY-001", false, "Capture failed"));

        String request = """
            {
                "paymentId": "PAY-001"
            }
            """;

        mockMvc.perform(post("/api/v1/payments/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.captured").value(false));
    }

    @Test
    @DisplayName("取消授權成功應返回 200")
    void voidPayment_success_shouldReturn200() throws Exception {
        when(voidPaymentUseCase.execute(any(VoidPaymentCommand.class)))
                .thenReturn(new VoidPaymentUseCase.VoidResult("PAY-001", true, "Voided"));

        String request = """
            {
                "paymentId": "PAY-001"
            }
            """;

        mockMvc.perform(post("/api/v1/payments/void")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voided").value(true));
    }

    @Test
    @DisplayName("取消授權失敗應返回失敗結果")
    void voidPayment_failure_shouldReturnFailure() throws Exception {
        when(voidPaymentUseCase.execute(any(VoidPaymentCommand.class)))
                .thenReturn(new VoidPaymentUseCase.VoidResult("PAY-001", false, "Void failed"));

        String request = """
            {
                "paymentId": "PAY-001"
            }
            """;

        mockMvc.perform(post("/api/v1/payments/void")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voided").value(false));
    }
}
