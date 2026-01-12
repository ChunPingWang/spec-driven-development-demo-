package com.example.order.infrastructure.adapter.outbound.external;

import com.example.order.application.port.outbound.PaymentServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * HTTP adapter for payment service communication.
 */
@Component
public class PaymentServiceAdapter implements PaymentServicePort {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceAdapter.class);

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;

    public PaymentServiceAdapter(
            RestTemplate restTemplate,
            @Value("${services.payment.url}") String paymentServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }

    @Override
    public AuthorizationResult authorize(
            String orderId,
            BigDecimal amount,
            String currency,
            String cardNumber,
            String expiryDate,
            String cvv
    ) {
        log.info("Calling payment service to authorize payment for order: {}", orderId);

        try {
            Map<String, Object> request = Map.of(
                    "orderId", orderId,
                    "amount", amount,
                    "currency", currency,
                    "cardNumber", cardNumber,
                    "expiryDate", expiryDate,
                    "cvv", cvv
            );

            ResponseEntity<AuthorizeResponse> response = restTemplate.postForEntity(
                    paymentServiceUrl + "/api/v1/payments/authorize",
                    request,
                    AuthorizeResponse.class
            );

            AuthorizeResponse body = response.getBody();
            if (body == null) {
                return AuthorizationResult.failure("Empty response from payment service");
            }

            if (body.authorized()) {
                return AuthorizationResult.success(body.paymentId(), body.authorizationCode());
            } else {
                return AuthorizationResult.failure(body.message());
            }
        } catch (RestClientException e) {
            log.error("Payment service authorization failed", e);
            return AuthorizationResult.failure("Payment service unavailable: " + e.getMessage());
        }
    }

    @Override
    public CaptureResult capture(String paymentId) {
        log.info("Calling payment service to capture payment: {}", paymentId);

        try {
            Map<String, Object> request = Map.of("paymentId", paymentId);

            ResponseEntity<CaptureResponse> response = restTemplate.postForEntity(
                    paymentServiceUrl + "/api/v1/payments/capture",
                    request,
                    CaptureResponse.class
            );

            CaptureResponse body = response.getBody();
            if (body == null) {
                return CaptureResult.failure("Empty response from payment service");
            }

            if (body.captured()) {
                return CaptureResult.success();
            } else {
                return CaptureResult.failure(body.message());
            }
        } catch (RestClientException e) {
            log.error("Payment service capture failed", e);
            return CaptureResult.failure("Payment service unavailable: " + e.getMessage());
        }
    }

    @Override
    public VoidResult voidPayment(String paymentId) {
        log.info("Calling payment service to void payment: {}", paymentId);

        try {
            Map<String, Object> request = Map.of("paymentId", paymentId);

            ResponseEntity<VoidResponse> response = restTemplate.postForEntity(
                    paymentServiceUrl + "/api/v1/payments/void",
                    request,
                    VoidResponse.class
            );

            VoidResponse body = response.getBody();
            if (body == null) {
                return VoidResult.failure("Empty response from payment service");
            }

            if (body.voided()) {
                return VoidResult.success();
            } else {
                return VoidResult.failure(body.message());
            }
        } catch (RestClientException e) {
            log.error("Payment service void failed", e);
            return VoidResult.failure("Payment service unavailable: " + e.getMessage());
        }
    }

    // Response DTOs for payment service
    private record AuthorizeResponse(
            String paymentId,
            boolean authorized,
            String authorizationCode,
            String message
    ) {}

    private record CaptureResponse(
            String paymentId,
            boolean captured,
            String message
    ) {}

    private record VoidResponse(
            String paymentId,
            boolean voided,
            String message
    ) {}
}
