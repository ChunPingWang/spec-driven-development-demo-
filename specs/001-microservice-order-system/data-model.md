# Data Model: Microservices Ordering System MVP

**Feature**: 001-microservice-order-system
**Date**: 2026-01-12
**Status**: Complete

## Overview

This document defines the domain entities, value objects, and their relationships across the three bounded contexts: Order, Payment, and Inventory.

---

## 1. Order Context

### 1.1 Order (Aggregate Root)

The central aggregate managing the order lifecycle.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| orderId | OrderId | Required, Unique | Generated ID: "ORD-{UUID-8chars}" |
| idempotencyKey | String | Required, Unique | Client-provided deduplication key |
| buyer | Buyer | Required | Purchaser information |
| orderItem | OrderItem | Required | Product being ordered |
| money | Money | Required | Order amount with currency |
| paymentInfo | PaymentInfo | Required | Payment method details |
| status | OrderStatus | Required | Current order state |
| paymentId | String | Optional | Reference to Payment aggregate |
| createdAt | LocalDateTime | Required | Order creation timestamp |
| updatedAt | LocalDateTime | Required | Last modification timestamp |
| domainEvents | List<DomainEvent> | Transient | Uncommitted domain events |

### 1.2 Value Objects

#### OrderId
```
record OrderId(String value)
- Validation: Not null, not blank
- Format: "ORD-" + 8 uppercase alphanumeric characters
- Factory: OrderId.generate() creates new ID
```

#### Buyer
```
record Buyer(String name, String email)
- name: Required, not blank
- email: Required, must contain "@"
```

#### OrderItem
```
record OrderItem(String productId, String productName, int quantity)
- productId: Required, not blank
- productName: Required
- quantity: Must be positive (> 0)
```

#### Money
```
record Money(BigDecimal amount, String currency)
- amount: Required, non-negative (>= 0)
- currency: Required, 3-letter ISO code (e.g., "TWD")
```

#### PaymentInfo
```
record PaymentInfo(String method, String cardNumber, String expiryDate, String cvv)
- method: Payment method (e.g., "CREDIT_CARD")
- cardNumber: Full card number (not stored in DB)
- expiryDate: Format "MM/YY"
- cvv: 3-4 digit security code
```

#### OrderStatus (Enum)
```
enum OrderStatus {
    CREATED,              // Initial state
    PAYMENT_AUTHORIZED,   // Payment pre-auth successful
    INVENTORY_DEDUCTED,   // Stock reserved
    COMPLETED,            // Payment captured, order done
    FAILED,               // Early failure (no compensation needed)
    ROLLBACK_COMPLETED    // Compensation executed
}
```

### 1.3 State Machine

```
                    ┌─────────────────────────────────────────┐
                    │                                         │
                    ▼                                         │
┌─────────┐    ┌─────────────────────┐    ┌────────────────────────────┐
│ CREATED │───▶│ PAYMENT_AUTHORIZED  │───▶│    INVENTORY_DEDUCTED      │
└─────────┘    └─────────────────────┘    └────────────────────────────┘
     │                   │                            │
     │                   │                            │
     ▼                   ▼                            ▼
┌─────────┐    ┌────────────────────┐         ┌───────────┐
│ FAILED  │◀───│ ROLLBACK_COMPLETED │◀────────│ COMPLETED │
└─────────┘    └────────────────────┘         └───────────┘
```

**Valid Transitions:**
- CREATED → PAYMENT_AUTHORIZED (payment auth success)
- CREATED → FAILED (payment auth failed)
- PAYMENT_AUTHORIZED → INVENTORY_DEDUCTED (stock deducted)
- PAYMENT_AUTHORIZED → ROLLBACK_COMPLETED (inventory failed, payment voided)
- INVENTORY_DEDUCTED → COMPLETED (payment captured)
- INVENTORY_DEDUCTED → ROLLBACK_COMPLETED (capture failed, compensated)

### 1.4 Domain Events

| Event | Trigger | Payload |
|-------|---------|---------|
| OrderCreated | Order.create() | orderId, buyer, orderItem |
| PaymentAuthorizedEvent | markPaymentAuthorized() | orderId, paymentId |
| InventoryDeductedEvent | markInventoryDeducted() | orderId |
| OrderCompleted | complete() | orderId |
| OrderFailed | fail() | orderId |
| OrderRolledBack | markRolledBack() | orderId |

---

## 2. Payment Context

### 2.1 Payment (Aggregate Root)

Manages payment lifecycle with two-phase commit (authorize → capture).

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| paymentId | PaymentId | Required, Unique | Generated payment ID |
| orderId | String | Required, Unique | One payment per order |
| money | Money | Required | Payment amount |
| cardInfo | CardInfo | Required | Masked card details |
| status | PaymentStatus | Required | Current payment state |
| authorizationCode | String | Optional | From acquirer on auth |
| createdAt | LocalDateTime | Required | Payment creation |
| updatedAt | LocalDateTime | Required | Last modification |

### 2.2 Value Objects

#### PaymentId
```
record PaymentId(String value)
- Format: "PAY-" + UUID
```

#### CardInfo
```
record CardInfo(String lastFour, String expiryDate)
- lastFour: Last 4 digits of card (masked storage)
- expiryDate: "MM/YY" format
```

#### AuthorizationCode
```
record AuthorizationCode(String value)
- Value from acquirer response
```

#### PaymentStatus (Enum)
```
enum PaymentStatus {
    PENDING,      // Initial state
    AUTHORIZED,   // Pre-auth successful
    CAPTURED,     // Payment confirmed
    FAILED,       // Authorization failed
    VOIDED        // Authorization cancelled
}
```

### 2.3 State Machine

```
┌──────────┐    ┌────────────┐    ┌──────────┐
│ PENDING  │───▶│ AUTHORIZED │───▶│ CAPTURED │
└──────────┘    └────────────┘    └──────────┘
     │                │
     │                │
     ▼                ▼
┌──────────┐    ┌──────────┐
│  FAILED  │    │  VOIDED  │
└──────────┘    └──────────┘
```

**Valid Transitions:**
- PENDING → AUTHORIZED (auth success)
- PENDING → FAILED (auth failed)
- AUTHORIZED → CAPTURED (capture success)
- AUTHORIZED → VOIDED (cancel authorization)

### 2.4 Domain Events

| Event | Trigger | Payload |
|-------|---------|---------|
| PaymentAuthorized | authorize() success | paymentId, authCode |
| PaymentAuthorizationFailed | authorize() failed | paymentId, reason |
| PaymentCaptured | capture() success | paymentId |
| PaymentCaptureFailed | capture() failed | paymentId, reason |
| PaymentVoided | void() | paymentId |

---

## 3. Inventory Context

### 3.1 Product (Aggregate Root)

Manages product stock levels.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| productId | ProductId | Required, Unique | Product identifier |
| productName | String | Required | Display name |
| stockQuantity | StockQuantity | Required | Current stock level |
| createdAt | LocalDateTime | Required | Product creation |
| updatedAt | LocalDateTime | Required | Last modification |

### 3.2 Value Objects

#### ProductId
```
record ProductId(String value)
- Example: "IPHONE-17-PRO-MAX"
```

#### StockQuantity
```
record StockQuantity(int value)
- Validation: value >= 0 (no negative stock)
- Methods: deduct(int), add(int)
```

#### InventoryOperation (Enum)
```
enum InventoryOperation {
    DEDUCT,    // Reduce stock
    ROLLBACK   // Restore stock
}
```

### 3.3 Inventory Log

Tracks all inventory operations for idempotency and audit.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | Auto-generated | Primary key |
| orderId | String | Required | Reference to order |
| productId | String | Required | Reference to product |
| operationType | InventoryOperation | Required | DEDUCT or ROLLBACK |
| quantity | int | Required | Amount changed |
| status | String | Required | SUCCESS or FAILED |
| createdAt | LocalDateTime | Required | Operation timestamp |

**Unique Constraint**: (orderId, productId, operationType) - ensures one operation per order per type

### 3.4 Domain Events

| Event | Trigger | Payload |
|-------|---------|---------|
| StockDeducted | deduct() success | productId, orderId, quantity |
| StockDeductionFailed | deduct() failed | productId, orderId, reason |
| StockRolledBack | rollback() | productId, orderId, quantity |

---

## 4. Database Schema

### 4.1 Order Schema (order_schema)

```sql
CREATE TABLE order_schema.orders (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) UNIQUE NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    buyer_name VARCHAR(100) NOT NULL,
    buyer_email VARCHAR(255) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_method VARCHAR(20),
    card_last_four VARCHAR(4),
    status VARCHAR(30) NOT NULL,
    payment_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_orders_status ON order_schema.orders(status);
CREATE INDEX idx_orders_created_at ON order_schema.orders(created_at);
```

### 4.2 Payment Schema (payment_schema)

```sql
CREATE TABLE payment_schema.payments (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) UNIQUE NOT NULL,
    order_id VARCHAR(50) UNIQUE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    card_last_four VARCHAR(4),
    status VARCHAR(30) NOT NULL,
    authorization_code VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE payment_schema.payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL REFERENCES payment_schema.payments(payment_id),
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_transactions_payment ON payment_schema.payment_transactions(payment_id);
```

### 4.3 Inventory Schema (inventory_schema)

```sql
CREATE TABLE inventory_schema.products (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(50) UNIQUE NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE inventory_schema.inventory_logs (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (order_id, product_id, operation_type)
);

CREATE INDEX idx_inventory_logs_product ON inventory_schema.inventory_logs(product_id);
```

---

## 5. Entity Relationships

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Order System                                 │
│                                                                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│  │  Order Context  │  │ Payment Context │  │Inventory Context│     │
│  │                 │  │                 │  │                 │     │
│  │  Order ─────────┼──┼─▶ Payment       │  │  Product        │     │
│  │    │            │  │     (by orderId)│  │    │            │     │
│  │    ├─ Buyer     │  │     │           │  │    └─ StockQty  │     │
│  │    ├─ OrderItem─┼──┼─────┼───────────┼──┼─▶ (by productId)│     │
│  │    ├─ Money     │  │     ├─ CardInfo │  │                 │     │
│  │    └─ Status    │  │     └─ Status   │  │  InventoryLog   │     │
│  │                 │  │                 │  │    (by orderId) │     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘     │
│                                                                      │
│  Cross-Context References (by ID only, no direct object refs):      │
│  • Order.paymentId → Payment.paymentId                              │
│  • Order.orderItem.productId → Product.productId                    │
│  • Payment.orderId → Order.orderId                                  │
│  • InventoryLog.orderId → Order.orderId                             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6. Validation Rules Summary

| Entity | Rule | Error |
|--------|------|-------|
| Order | idempotencyKey must be unique | Duplicate order request |
| Order | quantity > 0 | Invalid quantity |
| Order | amount >= 0 | Invalid amount |
| Order | Valid state transition | IllegalStateException |
| Payment | orderId must be unique | One payment per order |
| Payment | Valid state transition | IllegalStateException |
| Product | stockQuantity >= 0 | Negative stock not allowed |
| InventoryLog | (orderId, productId, op) unique | Idempotent operation |
