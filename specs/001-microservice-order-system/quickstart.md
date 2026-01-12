# Quickstart: Microservices Ordering System MVP

**Feature**: 001-microservice-order-system
**Date**: 2026-01-12

## Prerequisites

- Java 21 (JDK)
- Gradle 8.x (or use included wrapper)
- Docker (for Testcontainers in tests)
- Git

## Project Setup

### 1. Clone and Build

```bash
# Navigate to project root
cd order-system

# Build all modules
./gradlew build

# Run tests
./gradlew test

# Generate test coverage report
./gradlew jacocoTestReport
```

### 2. Start Services (Development Mode)

Start each service in a separate terminal:

```bash
# Terminal 1: Order Service (port 8081)
./gradlew :order-service:bootRun --args='--spring.profiles.active=dev'

# Terminal 2: Payment Service (port 8082)
./gradlew :payment-service:bootRun --args='--spring.profiles.active=dev'

# Terminal 3: Inventory Service (port 8083)
./gradlew :inventory-service:bootRun --args='--spring.profiles.active=dev'
```

### 3. Verify Services

Check each service is running:

```bash
# Order Service health
curl http://localhost:8081/actuator/health

# Payment Service health
curl http://localhost:8082/actuator/health

# Inventory Service health
curl http://localhost:8083/actuator/health
```

## API Usage

### Create an Order (Happy Path)

```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: order-demo-001" \
  -d '{
    "buyer": {
      "name": "Rex Wang",
      "email": "rex.wang@example.com"
    },
    "orderItem": {
      "productId": "IPHONE-17-PRO-MAX",
      "productName": "iPhone 17 Pro Max",
      "quantity": 1
    },
    "payment": {
      "method": "CREDIT_CARD",
      "amount": 35000,
      "currency": "TWD",
      "cardNumber": "4111111111111111",
      "expiryDate": "12/28",
      "cvv": "123"
    }
  }'
```

**Expected Response (Success):**
```json
{
  "orderId": "ORD-A1B2C3D4",
  "status": "COMPLETED",
  "message": "訂購成功",
  "createdAt": "2026-01-12T10:30:00Z"
}
```

### Query Order

```bash
curl http://localhost:8081/api/v1/orders/ORD-A1B2C3D4
```

**Expected Response:**
```json
{
  "orderId": "ORD-A1B2C3D4",
  "buyerName": "Rex Wang",
  "buyerEmail": "rex.wang@example.com",
  "productId": "IPHONE-17-PRO-MAX",
  "productName": "iPhone 17 Pro Max",
  "quantity": 1,
  "amount": 35000,
  "currency": "TWD",
  "status": "COMPLETED",
  "createdAt": "2026-01-12T10:30:00Z",
  "updatedAt": "2026-01-12T10:30:05Z"
}
```

### Test Idempotency

Send the same request again with the same X-Idempotency-Key:

```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: order-demo-001" \
  -d '{ ... same payload ... }'
```

**Expected:** Returns the same order ID without re-processing.

### Test Payment Failure

Use a card number known to fail (configured in mock acquirer):

```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: order-demo-002" \
  -d '{
    "buyer": {
      "name": "Rex Wang",
      "email": "rex.wang@example.com"
    },
    "orderItem": {
      "productId": "IPHONE-17-PRO-MAX",
      "productName": "iPhone 17 Pro Max",
      "quantity": 1
    },
    "payment": {
      "method": "CREDIT_CARD",
      "amount": 35000,
      "currency": "TWD",
      "cardNumber": "4000000000000002",
      "expiryDate": "12/28",
      "cvv": "123"
    }
  }'
```

**Expected Response (Payment Failed):**
```json
{
  "orderId": "ORD-X1Y2Z3W4",
  "status": "FAILED",
  "message": "支付失敗",
  "createdAt": "2026-01-12T10:35:00Z"
}
```

### Test Inventory Failure

First, check current stock:

```bash
curl http://localhost:8083/api/v1/inventory/IPHONE-17-PRO-MAX
```

Then create orders until stock runs out (or set stock to 0 via seeded data).

**Expected Response (Inventory Failed):**
```json
{
  "orderId": "ORD-P1Q2R3S4",
  "status": "ROLLBACK_COMPLETED",
  "message": "庫存扣減失敗",
  "createdAt": "2026-01-12T10:40:00Z"
}
```

## API Documentation

Access Swagger UI when services are running:

- Order Service: http://localhost:8081/swagger-ui.html
- Payment Service: http://localhost:8082/swagger-ui.html
- Inventory Service: http://localhost:8083/swagger-ui.html

## Running Tests

### Unit Tests Only

```bash
./gradlew test --tests "*Test"
```

### Integration Tests (requires Docker)

```bash
./gradlew test --tests "*IntegrationTest"
```

### BDD/E2E Tests

```bash
./gradlew :e2e-tests:test
```

### Coverage Report

```bash
./gradlew jacocoTestReport
# Open: build/reports/jacoco/test/html/index.html
```

### Verify Coverage Threshold (80%)

```bash
./gradlew jacocoTestCoverageVerification
```

## Project Structure Reference

```
order-system/
├── order-service/          # Port 8081
├── payment-service/        # Port 8082
├── inventory-service/      # Port 8083
└── e2e-tests/             # Cucumber BDD tests
```

## Configuration

### Service URLs (application.yml)

```yaml
# order-service
services:
  payment:
    url: http://localhost:8082
  inventory:
    url: http://localhost:8083
```

### Timeout Configuration

```yaml
# RestTemplate configuration (5 second timeout)
spring:
  rest:
    connection-timeout: 5000
    read-timeout: 5000
```

## Troubleshooting

### Service Not Starting

1. Check port availability: `lsof -i :8081` (or 8082, 8083)
2. Verify Java 21: `java -version`
3. Check logs: `./gradlew :order-service:bootRun --debug`

### Test Failures

1. Ensure Docker is running (for Testcontainers)
2. Check PostgreSQL container logs
3. Run with verbose: `./gradlew test --info`

### Database Issues (H2 Dev Mode)

Access H2 console: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:orderdb;MODE=PostgreSQL`
- Username: `sa`
- Password: (empty)

## Validation Checklist

Before considering the feature complete:

- [ ] All services start without errors
- [ ] Create order returns COMPLETED status
- [ ] Query order returns correct details
- [ ] Idempotency works (duplicate key returns same order)
- [ ] Payment failure returns FAILED status
- [ ] Inventory failure triggers compensation (ROLLBACK_COMPLETED)
- [ ] All tests pass (`./gradlew test`)
- [ ] Coverage >= 80% (`./gradlew jacocoTestCoverageVerification`)
