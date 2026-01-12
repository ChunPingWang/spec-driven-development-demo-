# Tasks: Microservices Ordering System MVP

**Input**: Design documents from `/specs/001-microservice-order-system/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/

**Tests**: TDD is required per constitution. Tests MUST be written FIRST and FAIL before implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

```
order-system/
├── order-service/src/main/java/com/example/order/
├── payment-service/src/main/java/com/example/payment/
├── inventory-service/src/main/java/com/example/inventory/
└── e2e-tests/src/test/
```

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Gradle multi-module project initialization with Spring Boot 3.x

- [x] T001 Create root project structure with build.gradle, settings.gradle, gradle.properties in order-system/
- [x] T002 [P] Create order-service module with build.gradle in order-system/order-service/
- [x] T003 [P] Create payment-service module with build.gradle in order-system/payment-service/
- [x] T004 [P] Create inventory-service module with build.gradle in order-system/inventory-service/
- [x] T005 [P] Create e2e-tests module with build.gradle and Cucumber dependencies in order-system/e2e-tests/
- [x] T006 [P] Configure Spring Boot application.yml for order-service in order-system/order-service/src/main/resources/application.yml
- [x] T007 [P] Configure Spring Boot application.yml for payment-service in order-system/payment-service/src/main/resources/application.yml
- [x] T008 [P] Configure Spring Boot application.yml for inventory-service in order-system/inventory-service/src/main/resources/application.yml
- [x] T009 [P] Configure OpenAPI/Swagger for all services in respective config/ packages

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain models and infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Order Service Domain Layer

- [x] T010 [P] Create OrderId value object in order-service/.../domain/model/valueobject/OrderId.java
- [x] T011 [P] Create Buyer value object in order-service/.../domain/model/valueobject/Buyer.java
- [x] T012 [P] Create OrderItem value object in order-service/.../domain/model/valueobject/OrderItem.java
- [x] T013 [P] Create Money value object in order-service/.../domain/model/valueobject/Money.java
- [x] T014 [P] Create PaymentInfo value object in order-service/.../domain/model/valueobject/PaymentInfo.java
- [x] T015 [P] Create OrderStatus enum in order-service/.../domain/model/valueobject/OrderStatus.java
- [x] T016 Create Order aggregate root with state machine in order-service/.../domain/model/aggregate/Order.java
- [x] T017 [P] Create domain events (OrderCreated, OrderCompleted, OrderFailed, OrderRolledBack) in order-service/.../domain/event/
- [x] T018 Create OrderDomainException in order-service/.../domain/exception/OrderDomainException.java

### Payment Service Domain Layer

- [x] T019 [P] Create PaymentId value object in payment-service/.../domain/model/valueobject/PaymentId.java
- [x] T020 [P] Create CardInfo value object in payment-service/.../domain/model/valueobject/CardInfo.java
- [x] T021 [P] Create PaymentStatus enum in payment-service/.../domain/model/valueobject/PaymentStatus.java
- [x] T022 Create Payment aggregate root with state machine in payment-service/.../domain/model/aggregate/Payment.java
- [x] T023 [P] Create domain events (PaymentAuthorized, PaymentCaptured, PaymentVoided) in payment-service/.../domain/event/

### Inventory Service Domain Layer

- [x] T024 [P] Create ProductId value object in inventory-service/.../domain/model/valueobject/ProductId.java
- [x] T025 [P] Create StockQuantity value object in inventory-service/.../domain/model/valueobject/StockQuantity.java
- [x] T026 [P] Create InventoryOperation enum in inventory-service/.../domain/model/valueobject/InventoryOperation.java
- [x] T027 Create Product aggregate root in inventory-service/.../domain/model/aggregate/Product.java
- [x] T028 [P] Create domain events (StockDeducted, StockRolledBack) in inventory-service/.../domain/event/
- [x] T029 Create InsufficientStockException in inventory-service/.../domain/exception/InsufficientStockException.java

### Application Layer Ports (Interfaces)

- [x] T030 [P] Create OrderRepository port in order-service/.../application/port/outbound/OrderRepository.java
- [x] T031 [P] Create PaymentServicePort in order-service/.../application/port/outbound/PaymentServicePort.java
- [x] T032 [P] Create InventoryServicePort in order-service/.../application/port/outbound/InventoryServicePort.java
- [x] T033 [P] Create PaymentRepository port in payment-service/.../application/port/outbound/PaymentRepository.java
- [x] T034 [P] Create AcquirerPort in payment-service/.../application/port/outbound/AcquirerPort.java
- [x] T035 [P] Create ProductRepository port in inventory-service/.../application/port/outbound/ProductRepository.java
- [x] T036 [P] Create InventoryLogRepository port in inventory-service/.../application/port/outbound/InventoryLogRepository.java

### Infrastructure Layer - Persistence

- [x] T037 Create schema.sql for order_schema in order-service/src/main/resources/schema.sql
- [x] T038 [P] Create schema.sql for payment_schema in payment-service/src/main/resources/schema.sql
- [x] T039 [P] Create schema.sql for inventory_schema in inventory-service/src/main/resources/schema.sql
- [x] T040 Create OrderJpaEntity in order-service/.../infrastructure/adapter/outbound/persistence/OrderJpaEntity.java
- [x] T041 Create OrderMapper in order-service/.../infrastructure/adapter/outbound/persistence/OrderMapper.java
- [x] T042 Create JpaOrderRepository implementing OrderRepository in order-service/.../infrastructure/adapter/outbound/persistence/JpaOrderRepository.java
- [x] T043 [P] Create PaymentJpaEntity in payment-service/.../infrastructure/adapter/outbound/persistence/PaymentJpaEntity.java
- [x] T044 [P] Create PaymentMapper in payment-service/.../infrastructure/adapter/outbound/persistence/PaymentMapper.java
- [x] T045 [P] Create JpaPaymentRepository in payment-service/.../infrastructure/adapter/outbound/persistence/JpaPaymentRepository.java
- [x] T046 [P] Create ProductJpaEntity in inventory-service/.../infrastructure/adapter/outbound/persistence/ProductJpaEntity.java
- [x] T047 [P] Create InventoryLogJpaEntity in inventory-service/.../infrastructure/adapter/outbound/persistence/InventoryLogJpaEntity.java
- [x] T048 [P] Create JpaProductRepository in inventory-service/.../infrastructure/adapter/outbound/persistence/JpaProductRepository.java

### Seed Data

- [x] T049 Create data.sql with sample product (iPhone 17 Pro Max) in inventory-service/src/main/resources/data.sql

**Checkpoint**: Foundation ready - all domain models, ports, and persistence layers complete

---

## Phase 3: User Story 1 - Create Order (Priority: P1) 🎯 MVP

**Goal**: Complete order creation with SAGA orchestration (happy path)

**Independent Test**: Submit valid order request → verify COMPLETED status

### Tests for User Story 1 (TDD - Write First, Must Fail)

- [ ] T050 [P] [US1] Unit test for Order aggregate in order-service/src/test/java/.../domain/model/aggregate/OrderTest.java
- [ ] T051 [P] [US1] Unit test for Payment aggregate in payment-service/src/test/java/.../domain/model/aggregate/PaymentTest.java
- [ ] T052 [P] [US1] Unit test for Product aggregate in inventory-service/src/test/java/.../domain/model/aggregate/ProductTest.java
- [ ] T053 [P] [US1] Unit test for CreateOrderCommandHandler in order-service/src/test/java/.../application/command/CreateOrderCommandHandlerTest.java
- [ ] T054 [P] [US1] Contract test for POST /api/v1/orders in order-service/src/contractTest/resources/contracts/createOrder.groovy
- [ ] T055 [P] [US1] Contract test for POST /api/v1/payments/authorize in payment-service/src/contractTest/resources/contracts/authorizePayment.groovy
- [ ] T056 [P] [US1] Contract test for POST /api/v1/inventory/deduct in inventory-service/src/contractTest/resources/contracts/deductStock.groovy
- [ ] T057 [US1] Cucumber feature for successful order creation in e2e-tests/src/test/resources/features/create-order-success.feature

### Implementation for User Story 1

- [x] T058 [US1] Create CreateOrderCommand in order-service/.../application/command/CreateOrderCommand.java
- [x] T059 [US1] Create CreateOrderUseCase port in order-service/.../application/port/inbound/CreateOrderUseCase.java
- [x] T060 [US1] Create DTOs (CreateOrderRequest, CreateOrderResponse, PaymentRequest, PaymentResponse, InventoryRequest, InventoryResponse) in order-service/.../application/dto/
- [x] T061 [US1] Create AuthorizePaymentUseCase port in payment-service/.../application/port/inbound/AuthorizePaymentUseCase.java
- [x] T062 [US1] Create CapturePaymentUseCase port in payment-service/.../application/port/inbound/CapturePaymentUseCase.java
- [x] T063 [US1] Create AuthorizePaymentCommandHandler in payment-service/.../application/command/AuthorizePaymentCommandHandler.java
- [x] T064 [US1] Create CapturePaymentCommandHandler in payment-service/.../application/command/CapturePaymentCommandHandler.java
- [x] T065 [US1] Create DeductStockUseCase port in inventory-service/.../application/port/inbound/DeductStockUseCase.java
- [x] T066 [US1] Create DeductStockCommandHandler in inventory-service/.../application/command/DeductStockCommandHandler.java
- [x] T067 [US1] Create CreateOrderSaga orchestrator in order-service/.../application/saga/CreateOrderSaga.java
- [x] T068 [US1] Create CreateOrderCommandHandler implementing CreateOrderUseCase in order-service/.../application/command/CreateOrderCommandHandler.java
- [x] T069 [US1] Create MockAcquirerAdapter in payment-service/.../infrastructure/adapter/outbound/external/MockAcquirerAdapter.java
- [x] T070 [US1] Create PaymentServiceAdapter in order-service/.../infrastructure/adapter/outbound/external/PaymentServiceAdapter.java
- [x] T071 [US1] Create InventoryServiceAdapter in order-service/.../infrastructure/adapter/outbound/external/InventoryServiceAdapter.java
- [x] T072 [US1] Create OrderCommandController with POST /api/v1/orders in order-service/.../infrastructure/adapter/inbound/rest/OrderCommandController.java
- [x] T073 [US1] Create PaymentCommandController with POST /authorize and /capture in payment-service/.../infrastructure/adapter/inbound/rest/PaymentCommandController.java
- [x] T074 [US1] Create InventoryCommandController with POST /deduct in inventory-service/.../infrastructure/adapter/inbound/rest/InventoryCommandController.java
- [x] T075 [US1] Configure RestTemplate with 5s timeout in order-service/.../infrastructure/config/BeanConfiguration.java
- [ ] T076 [US1] Create Cucumber step definitions for order creation in e2e-tests/src/test/java/.../steps/CreateOrderSteps.java

**Checkpoint**: User Story 1 complete - can create orders through full SAGA flow

---

## Phase 4: User Story 2 - Query Order Status (Priority: P2)

**Goal**: Retrieve order details by order ID

**Independent Test**: Create order → query by ID → verify all details returned

### Tests for User Story 2 (TDD)

- [ ] T077 [P] [US2] Unit test for GetOrderQueryHandler in order-service/src/test/java/.../application/query/GetOrderQueryHandlerTest.java
- [ ] T078 [P] [US2] Contract test for GET /api/v1/orders/{orderId} in order-service/src/contractTest/resources/contracts/getOrder.groovy

### Implementation for User Story 2

- [x] T079 [US2] Create GetOrderQuery in order-service/.../application/query/GetOrderQuery.java
- [x] T080 [US2] Create OrderReadModel in order-service/.../application/query/OrderReadModel.java
- [x] T081 [US2] Create GetOrderUseCase port in order-service/.../application/port/inbound/GetOrderUseCase.java
- [x] T082 [US2] Create GetOrderQueryHandler in order-service/.../application/query/GetOrderQueryHandler.java
- [x] T083 [US2] Create OrderQueryController with GET /api/v1/orders/{orderId} in order-service/.../infrastructure/adapter/inbound/rest/OrderQueryController.java
- [x] T084 [US2] Create OrderNotFoundException in order-service/.../application/exception/OrderNotFoundException.java

**Checkpoint**: User Story 2 complete - can query order status independently

---

## Phase 5: User Story 3 - Handle Payment Authorization Failure (Priority: P2)

**Goal**: Order marked FAILED when payment auth fails, no inventory deducted

**Independent Test**: Submit order with declined card → verify FAILED status

### Tests for User Story 3 (TDD)

- [ ] T085 [P] [US3] Unit test for payment auth failure flow in order-service/src/test/java/.../application/saga/CreateOrderSagaPaymentFailureTest.java
- [ ] T086 [US3] Cucumber feature for payment failure in e2e-tests/src/test/resources/features/order-payment-failure.feature

### Implementation for User Story 3

- [x] T087 [US3] Add declined card handling to MockAcquirerAdapter in payment-service/.../infrastructure/adapter/outbound/external/MockAcquirerAdapter.java
- [x] T088 [US3] Add payment failure handling to CreateOrderSaga in order-service/.../application/saga/CreateOrderSaga.java
- [x] T089 [US3] Add "支付失敗" error response handling in order-service/.../application/command/CreateOrderCommandHandler.java
- [ ] T090 [US3] Create Cucumber step definitions for payment failure in e2e-tests/src/test/java/.../steps/PaymentFailureSteps.java

**Checkpoint**: User Story 3 complete - payment failures handled correctly

---

## Phase 6: User Story 4 - Handle Inventory Deduction Failure with Compensation (Priority: P2)

**Goal**: Void payment when inventory deduction fails

**Independent Test**: Order with insufficient stock → verify payment voided and ROLLBACK_COMPLETED

### Tests for User Story 4 (TDD)

- [ ] T091 [P] [US4] Unit test for inventory failure compensation in order-service/src/test/java/.../application/saga/CreateOrderSagaInventoryFailureTest.java
- [ ] T092 [US4] Cucumber feature for inventory failure in e2e-tests/src/test/resources/features/order-inventory-failure.feature

### Implementation for User Story 4

- [x] T093 [US4] Create VoidPaymentUseCase port in payment-service/.../application/port/inbound/VoidPaymentUseCase.java
- [x] T094 [US4] Create VoidPaymentCommandHandler in payment-service/.../application/command/VoidPaymentCommandHandler.java
- [x] T095 [US4] Add POST /void endpoint to PaymentCommandController in payment-service/.../infrastructure/adapter/inbound/rest/PaymentCommandController.java
- [x] T096 [US4] Add inventory failure compensation to CreateOrderSaga (void payment) in order-service/.../application/saga/CreateOrderSaga.java
- [x] T097 [US4] Add "庫存扣減失敗" error response handling in order-service/.../application/command/CreateOrderCommandHandler.java
- [ ] T098 [US4] Create Cucumber step definitions for inventory failure in e2e-tests/src/test/java/.../steps/InventoryFailureSteps.java

**Checkpoint**: User Story 4 complete - inventory failures trigger payment void

---

## Phase 7: User Story 5 - Handle Capture Failure with Full Compensation (Priority: P3)

**Goal**: Rollback inventory AND void payment when capture fails

**Independent Test**: Simulate capture failure → verify both compensation steps

### Tests for User Story 5 (TDD)

- [ ] T099 [P] [US5] Unit test for capture failure compensation in order-service/src/test/java/.../application/saga/CreateOrderSagaCaptureFailureTest.java
- [ ] T100 [US5] Cucumber feature for capture failure in e2e-tests/src/test/resources/features/order-capture-failure.feature

### Implementation for User Story 5

- [x] T101 [US5] Create RollbackStockUseCase port in inventory-service/.../application/port/inbound/RollbackStockUseCase.java
- [x] T102 [US5] Create RollbackStockCommandHandler in inventory-service/.../application/command/RollbackStockCommandHandler.java
- [x] T103 [US5] Add POST /rollback endpoint to InventoryCommandController in inventory-service/.../infrastructure/adapter/inbound/rest/InventoryCommandController.java
- [x] T104 [US5] Add capture failure handling to MockAcquirerAdapter in payment-service/.../infrastructure/adapter/outbound/external/MockAcquirerAdapter.java
- [x] T105 [US5] Add full compensation to CreateOrderSaga (rollback inventory + void payment) in order-service/.../application/saga/CreateOrderSaga.java
- [ ] T106 [US5] Create Cucumber step definitions for capture failure in e2e-tests/src/test/java/.../steps/CaptureFailureSteps.java

**Checkpoint**: User Story 5 complete - capture failures trigger full compensation

---

## Phase 8: User Story 6 - Idempotent Order Processing (Priority: P3)

**Goal**: Duplicate requests with same idempotency key return same result

**Independent Test**: Submit same request twice → verify only one order created

### Tests for User Story 6 (TDD)

- [ ] T107 [P] [US6] Unit test for idempotency handling in order-service/src/test/java/.../application/command/CreateOrderCommandHandlerIdempotencyTest.java
- [ ] T108 [US6] Cucumber feature for idempotency in e2e-tests/src/test/resources/features/order-idempotency.feature

### Implementation for User Story 6

- [x] T109 [US6] Add findByIdempotencyKey to OrderRepository port in order-service/.../application/port/outbound/OrderRepository.java
- [x] T110 [US6] Implement findByIdempotencyKey in JpaOrderRepository in order-service/.../infrastructure/adapter/outbound/persistence/JpaOrderRepository.java
- [x] T111 [US6] Add idempotency check to CreateOrderCommandHandler in order-service/.../application/command/CreateOrderCommandHandler.java
- [ ] T112 [US6] Create Cucumber step definitions for idempotency in e2e-tests/src/test/java/.../steps/IdempotencySteps.java

**Checkpoint**: User Story 6 complete - duplicate requests handled idempotently

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements and validation

- [x] T113 [P] Add Swagger annotations to all controllers in order-service, payment-service, inventory-service
- [x] T114 [P] Add structured logging to CreateOrderSaga in order-service/.../application/saga/CreateOrderSaga.java
- [x] T115 [P] Add global exception handler in order-service/.../infrastructure/adapter/inbound/rest/GlobalExceptionHandler.java
- [x] T116 [P] Add global exception handler in payment-service/.../infrastructure/adapter/inbound/rest/GlobalExceptionHandler.java
- [x] T117 [P] Add global exception handler in inventory-service/.../infrastructure/adapter/inbound/rest/GlobalExceptionHandler.java
- [ ] T118 Run jacoco test coverage verification (./gradlew jacocoTestCoverageVerification) - must achieve 80%
- [ ] T119 Validate quickstart.md by running all documented curl commands
- [ ] T120 Run all Cucumber E2E tests (./gradlew :e2e-tests:test)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Foundational - Core MVP
- **US2 (Phase 4)**: Depends on Foundational - Can parallel with US1
- **US3 (Phase 5)**: Depends on US1 (extends SAGA) - Sequential after US1
- **US4 (Phase 6)**: Depends on US1 (extends SAGA) - Sequential after US3
- **US5 (Phase 7)**: Depends on US4 (extends compensation) - Sequential after US4
- **US6 (Phase 8)**: Depends on US1 (extends handler) - Can parallel with US3-US5
- **Polish (Phase 9)**: Depends on all user stories

### User Story Dependencies

```
Foundational
     │
     ├── US1 (P1): Create Order ───────┬── US3 (P2): Payment Failure
     │                                 │
     ├── US2 (P2): Query Order         ├── US4 (P2): Inventory Failure + Compensation
     │                                 │
     └── US6 (P3): Idempotency         └── US5 (P3): Capture Failure + Full Compensation
```

### Within Each User Story

1. Tests written FIRST (TDD) - must FAIL
2. Domain/Application layer implementation
3. Infrastructure layer (controllers, adapters)
4. Verify tests now PASS
5. Cucumber E2E test (if applicable)

### Parallel Opportunities

**Phase 1 (Setup)**: T002-T009 can all run in parallel
**Phase 2 (Foundational)**: Value objects (T010-T015, T019-T021, T024-T026) can run in parallel; persistence entities (T040-T048) can run in parallel
**User Stories**: US2 can parallel with US1; US6 can parallel with US3-US5

---

## Parallel Execution Examples

### Phase 2: Launch Value Objects in Parallel

```bash
# Launch all value object tasks together:
Task: "Create OrderId value object in order-service/.../domain/model/valueobject/OrderId.java"
Task: "Create Buyer value object in order-service/.../domain/model/valueobject/Buyer.java"
Task: "Create OrderItem value object in order-service/.../domain/model/valueobject/OrderItem.java"
Task: "Create Money value object in order-service/.../domain/model/valueobject/Money.java"
Task: "Create PaymentId value object in payment-service/.../domain/model/valueobject/PaymentId.java"
Task: "Create ProductId value object in inventory-service/.../domain/model/valueobject/ProductId.java"
```

### Phase 3: Launch US1 Tests in Parallel (TDD)

```bash
# Launch all US1 unit tests together (must fail initially):
Task: "Unit test for Order aggregate in order-service/src/test/.../OrderTest.java"
Task: "Unit test for Payment aggregate in payment-service/src/test/.../PaymentTest.java"
Task: "Unit test for Product aggregate in inventory-service/src/test/.../ProductTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T009)
2. Complete Phase 2: Foundational (T010-T049)
3. Complete Phase 3: User Story 1 (T050-T076)
4. **STOP and VALIDATE**: Run tests, verify order creation works
5. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 → Test → Deploy (MVP!)
3. Add US2 → Test → Deploy (queries enabled)
4. Add US3-US4 → Test → Deploy (failure handling)
5. Add US5-US6 → Test → Deploy (full resilience)
6. Polish → Final release

### Parallel Team Strategy

With 3 developers after Foundational:
- Developer A: US1 (Create Order) → US3 → US4 → US5
- Developer B: US2 (Query Order) → US6 (Idempotency)
- Developer C: E2E test infrastructure, Polish tasks

---

## Summary

| Phase | Tasks | Stories | Description |
|-------|-------|---------|-------------|
| Phase 1 | T001-T009 | - | Setup |
| Phase 2 | T010-T049 | - | Foundational |
| Phase 3 | T050-T076 | US1 | Create Order (MVP) |
| Phase 4 | T077-T084 | US2 | Query Order |
| Phase 5 | T085-T090 | US3 | Payment Failure |
| Phase 6 | T091-T098 | US4 | Inventory Failure |
| Phase 7 | T099-T106 | US5 | Capture Failure |
| Phase 8 | T107-T112 | US6 | Idempotency |
| Phase 9 | T113-T120 | - | Polish |
| **Total** | **120** | **6** | |

**MVP Scope**: Phases 1-3 (T001-T076) = 76 tasks for minimal viable product
