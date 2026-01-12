package com.example.order.application.saga;

import com.example.order.application.port.outbound.InventoryServicePort;
import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.application.port.outbound.PaymentServicePort;
import com.example.order.domain.model.aggregate.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SAGA Orchestrator for order creation.
 *
 * Flow:
 * 1. Authorize Payment
 * 2. Deduct Inventory
 * 3. Capture Payment
 *
 * Compensation:
 * - If inventory deduction fails: void payment
 * - If capture fails: rollback inventory, void payment
 */
@Component
public class CreateOrderSaga {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderSaga.class);

    private final OrderRepository orderRepository;
    private final PaymentServicePort paymentServicePort;
    private final InventoryServicePort inventoryServicePort;

    public CreateOrderSaga(
            OrderRepository orderRepository,
            PaymentServicePort paymentServicePort,
            InventoryServicePort inventoryServicePort
    ) {
        this.orderRepository = orderRepository;
        this.paymentServicePort = paymentServicePort;
        this.inventoryServicePort = inventoryServicePort;
    }

    /**
     * Execute the order creation SAGA.
     * @param order The order to process
     * @return The result of the SAGA execution
     */
    @Transactional
    public SagaResult execute(Order order) {
        log.info("Starting CreateOrderSaga for order: {}", order.getOrderId().value());

        // Step 1: Authorize Payment
        PaymentServicePort.AuthorizationResult authResult = authorizePayment(order);
        if (!authResult.success()) {
            log.warn("Payment authorization failed for order: {}", order.getOrderId().value());
            order.fail(authResult.message());
            orderRepository.save(order);
            return SagaResult.paymentFailed(authResult.message());
        }

        order.markPaymentAuthorized(authResult.paymentId());
        orderRepository.save(order);
        log.info("Payment authorized for order: {}, paymentId: {}",
                order.getOrderId().value(), authResult.paymentId());

        // Step 2: Deduct Inventory
        InventoryServicePort.DeductionResult deductResult = deductInventory(order);
        if (!deductResult.success()) {
            log.warn("Inventory deduction failed for order: {}, starting compensation",
                    order.getOrderId().value());

            // Compensation: Void payment
            compensatePayment(authResult.paymentId());
            order.markRolledBack("Inventory deduction failed: " + deductResult.message());
            orderRepository.save(order);
            return SagaResult.inventoryFailed(deductResult.message());
        }

        order.markInventoryDeducted();
        orderRepository.save(order);
        log.info("Inventory deducted for order: {}", order.getOrderId().value());

        // Step 3: Capture Payment
        PaymentServicePort.CaptureResult captureResult = capturePayment(authResult.paymentId());
        if (!captureResult.succeeded()) {
            log.warn("Payment capture failed for order: {}, starting full compensation",
                    order.getOrderId().value());

            // Full Compensation: Rollback inventory + void payment
            compensateInventory(order);
            compensatePayment(authResult.paymentId());
            order.markRolledBack("Payment capture failed: " + captureResult.message());
            orderRepository.save(order);
            return SagaResult.captureFailed(captureResult.message());
        }

        // Success: Complete the order
        order.complete();
        orderRepository.save(order);
        log.info("Order completed successfully: {}", order.getOrderId().value());

        return SagaResult.success();
    }

    private PaymentServicePort.AuthorizationResult authorizePayment(Order order) {
        return paymentServicePort.authorize(
                order.getOrderId().value(),
                order.getMoney().amount(),
                order.getMoney().currency(),
                order.getPaymentInfo().cardNumber(),
                order.getPaymentInfo().expiryDate(),
                order.getPaymentInfo().cvv()
        );
    }

    private InventoryServicePort.DeductionResult deductInventory(Order order) {
        return inventoryServicePort.deductStock(
                order.getOrderId().value(),
                order.getOrderItem().productId(),
                order.getOrderItem().quantity()
        );
    }

    private PaymentServicePort.CaptureResult capturePayment(String paymentId) {
        return paymentServicePort.capture(paymentId);
    }

    private void compensatePayment(String paymentId) {
        try {
            PaymentServicePort.VoidResult voidResult = paymentServicePort.voidPayment(paymentId);
            if (!voidResult.succeeded()) {
                log.error("Payment void compensation failed: {}", voidResult.message());
            } else {
                log.info("Payment voided successfully: {}", paymentId);
            }
        } catch (Exception e) {
            log.error("Payment void compensation error", e);
        }
    }

    private void compensateInventory(Order order) {
        try {
            InventoryServicePort.RollbackResult rollbackResult = inventoryServicePort.rollbackStock(
                    order.getOrderId().value(),
                    order.getOrderItem().productId(),
                    order.getOrderItem().quantity()
            );
            if (!rollbackResult.success()) {
                log.error("Inventory rollback compensation failed: {}", rollbackResult.message());
            } else {
                log.info("Inventory rolled back successfully for order: {}",
                        order.getOrderId().value());
            }
        } catch (Exception e) {
            log.error("Inventory rollback compensation error", e);
        }
    }

    /**
     * Result of the SAGA execution.
     */
    public record SagaResult(
            boolean succeeded,
            SagaStatus status,
            String message
    ) {
        public static SagaResult success() {
            return new SagaResult(true, SagaStatus.COMPLETED, "Order completed successfully");
        }

        public static SagaResult paymentFailed(String message) {
            return new SagaResult(false, SagaStatus.PAYMENT_FAILED, message);
        }

        public static SagaResult inventoryFailed(String message) {
            return new SagaResult(false, SagaStatus.INVENTORY_FAILED, message);
        }

        public static SagaResult captureFailed(String message) {
            return new SagaResult(false, SagaStatus.CAPTURE_FAILED, message);
        }
    }

    public enum SagaStatus {
        COMPLETED,
        PAYMENT_FAILED,
        INVENTORY_FAILED,
        CAPTURE_FAILED
    }
}
