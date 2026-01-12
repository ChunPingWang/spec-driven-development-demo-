package com.example.order.domain.model.aggregate;

import com.example.order.domain.event.*;
import com.example.order.domain.exception.OrderDomainException;
import com.example.order.domain.model.valueobject.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order Aggregate Root - manages order lifecycle and state transitions.
 */
public class Order {

    private OrderId orderId;
    private String idempotencyKey;
    private Buyer buyer;
    private OrderItem orderItem;
    private Money money;
    private PaymentInfo paymentInfo;
    private OrderStatus status;
    private String paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Private constructor for factory method
    private Order() {
    }

    /**
     * Factory method to create a new Order.
     */
    public static Order create(
            String idempotencyKey,
            Buyer buyer,
            OrderItem orderItem,
            Money money,
            PaymentInfo paymentInfo
    ) {
        Order order = new Order();
        order.orderId = OrderId.generate();
        order.idempotencyKey = idempotencyKey;
        order.buyer = buyer;
        order.orderItem = orderItem;
        order.money = money;
        order.paymentInfo = paymentInfo;
        order.status = OrderStatus.CREATED;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = order.createdAt;

        order.domainEvents.add(OrderCreated.of(order.orderId, buyer, orderItem));

        return order;
    }

    /**
     * Reconstitute an Order from persistence.
     */
    public static Order reconstitute(
            OrderId orderId,
            String idempotencyKey,
            Buyer buyer,
            OrderItem orderItem,
            Money money,
            PaymentInfo paymentInfo,
            OrderStatus status,
            String paymentId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Order order = new Order();
        order.orderId = orderId;
        order.idempotencyKey = idempotencyKey;
        order.buyer = buyer;
        order.orderItem = orderItem;
        order.money = money;
        order.paymentInfo = paymentInfo;
        order.status = status;
        order.paymentId = paymentId;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        return order;
    }

    /**
     * Mark payment as authorized.
     * Valid transition: CREATED -> PAYMENT_AUTHORIZED
     */
    public void markPaymentAuthorized(String paymentId) {
        validateStateTransition(OrderStatus.CREATED, OrderStatus.PAYMENT_AUTHORIZED);
        this.paymentId = paymentId;
        this.status = OrderStatus.PAYMENT_AUTHORIZED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark inventory as deducted.
     * Valid transition: PAYMENT_AUTHORIZED -> INVENTORY_DEDUCTED
     */
    public void markInventoryDeducted() {
        validateStateTransition(OrderStatus.PAYMENT_AUTHORIZED, OrderStatus.INVENTORY_DEDUCTED);
        this.status = OrderStatus.INVENTORY_DEDUCTED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Complete the order.
     * Valid transition: INVENTORY_DEDUCTED -> COMPLETED
     */
    public void complete() {
        validateStateTransition(OrderStatus.INVENTORY_DEDUCTED, OrderStatus.COMPLETED);
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
        this.domainEvents.add(OrderCompleted.of(this.orderId));
    }

    /**
     * Mark order as failed (early failure, no compensation needed).
     * Valid transition: CREATED -> FAILED
     */
    public void fail(String reason) {
        validateStateTransition(OrderStatus.CREATED, OrderStatus.FAILED);
        this.status = OrderStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
        this.domainEvents.add(OrderFailed.of(this.orderId, reason));
    }

    /**
     * Mark order as rolled back after compensation.
     * Valid transitions: PAYMENT_AUTHORIZED -> ROLLBACK_COMPLETED
     *                    INVENTORY_DEDUCTED -> ROLLBACK_COMPLETED
     */
    public void markRolledBack(String reason) {
        if (this.status != OrderStatus.PAYMENT_AUTHORIZED &&
            this.status != OrderStatus.INVENTORY_DEDUCTED) {
            throw OrderDomainException.invalidStateTransition(
                    this.status.name(), OrderStatus.ROLLBACK_COMPLETED.name());
        }
        this.status = OrderStatus.ROLLBACK_COMPLETED;
        this.updatedAt = LocalDateTime.now();
        this.domainEvents.add(OrderRolledBack.of(this.orderId, reason));
    }

    private void validateStateTransition(OrderStatus expectedCurrent, OrderStatus target) {
        if (this.status != expectedCurrent) {
            throw OrderDomainException.invalidStateTransition(this.status.name(), target.name());
        }
    }

    // Getters
    public OrderId getOrderId() {
        return orderId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public Money getMoney() {
        return money;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
