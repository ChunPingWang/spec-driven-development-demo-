# Research: Microservices Ordering System MVP

**Feature**: 001-microservice-order-system
**Date**: 2026-01-12
**Status**: Complete

## Overview

This document consolidates technical research and decisions for the microservices ordering system MVP. All major decisions were derived from the PRD and TECH.md specifications.

---

## 1. Architecture Pattern: Hexagonal + CQRS

### Decision
Implement Hexagonal Architecture (Ports & Adapters) with CQRS (Command Query Responsibility Segregation) pattern.

### Rationale
- **Hexagonal**: Isolates domain logic from infrastructure concerns, enabling easy testing and technology swaps
- **CQRS**: Separates read and write models for optimized query performance and clearer command handling
- **DDD Alignment**: Both patterns naturally support bounded contexts and aggregate roots

### Alternatives Considered
| Alternative | Reason Rejected |
|-------------|-----------------|
| Layered Architecture | Less flexible for testing; tighter coupling between layers |
| Clean Architecture | Similar benefits but hexagonal more explicit about ports/adapters |
| Event Sourcing | Deferred to v1.1; adds complexity without MVP necessity |

---

## 2. Distributed Transaction: SAGA Orchestration

### Decision
Implement synchronous SAGA orchestration pattern with the Order service as the orchestrator.

### Rationale
- **Consistency**: Ensures data consistency across Order, Payment, and Inventory services
- **Compensation**: Clear rollback path at each step (void payment, rollback inventory)
- **Simplicity**: Synchronous flow easier to debug and trace than choreography
- **MVP Fit**: Explicit control flow matches B2B reliability requirements

### SAGA Flow
```
1. CreateOrder         → Order CREATED
2. AuthorizePayment    → Order PAYMENT_AUTHORIZED (or FAILED)
3. DeductInventory     → Order INVENTORY_DEDUCTED (or compensate → ROLLBACK_COMPLETED)
4. CapturePayment      → Order COMPLETED (or compensate → ROLLBACK_COMPLETED)
```

### Compensation Matrix
| Failure Point | Compensation Actions |
|---------------|---------------------|
| Payment Authorization | None (order marked FAILED) |
| Inventory Deduction | Void payment authorization |
| Payment Capture | Rollback inventory, then void payment |

### Alternatives Considered
| Alternative | Reason Rejected |
|-------------|-----------------|
| Choreography (Event-driven) | More complex debugging; deferred to v1.1 |
| 2PC (Two-Phase Commit) | Not suitable for microservices; blocking |
| Outbox Pattern | Adds infrastructure complexity; not needed for MVP |

---

## 3. Technology Stack

### Decision
Java 21 with Spring Boot 3.x in a Gradle multi-module monorepo.

### Rationale
- **Java 21**: Latest LTS with virtual threads, pattern matching, records
- **Spring Boot 3.x**: Industry standard for microservices, excellent DDD support
- **Gradle**: Better multi-module support than Maven, faster builds
- **Monorepo**: Simplifies dependency management and atomic changes across services

### Stack Details
| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.x |
| Build | Gradle | 8.x |
| ORM | Spring Data JPA | 3.x |
| DB (Dev) | H2 | In-memory |
| DB (Test) | PostgreSQL | 15 (Testcontainers) |
| API Docs | Swagger/OpenAPI | 3.0 |
| Unit Test | JUnit 5 + Mockito | 5.x |
| BDD | Cucumber | 7.x |
| Contract | Spring Cloud Contract | 4.1.x |

### Alternatives Considered
| Alternative | Reason Rejected |
|-------------|-----------------|
| Kotlin | Team familiarity with Java; Kotlin adds learning curve |
| Maven | Gradle better for multi-module, faster incremental builds |
| Polyrepo | Increases coordination overhead for MVP |

---

## 4. Inter-Service Communication

### Decision
REST/HTTP with JSON payloads, 5-second timeout, synchronous calls.

### Rationale
- **REST**: Standard, well-understood, easy to test and debug
- **JSON**: Human-readable, good tooling, sufficient for MVP scale
- **5s Timeout**: Balance between allowing transient latency and failing fast

### Service Endpoints
| Service | Base URL | Port |
|---------|----------|------|
| Order | http://localhost:8081 | 8081 |
| Payment | http://localhost:8082 | 8082 |
| Inventory | http://localhost:8083 | 8083 |

### Alternatives Considered
| Alternative | Reason Rejected |
|-------------|-----------------|
| gRPC | Added complexity; REST sufficient for MVP |
| GraphQL | Overkill for B2B service-to-service calls |
| Message Queue | Synchronous SAGA chosen; async deferred to v1.1 |

---

## 5. Database Strategy

### Decision
Schema-per-service isolation with same PostgreSQL instance for MVP.

### Rationale
- **Schema Isolation**: Each service owns its schema (order_schema, payment_schema, inventory_schema)
- **Shared Instance**: Reduces operational complexity for MVP
- **H2 for Dev**: Fast local development without external dependencies

### Schema Allocation
| Service | Schema | Key Tables |
|---------|--------|------------|
| Order | order_schema | orders |
| Payment | payment_schema | payments, payment_transactions |
| Inventory | inventory_schema | products, inventory_logs |

### Alternatives Considered
| Alternative | Reason Rejected |
|-------------|-----------------|
| Database-per-service | Operational overhead for MVP |
| Shared tables | Violates bounded context principle |
| NoSQL | Relational model fits transactional domain |

---

## 6. Idempotency Strategy

### Decision
Header-based idempotency key for order creation; order ID as natural key for downstream operations.

### Rationale
- **X-Idempotency-Key Header**: Client-provided unique identifier for order creation
- **Order ID**: Natural idempotency key for payment and inventory (one operation per order)
- **Database Unique Constraints**: Enforce at storage level

### Implementation
```
Order Creation:  X-Idempotency-Key header → orders.idempotency_key (UNIQUE)
Payment:         order_id → payments.order_id (UNIQUE)
Inventory:       order_id + product_id + operation → inventory_logs (UNIQUE)
```

---

## 7. Testing Strategy

### Decision
Multi-layer testing aligned with hexagonal architecture.

### Rationale
- **Domain Tests**: Pure unit tests, no mocks needed (domain has no dependencies)
- **Application Tests**: Unit tests with mocked ports
- **Infrastructure Tests**: Integration tests with Testcontainers
- **E2E Tests**: Cucumber BDD scenarios

### Test Distribution
| Layer | Test Type | Tools | Coverage Target |
|-------|-----------|-------|-----------------|
| Domain | Unit | JUnit 5 | 90%+ |
| Application | Unit | JUnit 5 + Mockito | 80%+ |
| Infrastructure | Integration | Spring Boot Test + Testcontainers | 80%+ |
| E2E | BDD | Cucumber | Critical paths |
| Contract | Contract | Spring Cloud Contract | All API endpoints |

---

## 8. Error Handling

### Decision
Simplified error responses with localized messages for MVP.

### Rationale
- **B2B Focus**: External systems need clear, consistent error codes
- **Localization**: Chinese error messages per PRD requirements
- **No Detailed Codes**: MVP defers granular error taxonomy

### Error Mapping
| Failure Type | HTTP Status | Message |
|--------------|-------------|---------|
| Payment failure | 200 (order failed) | 支付失敗 |
| Inventory failure | 200 (order failed) | 庫存扣減失敗 |
| Order not found | 404 | Order not found |
| Validation error | 400 | Validation failed |
| Service timeout | 200 (order failed) | Service unavailable |

---

## 9. Deferred to Post-MVP

| Feature | Target Version | Reason for Deferral |
|---------|---------------|---------------------|
| Event Sourcing | v1.1 | Adds complexity without MVP necessity |
| API Gateway | v1.1 | Direct service calls sufficient for MVP |
| Authentication/Authorization | v1.1 | B2B auth mechanism TBD |
| Distributed Tracing | v1.1 | Logging sufficient for MVP debugging |
| Read Model Separation | v1.2 | Same DB adequate for MVP scale |
| Multi-currency | v1.2 | TWD only for MVP |
| Multi-item Orders | v1.2 | Single product per order for MVP |

---

## Resolved Clarifications

All NEEDS CLARIFICATION items from Technical Context have been resolved:

| Item | Resolution | Source |
|------|------------|--------|
| Language/Version | Java 21 | TECH.md, Clarification Session |
| Inter-service timeout | 5 seconds | Clarification Session |
| Communication protocol | REST/HTTP with JSON | Clarification Session |
| Build tool | Gradle multi-module | TECH.md |
| Database | H2 (dev) / PostgreSQL (test) | TECH.md |

**Phase 0 Complete. Ready for Phase 1: Design & Contracts.**
