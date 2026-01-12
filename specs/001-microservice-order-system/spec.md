# Feature Specification: Microservices Ordering System MVP

**Feature Branch**: `001-microservice-order-system`
**Created**: 2026-01-12
**Status**: Draft
**Input**: PRD.md - Microservices ordering system with DDD + Hexagonal Architecture + CQRS

## Clarifications

### Session 2026-01-12

- Q: What is the timeout threshold for inter-service calls? → A: 5 seconds (standard microservice timeout)
- Q: What protocol for inter-service communication? → A: REST/HTTP with JSON
- Q: What is the primary programming language? → A: Java with Spring Boot

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create Order (Priority: P1)

As an external system (B2B integration), I want to submit a purchase order with buyer information, product details, and payment data so that the order is processed through the complete purchase flow including payment authorization, inventory deduction, and payment capture.

**Why this priority**: This is the core business function - without order creation, the entire system has no purpose. It represents the complete happy path of the ordering workflow.

**Independent Test**: Can be fully tested by submitting a valid order request and verifying the order reaches COMPLETED status with all intermediate steps executed successfully.

**Acceptance Scenarios**:

1. **Given** an external system with valid credentials, **When** it submits a CreateOrderCommand with valid buyer info, product details, and payment data, **Then** the system returns an order ID with status COMPLETED and a success message.

2. **Given** a valid order request, **When** the payment authorization succeeds and inventory is available, **Then** the order progresses through CREATED → PAYMENT_AUTHORIZED → INVENTORY_DEDUCTED → COMPLETED states.

3. **Given** a valid order request, **When** processing completes successfully, **Then** the buyer receives confirmation with order ID, status, and timestamp.

---

### User Story 2 - Query Order Status (Priority: P2)

As an external system, I want to query the status and details of a previously submitted order so that I can track order progress and retrieve order information.

**Why this priority**: Order visibility is essential for B2B operations - clients need to verify order status and reconcile their records.

**Independent Test**: Can be tested by creating an order and then querying it by order ID to verify all details are returned correctly.

**Acceptance Scenarios**:

1. **Given** an existing order with ID "ORD-A1B2C3D4", **When** I send a GetOrderQuery with that order ID, **Then** I receive the complete order details including buyer, product, amount, status, and timestamps.

2. **Given** an order that has been processed, **When** I query its status, **Then** I see the current state (CREATED, PAYMENT_AUTHORIZED, INVENTORY_DEDUCTED, COMPLETED, FAILED, or ROLLBACK_COMPLETED).

---

### User Story 3 - Handle Payment Authorization Failure (Priority: P2)

As the system, I want to gracefully handle payment authorization failures so that orders are marked as failed and the external system receives clear feedback.

**Why this priority**: Payment failures are common in production. Proper handling ensures data consistency and clear communication.

**Independent Test**: Can be tested by submitting an order with payment data known to fail authorization (e.g., declined card) and verifying the order status becomes FAILED.

**Acceptance Scenarios**:

1. **Given** an order with invalid or declined payment information, **When** payment authorization fails, **Then** the order status becomes FAILED and the system responds with "支付失敗" (Payment Failed).

2. **Given** a payment authorization failure, **When** the failure is recorded, **Then** no inventory is deducted and no further processing occurs.

---

### User Story 4 - Handle Inventory Deduction Failure with Compensation (Priority: P2)

As the system, I want to automatically void payment authorization when inventory deduction fails so that no customer funds are held for unavailable products.

**Why this priority**: Compensation mechanisms are critical for maintaining data consistency across microservices.

**Independent Test**: Can be tested by submitting an order for a product with insufficient stock after payment authorization succeeds.

**Acceptance Scenarios**:

1. **Given** an authorized payment but insufficient inventory, **When** stock deduction fails, **Then** the system automatically voids the payment authorization.

2. **Given** an inventory deduction failure with compensation, **When** rollback completes, **Then** the order status becomes ROLLBACK_COMPLETED and the system responds with "庫存扣減失敗" (Inventory Deduction Failed).

---

### User Story 5 - Handle Capture Failure with Full Compensation (Priority: P3)

As the system, I want to roll back both inventory and payment when the final payment capture fails so that the system returns to a consistent state.

**Why this priority**: Capture failures after inventory deduction require multi-step compensation - more complex but less frequent.

**Independent Test**: Can be tested by simulating a capture failure after successful authorization and inventory deduction.

**Acceptance Scenarios**:

1. **Given** an order with INVENTORY_DEDUCTED status, **When** payment capture fails, **Then** the system rolls back inventory and voids payment authorization.

2. **Given** a capture failure scenario, **When** all compensations complete, **Then** the order status becomes ROLLBACK_COMPLETED with appropriate failure message.

---

### User Story 6 - Idempotent Order Processing (Priority: P3)

As an external system, I want to safely retry failed requests without creating duplicate orders so that network issues don't cause duplicate transactions.

**Why this priority**: Idempotency is essential for B2B reliability but can leverage unique identifiers for implementation.

**Independent Test**: Can be tested by submitting the same order request twice with the same idempotency key and verifying only one order is created.

**Acceptance Scenarios**:

1. **Given** an order request with idempotency key "abc-123", **When** the same request is submitted twice, **Then** only one order is created and both requests return the same order ID.

2. **Given** a duplicate request for an existing order, **When** the original order is in any state, **Then** the system returns the current state without re-processing.

---

### Edge Cases

- What happens when a service times out? System treats timeout as failure and executes compensation.
- What happens when payment is authorized but the order service crashes before recording? Idempotency key allows safe retry.
- What happens when compensation itself fails? System logs the failure for manual intervention (MVP does not auto-retry compensation).
- What happens with concurrent orders for the last item in stock? First successful deduction wins; others receive stock failure.

## Requirements *(mandatory)*

### Functional Requirements

**Order Management**
- **FR-001**: System MUST accept CreateOrderCommand with buyer info (name, email), order item (product ID, name, quantity), order amount (amount, currency), and payment info (method, card details). Note: Amount/currency are stored as Money value object; payment info (card details) is used for authorization only.
- **FR-002**: System MUST generate unique order IDs in format "ORD-{8-char-alphanumeric}" (e.g., "ORD-A1B2C3D4").
- **FR-003**: System MUST track order state transitions: CREATED → PAYMENT_AUTHORIZED → INVENTORY_DEDUCTED → COMPLETED.
- **FR-004**: System MUST support GetOrderQuery returning full order details and current status.

**Payment Processing**
- **FR-005**: System MUST implement two-phase payment: authorization (pre-capture) followed by capture (confirm charge).
- **FR-006**: System MUST support payment void operation to cancel authorized but uncaptured payments.
- **FR-007**: Payment service MUST track payment states: PENDING → AUTHORIZED → CAPTURED, with FAILED and VOIDED as terminal states.

**Inventory Management**
- **FR-008**: System MUST deduct inventory atomically for ordered products.
- **FR-009**: System MUST support inventory rollback for failed orders.
- **FR-010**: System MUST prevent overselling (no negative stock).

**Compensation & Consistency**
- **FR-011**: System MUST execute compensation in correct order: void payment before rollback inventory (when both needed).
- **FR-012**: System MUST support idempotency via X-Idempotency-Key header for order creation.
- **FR-013**: System MUST use order ID as idempotency key for payment and inventory operations.

**Error Handling**
- **FR-014**: System MUST return "支付失敗" for any payment-related failures.
- **FR-015**: System MUST return "庫存扣減失敗" for inventory-related failures.
- **FR-016**: System MUST treat service timeout (5 seconds for inter-service calls) as operation failure.

### Key Entities

- **Order**: Aggregate root containing order state, buyer info, order items, and payment reference. Manages order lifecycle and state transitions.
- **Buyer**: Value object with name and email identifying the purchaser.
- **OrderItem**: Value object containing product reference (ID, name) and quantity.
- **Money**: Value object with amount and currency (e.g., 35000 TWD).
- **Payment**: Aggregate root managing payment lifecycle with authorization codes and transaction records.
- **Product**: Aggregate root in inventory context managing stock levels and deduction/rollback operations.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: External systems can complete a full order (create → confirm) in under 10 seconds under normal conditions.
- **SC-002**: Order queries return results in under 1 second.
- **SC-003**: 100% of failed orders execute appropriate compensation without manual intervention.
- **SC-004**: Duplicate requests with same idempotency key return consistent results 100% of the time.
- **SC-005**: System maintains data consistency - no orphaned authorizations or phantom inventory deductions after order completion or failure.
- **SC-006**: All order state transitions are logged with timestamps for audit purposes.
- **SC-007**: System handles service unavailability gracefully with clear error messages within 30 seconds.

## Assumptions

- External systems authenticate via a mechanism to be defined post-MVP (authentication/authorization deferred).
- Credit card processing integrates with an external acquirer (payment gateway) - mock implementation acceptable for MVP.
- Single currency (TWD) for MVP; multi-currency support deferred.
- Single product per order for MVP simplicity; multi-item orders can be added later.
- Products and initial inventory exist in the system (seeded data for MVP).
- No API Gateway for MVP; direct service communication acceptable.
- Synchronous orchestration (not event-driven choreography) for SAGA pattern.
- Inter-service communication uses REST/HTTP with JSON payloads.
- Implementation uses Java with Spring Boot framework.
