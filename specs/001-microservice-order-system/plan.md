# Implementation Plan: Microservices Ordering System MVP

**Branch**: `001-microservice-order-system` | **Date**: 2026-01-12 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-microservice-order-system/spec.md`

## Summary

Build a microservices-based ordering system with three bounded contexts (Order, Payment, Inventory) following DDD, Hexagonal Architecture, and CQRS patterns. The system implements a synchronous SAGA orchestration for distributed transaction management with full compensation support. Core flow: CreateOrder → Authorize Payment → Deduct Inventory → Capture Payment → Complete Order, with rollback capabilities at each failure point.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, Spring Web
**Build Tool**: Gradle (Multi-module monorepo)
**Storage**: H2 (development), PostgreSQL with Testcontainers (testing)
**Testing**: JUnit 5 + Mockito (unit), Spring Boot Test + Testcontainers (integration), Cucumber (BDD), Spring Cloud Contract (contract)
**Target Platform**: Linux server / Docker containers
**Project Type**: Microservices monorepo (3 services)
**API Documentation**: Swagger / OpenAPI 3.0
**Performance Goals**: Order completion < 10 seconds, Query response < 1 second
**Constraints**: 5-second inter-service timeout, single currency (TWD), single product per order
**Scale/Scope**: MVP - B2B integration, synchronous orchestration

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I. Code Quality First | ✅ PASS | Clean architecture with clear separation, meaningful DDD naming conventions |
| II. Test-Driven Development (TDD) | ✅ PASS | JUnit 5 + Mockito for unit tests, Testcontainers for integration, TDD workflow enforced |
| III. Behavior-Driven Development (BDD) | ✅ PASS | Cucumber features for E2E tests, Given-When-Then acceptance scenarios in spec |
| IV. Domain-Driven Design (DDD) | ✅ PASS | Bounded contexts (Order, Payment, Inventory), Aggregates, Value Objects, Domain Events |
| V. SOLID Principles | ✅ PASS | Hexagonal ports/adapters enforce DIP, interface segregation via use case interfaces |
| VI. Hexagonal Architecture | ✅ PASS | Domain → Application → Infrastructure layers with ports/adapters pattern |

**Architecture Constraints Check:**

| Constraint | Status | Evidence |
|------------|--------|----------|
| Dependency Flow (inward only) | ✅ PASS | Infrastructure implements Application ports; Domain has zero external dependencies |
| Interface Requirements | ✅ PASS | Ports defined in Application layer (e.g., OrderRepository, PaymentServicePort) |
| Mapper Requirements | ✅ PASS | OrderMapper for JPA ↔ Domain conversion, DTOs for API layer |

**All gates passed. Proceeding to Phase 0.**

## Project Structure

### Documentation (this feature)

```text
specs/001-microservice-order-system/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (OpenAPI specs)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
order-system/
├── build.gradle                    # Root build configuration
├── settings.gradle                 # Module definitions
├── gradle.properties               # Shared properties
│
├── order-service/                  # Order microservice (SAGA orchestrator)
│   ├── build.gradle
│   └── src/
│       ├── main/java/com/example/order/
│       │   ├── domain/             # Domain Layer
│       │   │   ├── model/
│       │   │   │   ├── aggregate/  # Order (Aggregate Root)
│       │   │   │   └── valueobject/# OrderId, Buyer, OrderItem, Money, OrderStatus
│       │   │   ├── event/          # OrderCreated, OrderCompleted, etc.
│       │   │   └── exception/
│       │   ├── application/        # Application Layer
│       │   │   ├── port/
│       │   │   │   ├── inbound/    # CreateOrderUseCase, GetOrderUseCase
│       │   │   │   └── outbound/   # OrderRepository, PaymentServicePort, InventoryServicePort
│       │   │   ├── command/        # CreateOrderCommand, CreateOrderCommandHandler
│       │   │   ├── query/          # GetOrderQuery, GetOrderQueryHandler, OrderReadModel
│       │   │   ├── dto/            # Request/Response DTOs
│       │   │   └── saga/           # CreateOrderSaga
│       │   └── infrastructure/     # Infrastructure Layer
│       │       ├── adapter/
│       │       │   ├── inbound/rest/   # OrderCommandController, OrderQueryController
│       │       │   └── outbound/
│       │       │       ├── persistence/# JpaOrderRepository, OrderJpaEntity, OrderMapper
│       │       │       └── external/   # PaymentServiceAdapter, InventoryServiceAdapter
│       │       └── config/
│       └── test/
│
├── payment-service/                # Payment microservice
│   └── src/main/java/com/example/payment/
│       ├── domain/                 # Payment aggregate, PaymentStatus, CardInfo
│       ├── application/            # Authorize/Capture/Void use cases
│       └── infrastructure/         # Controllers, JPA, Mock Acquirer
│
├── inventory-service/              # Inventory microservice
│   └── src/main/java/com/example/inventory/
│       ├── domain/                 # Product aggregate, StockQuantity
│       ├── application/            # Deduct/Rollback/GetStock use cases
│       └── infrastructure/         # Controllers, JPA
│
└── e2e-tests/                      # End-to-end BDD tests
    └── src/test/
        ├── java/                   # Step definitions
        └── resources/features/     # Cucumber feature files
```

**Structure Decision**: Gradle multi-module monorepo with 3 microservices + E2E test module. Each service follows identical hexagonal package structure. This enables shared build configuration while maintaining service independence.

## Complexity Tracking

> No constitution violations requiring justification. Architecture aligns with all principles.

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| 3 Microservices | Required by domain | Order, Payment, Inventory are distinct bounded contexts per PRD |
| SAGA Orchestration | Synchronous | MVP simplicity; event-driven choreography deferred to v1.1 |
| Same DB for CQRS | MVP scope | Read/Write model separation without separate storage |
