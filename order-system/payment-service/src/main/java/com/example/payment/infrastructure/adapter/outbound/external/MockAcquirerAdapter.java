package com.example.payment.infrastructure.adapter.outbound.external;

import com.example.payment.application.port.outbound.AcquirerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock implementation of the payment acquirer for development/testing.
 *
 * Simulates acquirer behavior:
 * - Cards starting with "4111" are approved
 * - Cards starting with "4000" are declined (insufficient funds)
 * - Cards starting with "5000" trigger capture failure
 * - All other cards are approved
 */
@Component
public class MockAcquirerAdapter implements AcquirerPort {

    private static final Logger log = LoggerFactory.getLogger(MockAcquirerAdapter.class);

    private static final String DECLINED_CARD_PREFIX = "4000";
    private static final String CAPTURE_FAIL_CARD_PREFIX = "5000";

    @Override
    public AuthorizationResponse authorize(
            BigDecimal amount,
            String currency,
            String cardNumber,
            String expiryDate,
            String cvv
    ) {
        log.info("Mock acquirer: authorizing payment amount={} {} card={}****",
                amount, currency, cardNumber.substring(0, 4));

        // Simulate network delay
        simulateNetworkDelay();

        // Check for declined card
        if (cardNumber.startsWith(DECLINED_CARD_PREFIX)) {
            log.info("Mock acquirer: card declined (insufficient funds)");
            return AuthorizationResponse.declined("Insufficient funds");
        }

        // Generate authorization code
        String authCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Mock acquirer: authorization approved, code={}", authCode);

        return AuthorizationResponse.approved(authCode);
    }

    @Override
    public CaptureResponse capture(String authorizationCode, BigDecimal amount) {
        log.info("Mock acquirer: capturing payment authCode={} amount={}",
                authorizationCode, amount);

        // Simulate network delay
        simulateNetworkDelay();

        // Simulate capture failure for specific authorization codes
        // In real implementation, this would be based on card characteristics
        // For testing, we'll use a flag stored with the authorization
        if (authorizationCode.contains("FAIL")) {
            log.info("Mock acquirer: capture failed");
            return CaptureResponse.failure("Capture failed - acquirer error");
        }

        log.info("Mock acquirer: capture successful");
        return CaptureResponse.success();
    }

    @Override
    public VoidResponse voidAuthorization(String authorizationCode) {
        log.info("Mock acquirer: voiding authorization code={}", authorizationCode);

        // Simulate network delay
        simulateNetworkDelay();

        log.info("Mock acquirer: void successful");
        return VoidResponse.success();
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(50); // 50ms simulated latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
