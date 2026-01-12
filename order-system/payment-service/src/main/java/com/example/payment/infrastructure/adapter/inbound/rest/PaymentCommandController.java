package com.example.payment.infrastructure.adapter.inbound.rest;

import com.example.payment.application.command.AuthorizePaymentCommand;
import com.example.payment.application.command.CapturePaymentCommand;
import com.example.payment.application.command.VoidPaymentCommand;
import com.example.payment.application.port.inbound.AuthorizePaymentUseCase;
import com.example.payment.application.port.inbound.CapturePaymentUseCase;
import com.example.payment.application.port.inbound.VoidPaymentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for payment commands.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentCommandController {

    private static final Logger log = LoggerFactory.getLogger(PaymentCommandController.class);

    private final AuthorizePaymentUseCase authorizePaymentUseCase;
    private final CapturePaymentUseCase capturePaymentUseCase;
    private final VoidPaymentUseCase voidPaymentUseCase;

    public PaymentCommandController(
            AuthorizePaymentUseCase authorizePaymentUseCase,
            CapturePaymentUseCase capturePaymentUseCase,
            VoidPaymentUseCase voidPaymentUseCase
    ) {
        this.authorizePaymentUseCase = authorizePaymentUseCase;
        this.capturePaymentUseCase = capturePaymentUseCase;
        this.voidPaymentUseCase = voidPaymentUseCase;
    }

    @PostMapping("/authorize")
    @Operation(summary = "Authorize a payment", description = "Authorizes payment for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authorization processed"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<AuthorizeResponse> authorize(@Valid @RequestBody AuthorizeRequest request) {
        log.info("Received authorize request for order: {}", request.orderId());

        AuthorizePaymentCommand command = new AuthorizePaymentCommand(
                request.orderId(),
                request.amount(),
                request.currency(),
                request.cardNumber(),
                request.expiryDate(),
                request.cvv()
        );

        AuthorizePaymentUseCase.AuthorizeResult result = authorizePaymentUseCase.execute(command);

        return ResponseEntity.ok(new AuthorizeResponse(
                result.paymentId(),
                result.authorized(),
                result.authorized() ? result.paymentId() : null, // authorizationCode stored with paymentId
                result.message()
        ));
    }

    @PostMapping("/capture")
    @Operation(summary = "Capture a payment", description = "Captures an authorized payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Capture processed"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<CaptureResponse> capture(@Valid @RequestBody CaptureRequest request) {
        log.info("Received capture request for payment: {}", request.paymentId());

        CapturePaymentCommand command = new CapturePaymentCommand(request.paymentId());
        CapturePaymentUseCase.CaptureResult result = capturePaymentUseCase.execute(command);

        return ResponseEntity.ok(new CaptureResponse(
                request.paymentId(),
                result.captured(),
                result.message()
        ));
    }

    @PostMapping("/void")
    @Operation(summary = "Void a payment", description = "Voids an authorized payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Void processed"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<VoidResponse> voidPayment(@Valid @RequestBody VoidRequest request) {
        log.info("Received void request for payment: {}", request.paymentId());

        VoidPaymentCommand command = new VoidPaymentCommand(request.paymentId());
        VoidPaymentUseCase.VoidResult result = voidPaymentUseCase.execute(command);

        return ResponseEntity.ok(new VoidResponse(
                request.paymentId(),
                result.voided(),
                result.message()
        ));
    }

    // Request/Response DTOs
    public record AuthorizeRequest(
            @NotBlank String orderId,
            @NotNull @Positive BigDecimal amount,
            @NotBlank String currency,
            @NotBlank String cardNumber,
            @NotBlank String expiryDate,
            @NotBlank String cvv
    ) {}

    public record AuthorizeResponse(
            String paymentId,
            boolean authorized,
            String authorizationCode,
            String message
    ) {}

    public record CaptureRequest(
            @NotBlank String paymentId
    ) {}

    public record CaptureResponse(
            String paymentId,
            boolean captured,
            String message
    ) {}

    public record VoidRequest(
            @NotBlank String paymentId
    ) {}

    public record VoidResponse(
            String paymentId,
            boolean voided,
            String message
    ) {}
}
