# 微服務訂購系統 - 技術規格文件 (TECH)

## 文件資訊

| 項目 | 內容 |
|------|------|
| 文件版本 | 2.0 |
| 建立日期 | 2026-01-11 |
| 專案名稱 | 微服務訂購系統 MVP |
| 架構模式 | DDD + Hexagonal Architecture + CQRS |

---

## 1. 架構設計原則

### 1.1 六角形架構（Hexagonal Architecture）

```
                            ┌─────────────────────────────────────┐
                            │         Driving Adapters            │
                            │  (Primary / Inbound)                │
                            │                                     │
                            │  ┌─────────────┐ ┌─────────────┐   │
                            │  │ REST        │ │ Message     │   │
                            │  │ Controller  │ │ Listener    │   │
                            │  └──────┬──────┘ └──────┬──────┘   │
                            └─────────┼───────────────┼───────────┘
                                      │               │
                                      ▼               ▼
                            ┌─────────────────────────────────────┐
                            │         Driving Ports               │
                            │  (Primary / Inbound)                │
                            │                                     │
                            │  ┌─────────────────────────────┐   │
                            │  │    Use Case Interfaces      │   │
                            │  │  (Command/Query Handlers)   │   │
                            │  └──────────────┬──────────────┘   │
                            └─────────────────┼───────────────────┘
                                              │
                    ┌─────────────────────────┼─────────────────────────┐
                    │                         ▼                         │
                    │  ┌─────────────────────────────────────────────┐ │
                    │  │            Application Layer                │ │
                    │  │                                             │ │
                    │  │  ┌─────────────┐     ┌─────────────┐       │ │
                    │  │  │ Command     │     │ Query       │       │ │
                    │  │  │ Handlers    │     │ Handlers    │       │ │
                    │  │  └──────┬──────┘     └──────┬──────┘       │ │
                    │  └─────────┼───────────────────┼───────────────┘ │
                    │            │                   │                 │
                    │            ▼                   ▼                 │
                    │  ┌─────────────────────────────────────────────┐ │
                    │  │              Domain Layer                   │ │
                    │  │                                             │ │
                    │  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ │ │
                    │  │  │ Aggregate │ │  Domain   │ │  Domain   │ │ │
                    │  │  │   Root    │ │  Service  │ │  Event    │ │ │
                    │  │  └───────────┘ └───────────┘ └───────────┘ │ │
                    │  │                                             │ │
                    │  │  ┌───────────┐ ┌───────────┐               │ │
                    │  │  │  Entity   │ │   Value   │               │ │
                    │  │  │           │ │  Object   │               │ │
                    │  │  └───────────┘ └───────────┘               │ │
                    │  └─────────────────────────────────────────────┘ │
                    │                    CORE                          │
                    └─────────────────────┬─────────────────────────────┘
                                          │
                            ┌─────────────┼───────────────────┐
                            │             ▼                   │
                            │        Driven Ports             │
                            │  (Secondary / Outbound)         │
                            │                                 │
                            │  ┌───────────┐ ┌───────────┐   │
                            │  │Repository │ │ External  │   │
                            │  │ Interface │ │ Service   │   │
                            │  │  (Port)   │ │ Interface │   │
                            │  └─────┬─────┘ └─────┬─────┘   │
                            └────────┼─────────────┼──────────┘
                                     │             │
                                     ▼             ▼
                            ┌─────────────────────────────────────┐
                            │        Driven Adapters              │
                            │  (Secondary / Outbound)             │
                            │                                     │
                            │  ┌─────────────┐ ┌─────────────┐   │
                            │  │ JPA         │ │ REST        │   │
                            │  │ Repository  │ │ Client      │   │
                            │  └─────────────┘ └─────────────┘   │
                            └─────────────────────────────────────┘
```

### 1.2 依賴規則（Dependency Rule）

**核心原則：依賴方向由外向內，內層不得依賴外層**

```
┌─────────────────────────────────────────────────────────────────┐
│                      Infrastructure Layer                        │
│  (Controllers, Repositories, External Clients)                   │
│                              │                                   │
│                              │ depends on                        │
│                              ▼                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Application Layer                       │  │
│  │  (Use Cases, Command/Query Handlers, DTOs)                │  │
│  │                              │                             │  │
│  │                              │ depends on                  │  │
│  │                              ▼                             │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │                    Domain Layer                      │  │  │
│  │  │  (Aggregates, Entities, Value Objects, Events)      │  │  │
│  │  │                                                      │  │  │
│  │  │            *** NO EXTERNAL DEPENDENCIES ***          │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

✓ Infrastructure → Application → Domain
✗ Domain → Application (禁止)
✗ Domain → Infrastructure (禁止)
✗ Application → Infrastructure (禁止)
```

### 1.3 CQRS 架構

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Request                          │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                │                               │
                ▼                               ▼
┌───────────────────────────┐   ┌───────────────────────────┐
│      Command Side         │   │       Query Side          │
│                           │   │                           │
│  ┌─────────────────────┐  │   │  ┌─────────────────────┐  │
│  │  Command Handler    │  │   │  │   Query Handler     │  │
│  └──────────┬──────────┘  │   │  └──────────┬──────────┘  │
│             │             │   │             │             │
│             ▼             │   │             ▼             │
│  ┌─────────────────────┐  │   │  ┌─────────────────────┐  │
│  │   Domain Model      │  │   │  │   Read Model        │  │
│  │   (Aggregate)       │  │   │  │   (Projection)      │  │
│  └──────────┬──────────┘  │   │  └──────────┬──────────┘  │
│             │             │   │             │             │
│             ▼             │   │             ▼             │
│  ┌─────────────────────┐  │   │  ┌─────────────────────┐  │
│  │  Write Repository   │  │   │  │   Read Repository   │  │
│  └─────────────────────┘  │   │  └─────────────────────┘  │
└───────────────────────────┘   └───────────────────────────┘
                │                               │
                └───────────────┬───────────────┘
                                ▼
                    ┌───────────────────────┐
                    │      Database         │
                    │  (Same DB for MVP)    │
                    └───────────────────────┘
```

---

## 2. 技術堆疊

| 層級 | 技術選擇 |
|------|----------|
| 語言 | Java 21 |
| 框架 | Spring Boot 3.x |
| 建置工具 | Gradle (Multi-module) |
| 資料庫（開發） | H2 (In-memory) |
| 資料庫（測試） | PostgreSQL (Testcontainers) |
| API 文件 | Swagger / OpenAPI 3.0 |
| 契約測試 | Spring Cloud Contract |
| 單元測試 | JUnit 5 + Mockito |
| BDD 測試 | Cucumber |
| 服務通訊 | REST (HTTP) |

---

## 3. 專案結構

### 3.1 Monorepo 目錄結構

```
order-system/
├── build.gradle                    # Root build configuration
├── settings.gradle                 # Module definitions
├── gradle.properties               # Shared properties
│
├── order-service/                  # 訂單微服務
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/example/order/
│       │   │       │
│       │   │       ├── OrderServiceApplication.java
│       │   │       │
│       │   │       ├── domain/                      # Domain Layer
│       │   │       │   ├── model/
│       │   │       │   │   ├── aggregate/
│       │   │       │   │   │   └── Order.java              # Aggregate Root
│       │   │       │   │   ├── entity/
│       │   │       │   │   └── valueobject/
│       │   │       │   │       ├── OrderId.java
│       │   │       │   │       ├── Buyer.java
│       │   │       │   │       ├── OrderItem.java
│       │   │       │   │       ├── Money.java
│       │   │       │   │       ├── PaymentInfo.java
│       │   │       │   │       └── OrderStatus.java
│       │   │       │   ├── event/
│       │   │       │   │   ├── OrderCreated.java
│       │   │       │   │   ├── OrderCompleted.java
│       │   │       │   │   ├── OrderFailed.java
│       │   │       │   │   └── OrderRolledBack.java
│       │   │       │   ├── service/                       # Domain Service
│       │   │       │   │   └── OrderDomainService.java
│       │   │       │   └── exception/
│       │   │       │       └── OrderDomainException.java
│       │   │       │
│       │   │       ├── application/                  # Application Layer
│       │   │       │   ├── port/
│       │   │       │   │   ├── inbound/                   # Driving Ports
│       │   │       │   │   │   ├── CreateOrderUseCase.java
│       │   │       │   │   │   └── GetOrderUseCase.java
│       │   │       │   │   └── outbound/                  # Driven Ports
│       │   │       │   │       ├── OrderRepository.java
│       │   │       │   │       ├── PaymentServicePort.java
│       │   │       │   │       └── InventoryServicePort.java
│       │   │       │   ├── command/
│       │   │       │   │   ├── CreateOrderCommand.java
│       │   │       │   │   └── CreateOrderCommandHandler.java
│       │   │       │   ├── query/
│       │   │       │   │   ├── GetOrderQuery.java
│       │   │       │   │   ├── GetOrderQueryHandler.java
│       │   │       │   │   └── OrderReadModel.java
│       │   │       │   ├── dto/
│       │   │       │   │   ├── CreateOrderRequest.java
│       │   │       │   │   ├── CreateOrderResponse.java
│       │   │       │   │   ├── OrderDetailResponse.java
│       │   │       │   │   ├── PaymentRequest.java
│       │   │       │   │   ├── PaymentResponse.java
│       │   │       │   │   ├── InventoryRequest.java
│       │   │       │   │   └── InventoryResponse.java
│       │   │       │   └── saga/
│       │   │       │       └── CreateOrderSaga.java
│       │   │       │
│       │   │       └── infrastructure/               # Infrastructure Layer
│       │   │           ├── adapter/
│       │   │           │   ├── inbound/                   # Driving Adapters
│       │   │           │   │   └── rest/
│       │   │           │   │       ├── OrderCommandController.java
│       │   │           │   │       └── OrderQueryController.java
│       │   │           │   └── outbound/                  # Driven Adapters
│       │   │           │       ├── persistence/
│       │   │           │       │   ├── JpaOrderRepository.java
│       │   │           │       │   ├── OrderJpaEntity.java
│       │   │           │       │   └── OrderMapper.java
│       │   │           │       └── external/
│       │   │           │           ├── PaymentServiceAdapter.java
│       │   │           │           └── InventoryServiceAdapter.java
│       │   │           └── config/
│       │   │               ├── BeanConfiguration.java
│       │   │               └── OpenApiConfig.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-test.yml
│       │       ├── schema.sql
│       │       └── data.sql
│       │
│       ├── test/
│       │   └── java/
│       │       └── com/example/order/
│       │           ├── domain/
│       │           ├── application/
│       │           └── infrastructure/
│       │
│       └── contractTest/
│           └── resources/contracts/
│
├── payment-service/                # 支付微服務
│   ├── build.gradle
│   └── src/
│       └── main/
│           └── java/
│               └── com/example/payment/
│                   │
│                   ├── PaymentServiceApplication.java
│                   │
│                   ├── domain/
│                   │   ├── model/
│                   │   │   ├── aggregate/
│                   │   │   │   └── Payment.java
│                   │   │   ├── entity/
│                   │   │   │   └── PaymentTransaction.java
│                   │   │   └── valueobject/
│                   │   │       ├── PaymentId.java
│                   │   │       ├── Money.java
│                   │   │       ├── CardInfo.java
│                   │   │       ├── AuthorizationCode.java
│                   │   │       └── PaymentStatus.java
│                   │   ├── event/
│                   │   │   ├── PaymentAuthorized.java
│                   │   │   ├── PaymentCaptured.java
│                   │   │   └── PaymentVoided.java
│                   │   └── exception/
│                   │
│                   ├── application/
│                   │   ├── port/
│                   │   │   ├── inbound/
│                   │   │   │   ├── AuthorizePaymentUseCase.java
│                   │   │   │   ├── CapturePaymentUseCase.java
│                   │   │   │   ├── VoidPaymentUseCase.java
│                   │   │   │   └── GetPaymentUseCase.java
│                   │   │   └── outbound/
│                   │   │       ├── PaymentRepository.java
│                   │   │       └── AcquirerPort.java
│                   │   ├── command/
│                   │   │   ├── AuthorizePaymentCommand.java
│                   │   │   ├── AuthorizePaymentCommandHandler.java
│                   │   │   ├── CapturePaymentCommand.java
│                   │   │   ├── CapturePaymentCommandHandler.java
│                   │   │   ├── VoidPaymentCommand.java
│                   │   │   └── VoidPaymentCommandHandler.java
│                   │   ├── query/
│                   │   └── dto/
│                   │
│                   └── infrastructure/
│                       ├── adapter/
│                       │   ├── inbound/
│                       │   │   └── rest/
│                       │   │       ├── PaymentCommandController.java
│                       │   │       └── PaymentQueryController.java
│                       │   └── outbound/
│                       │       ├── persistence/
│                       │       │   ├── JpaPaymentRepository.java
│                       │       │   ├── PaymentJpaEntity.java
│                       │       │   └── PaymentMapper.java
│                       │       └── external/
│                       │           ├── MockAcquirerAdapter.java
│                       │           └── AcquirerAdapter.java
│                       └── config/
│
├── inventory-service/              # 庫存微服務
│   ├── build.gradle
│   └── src/
│       └── main/
│           └── java/
│               └── com/example/inventory/
│                   │
│                   ├── InventoryServiceApplication.java
│                   │
│                   ├── domain/
│                   │   ├── model/
│                   │   │   ├── aggregate/
│                   │   │   │   └── Product.java
│                   │   │   └── valueobject/
│                   │   │       ├── ProductId.java
│                   │   │       ├── StockQuantity.java
│                   │   │       └── InventoryOperation.java
│                   │   ├── event/
│                   │   │   ├── StockDeducted.java
│                   │   │   └── StockRolledBack.java
│                   │   └── exception/
│                   │       └── InsufficientStockException.java
│                   │
│                   ├── application/
│                   │   ├── port/
│                   │   │   ├── inbound/
│                   │   │   │   ├── DeductStockUseCase.java
│                   │   │   │   ├── RollbackStockUseCase.java
│                   │   │   │   └── GetStockUseCase.java
│                   │   │   └── outbound/
│                   │   │       ├── ProductRepository.java
│                   │   │       └── InventoryLogRepository.java
│                   │   ├── command/
│                   │   ├── query/
│                   │   └── dto/
│                   │
│                   └── infrastructure/
│                       ├── adapter/
│                       │   ├── inbound/rest/
│                       │   └── outbound/persistence/
│                       └── config/
│
└── e2e-tests/                      # 端對端測試
    ├── build.gradle
    └── src/
        └── test/
            ├── java/
            │   └── com/example/e2e/
            │       ├── steps/
            │       └── config/
            └── resources/
                └── features/
```

### 3.2 模組依賴關係

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           order-service module                          │
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                    infrastructure package                        │  │
│   │                                                                  │  │
│   │   OrderCommandController ──────┐                                │  │
│   │   OrderQueryController ────────┤                                │  │
│   │   JpaOrderRepository ──────────┤      implements                │  │
│   │   PaymentServiceAdapter ───────┤          │                     │  │
│   │   InventoryServiceAdapter ─────┘          │                     │  │
│   │                                           │                     │  │
│   └───────────────────────────────────────────┼─────────────────────┘  │
│                                               │                         │
│                                               ▼                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                     application package                          │  │
│   │                                                                  │  │
│   │   ┌─────────────────────────────────────────────────────────┐   │  │
│   │   │  port/inbound (interfaces)                              │   │  │
│   │   │    CreateOrderUseCase ◄────── implemented by Handler    │   │  │
│   │   │    GetOrderUseCase ◄────────── implemented by Handler   │   │  │
│   │   └─────────────────────────────────────────────────────────┘   │  │
│   │                                                                  │  │
│   │   CreateOrderCommandHandler ───────────────┐                    │  │
│   │   GetOrderQueryHandler ────────────────────┤                    │  │
│   │                                            │  uses              │  │
│   │   ┌────────────────────────────────────────┼────────────────┐   │  │
│   │   │  port/outbound (interfaces)            │                │   │  │
│   │   │    OrderRepository ◄───────────────────┤                │   │  │
│   │   │    PaymentServicePort ◄────────────────┤                │   │  │
│   │   │    InventoryServicePort ◄──────────────┘                │   │  │
│   │   └─────────────────────────────────────────────────────────┘   │  │
│   │                           │                                      │  │
│   └───────────────────────────┼──────────────────────────────────────┘  │
│                               │ uses                                    │
│                               ▼                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                       domain package                             │  │
│   │                                                                  │  │
│   │   Order (Aggregate Root)                                        │  │
│   │   OrderId, Buyer, OrderItem, Money (Value Objects)              │  │
│   │   OrderCreated, OrderCompleted (Domain Events)                  │  │
│   │   OrderDomainService                                            │  │
│   │                                                                  │  │
│   │              *** ZERO EXTERNAL DEPENDENCIES ***                 │  │
│   │                                                                  │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. 各層實作細節

### 4.1 Domain Layer

#### Order Aggregate Root

```java
// domain/model/aggregate/Order.java
package com.example.order.domain.model.aggregate;

import com.example.order.domain.model.valueobject.*;
import com.example.order.domain.event.*;
import java.util.ArrayList;
import java.util.List;

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
    
    // Private constructor - use factory method
    private Order() {}
    
    // Factory method
    public static Order create(
            String idempotencyKey,
            Buyer buyer,
            OrderItem orderItem,
            Money money,
            PaymentInfo paymentInfo) {
        
        Order order = new Order();
        order.orderId = OrderId.generate();
        order.idempotencyKey = idempotencyKey;
        order.buyer = buyer;
        order.orderItem = orderItem;
        order.money = money;
        order.paymentInfo = paymentInfo;
        order.status = OrderStatus.CREATED;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        
        order.registerEvent(new OrderCreated(order.orderId, order.buyer, order.orderItem));
        
        return order;
    }
    
    // Domain behavior methods
    public void markPaymentAuthorized(String paymentId) {
        validateStatus(OrderStatus.CREATED);
        this.paymentId = paymentId;
        this.status = OrderStatus.PAYMENT_AUTHORIZED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new PaymentAuthorizedEvent(this.orderId, paymentId));
    }
    
    public void markInventoryDeducted() {
        validateStatus(OrderStatus.PAYMENT_AUTHORIZED);
        this.status = OrderStatus.INVENTORY_DEDUCTED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new InventoryDeductedEvent(this.orderId));
    }
    
    public void complete() {
        validateStatus(OrderStatus.INVENTORY_DEDUCTED);
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new OrderCompleted(this.orderId));
    }
    
    public void fail() {
        this.status = OrderStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new OrderFailed(this.orderId));
    }
    
    public void markRolledBack() {
        this.status = OrderStatus.ROLLBACK_COMPLETED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new OrderRolledBack(this.orderId));
    }
    
    private void validateStatus(OrderStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                "Invalid state transition. Expected: " + expected + ", Actual: " + this.status);
        }
    }
    
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // Getters (no setters - immutability)
    public OrderId getOrderId() { return orderId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Buyer getBuyer() { return buyer; }
    public OrderItem getOrderItem() { return orderItem; }
    public Money getMoney() { return money; }
    public PaymentInfo getPaymentInfo() { return paymentInfo; }
    public OrderStatus getStatus() { return status; }
    public String getPaymentId() { return paymentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

#### Value Objects

```java
// domain/model/valueobject/OrderId.java
package com.example.order.domain.model.valueobject;

import java.util.UUID;

public record OrderId(String value) {
    
    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId cannot be null or blank");
        }
    }
    
    public static OrderId generate() {
        return new OrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
    
    public static OrderId of(String value) {
        return new OrderId(value);
    }
}

// domain/model/valueobject/Money.java
package com.example.order.domain.model.valueobject;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {
    
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be 3-letter code");
        }
    }
    
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }
}

// domain/model/valueobject/Buyer.java
package com.example.order.domain.model.valueobject;

public record Buyer(String name, String email) {
    
    public Buyer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Buyer name cannot be blank");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}

// domain/model/valueobject/OrderItem.java
package com.example.order.domain.model.valueobject;

public record OrderItem(String productId, String productName, int quantity) {
    
    public OrderItem {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("ProductId cannot be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}

// domain/model/valueobject/OrderStatus.java
package com.example.order.domain.model.valueobject;

public enum OrderStatus {
    CREATED,
    PAYMENT_AUTHORIZED,
    INVENTORY_DEDUCTED,
    COMPLETED,
    FAILED,
    ROLLBACK_COMPLETED
}
```

### 4.2 Application Layer

#### Ports (Interfaces)

```java
// application/port/inbound/CreateOrderUseCase.java
package com.example.order.application.port.inbound;

import com.example.order.application.command.CreateOrderCommand;
import com.example.order.application.dto.CreateOrderResponse;

public interface CreateOrderUseCase {
    CreateOrderResponse execute(CreateOrderCommand command);
}

// application/port/inbound/GetOrderUseCase.java
package com.example.order.application.port.inbound;

import com.example.order.application.query.GetOrderQuery;
import com.example.order.application.query.OrderReadModel;

public interface GetOrderUseCase {
    OrderReadModel execute(GetOrderQuery query);
}

// application/port/outbound/OrderRepository.java
package com.example.order.application.port.outbound;

import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.OrderId;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(OrderId orderId);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}

// application/port/outbound/PaymentServicePort.java
package com.example.order.application.port.outbound;

import com.example.order.application.dto.PaymentRequest;
import com.example.order.application.dto.PaymentResponse;

public interface PaymentServicePort {
    PaymentResponse authorize(PaymentRequest request);
    PaymentResponse capture(String paymentId);
    PaymentResponse voidPayment(String paymentId);
}

// application/port/outbound/InventoryServicePort.java
package com.example.order.application.port.outbound;

import com.example.order.application.dto.InventoryRequest;
import com.example.order.application.dto.InventoryResponse;

public interface InventoryServicePort {
    InventoryResponse deduct(InventoryRequest request);
    InventoryResponse rollback(InventoryRequest request);
}
```

#### Command & Handler

```java
// application/command/CreateOrderCommand.java
package com.example.order.application.command;

public record CreateOrderCommand(
    String idempotencyKey,
    String buyerName,
    String buyerEmail,
    String productId,
    String productName,
    int quantity,
    BigDecimal amount,
    String currency,
    String paymentMethod,
    String cardNumber,
    String expiryDate,
    String cvv
) {}

// application/command/CreateOrderCommandHandler.java
package com.example.order.application.command;

import com.example.order.application.port.inbound.CreateOrderUseCase;
import com.example.order.application.port.outbound.*;
import com.example.order.application.dto.*;
import com.example.order.application.saga.CreateOrderSaga;
import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderCommandHandler implements CreateOrderUseCase {
    
    private final OrderRepository orderRepository;
    private final CreateOrderSaga createOrderSaga;
    
    public CreateOrderCommandHandler(
            OrderRepository orderRepository,
            CreateOrderSaga createOrderSaga) {
        this.orderRepository = orderRepository;
        this.createOrderSaga = createOrderSaga;
    }
    
    @Override
    @Transactional
    public CreateOrderResponse execute(CreateOrderCommand command) {
        // Check idempotency
        var existingOrder = orderRepository.findByIdempotencyKey(command.idempotencyKey());
        if (existingOrder.isPresent()) {
            return toResponse(existingOrder.get());
        }
        
        // Create domain object
        Order order = Order.create(
            command.idempotencyKey(),
            new Buyer(command.buyerName(), command.buyerEmail()),
            new OrderItem(command.productId(), command.productName(), command.quantity()),
            Money.of(command.amount(), command.currency()),
            new PaymentInfo(command.paymentMethod(), command.cardNumber(), 
                           command.expiryDate(), command.cvv())
        );
        
        // Execute SAGA
        Order completedOrder = createOrderSaga.execute(order);
        
        return toResponse(completedOrder);
    }
    
    private CreateOrderResponse toResponse(Order order) {
        return new CreateOrderResponse(
            order.getOrderId().value(),
            order.getStatus().name(),
            order.getStatus() == OrderStatus.COMPLETED ? "訂購成功" : "訂購失敗",
            order.getCreatedAt()
        );
    }
}
```

#### Query & Handler

```java
// application/query/GetOrderQuery.java
package com.example.order.application.query;

public record GetOrderQuery(String orderId) {}

// application/query/OrderReadModel.java
package com.example.order.application.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderReadModel(
    String orderId,
    String buyerName,
    String buyerEmail,
    String productId,
    String productName,
    int quantity,
    BigDecimal amount,
    String currency,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

// application/query/GetOrderQueryHandler.java
package com.example.order.application.query;

import com.example.order.application.port.inbound.GetOrderUseCase;
import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.domain.model.valueobject.OrderId;
import org.springframework.stereotype.Service;

@Service
public class GetOrderQueryHandler implements GetOrderUseCase {
    
    private final OrderRepository orderRepository;
    
    public GetOrderQueryHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    public OrderReadModel execute(GetOrderQuery query) {
        var order = orderRepository.findById(OrderId.of(query.orderId()))
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
        
        return new OrderReadModel(
            order.getOrderId().value(),
            order.getBuyer().name(),
            order.getBuyer().email(),
            order.getOrderItem().productId(),
            order.getOrderItem().productName(),
            order.getOrderItem().quantity(),
            order.getMoney().amount(),
            order.getMoney().currency(),
            order.getStatus().name(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
```

#### SAGA Orchestrator

```java
// application/saga/CreateOrderSaga.java
package com.example.order.application.saga;

import com.example.order.application.port.outbound.*;
import com.example.order.application.dto.*;
import com.example.order.domain.model.aggregate.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateOrderSaga {
    
    private static final Logger log = LoggerFactory.getLogger(CreateOrderSaga.class);
    
    private final OrderRepository orderRepository;
    private final PaymentServicePort paymentService;
    private final InventoryServicePort inventoryService;
    
    public CreateOrderSaga(
            OrderRepository orderRepository,
            PaymentServicePort paymentService,
            InventoryServicePort inventoryService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
    }
    
    public Order execute(Order order) {
        try {
            // Step 1: Save initial order
            orderRepository.save(order);
            
            // Step 2: Authorize payment
            PaymentResponse paymentResponse = authorizePayment(order);
            if (!paymentResponse.isSuccess()) {
                order.fail();
                orderRepository.save(order);
                return order;
            }
            order.markPaymentAuthorized(paymentResponse.paymentId());
            orderRepository.save(order);
            
            // Step 3: Deduct inventory
            InventoryResponse inventoryResponse = deductInventory(order);
            if (!inventoryResponse.isSuccess()) {
                compensatePayment(order);
                order.markRolledBack();
                orderRepository.save(order);
                return order;
            }
            order.markInventoryDeducted();
            orderRepository.save(order);
            
            // Step 4: Capture payment
            PaymentResponse captureResponse = capturePayment(order);
            if (!captureResponse.isSuccess()) {
                compensateInventory(order);
                compensatePayment(order);
                order.markRolledBack();
                orderRepository.save(order);
                return order;
            }
            
            // Step 5: Complete order
            order.complete();
            orderRepository.save(order);
            
            return order;
            
        } catch (Exception e) {
            log.error("SAGA execution failed for order: {}", order.getOrderId(), e);
            handleSagaFailure(order);
            return order;
        }
    }
    
    private PaymentResponse authorizePayment(Order order) {
        try {
            return paymentService.authorize(new PaymentRequest(
                order.getOrderId().value(),
                order.getMoney().amount(),
                order.getMoney().currency(),
                order.getPaymentInfo().cardNumber(),
                order.getPaymentInfo().expiryDate(),
                order.getPaymentInfo().cvv()
            ));
        } catch (Exception e) {
            log.error("Payment authorization failed", e);
            return PaymentResponse.failure();
        }
    }
    
    private InventoryResponse deductInventory(Order order) {
        try {
            return inventoryService.deduct(new InventoryRequest(
                order.getOrderId().value(),
                order.getOrderItem().productId(),
                order.getOrderItem().quantity()
            ));
        } catch (Exception e) {
            log.error("Inventory deduction failed", e);
            return InventoryResponse.failure();
        }
    }
    
    private PaymentResponse capturePayment(Order order) {
        try {
            return paymentService.capture(order.getPaymentId());
        } catch (Exception e) {
            log.error("Payment capture failed", e);
            return PaymentResponse.failure();
        }
    }
    
    private void compensatePayment(Order order) {
        try {
            paymentService.voidPayment(order.getPaymentId());
        } catch (Exception e) {
            log.error("Payment compensation failed", e);
        }
    }
    
    private void compensateInventory(Order order) {
        try {
            inventoryService.rollback(new InventoryRequest(
                order.getOrderId().value(),
                order.getOrderItem().productId(),
                order.getOrderItem().quantity()
            ));
        } catch (Exception e) {
            log.error("Inventory compensation failed", e);
        }
    }
    
    private void handleSagaFailure(Order order) {
        // Compensate based on current state
        if (order.getStatus() == OrderStatus.INVENTORY_DEDUCTED) {
            compensateInventory(order);
            compensatePayment(order);
        } else if (order.getStatus() == OrderStatus.PAYMENT_AUTHORIZED) {
            compensatePayment(order);
        }
        order.markRolledBack();
        orderRepository.save(order);
    }
}
```

### 4.3 Infrastructure Layer

#### Driving Adapters (Controllers)

```java
// infrastructure/adapter/inbound/rest/OrderCommandController.java
package com.example.order.infrastructure.adapter.inbound.rest;

import com.example.order.application.port.inbound.CreateOrderUseCase;
import com.example.order.application.command.CreateOrderCommand;
import com.example.order.application.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Commands", description = "Order write operations")
public class OrderCommandController {
    
    private final CreateOrderUseCase createOrderUseCase;
    
    public OrderCommandController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }
    
    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestBody CreateOrderRequest request) {
        
        CreateOrderCommand command = new CreateOrderCommand(
            idempotencyKey,
            request.buyer().name(),
            request.buyer().email(),
            request.orderItem().productId(),
            request.orderItem().productName(),
            request.orderItem().quantity(),
            request.payment().amount(),
            request.payment().currency(),
            request.payment().method(),
            request.payment().cardNumber(),
            request.payment().expiryDate(),
            request.payment().cvv()
        );
        
        CreateOrderResponse response = createOrderUseCase.execute(command);
        
        HttpStatus status = "COMPLETED".equals(response.status()) 
            ? HttpStatus.CREATED 
            : HttpStatus.OK;
            
        return ResponseEntity.status(status).body(response);
    }
}

// infrastructure/adapter/inbound/rest/OrderQueryController.java
package com.example.order.infrastructure.adapter.inbound.rest;

import com.example.order.application.port.inbound.GetOrderUseCase;
import com.example.order.application.query.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Queries", description = "Order read operations")
public class OrderQueryController {
    
    private final GetOrderUseCase getOrderUseCase;
    
    public OrderQueryController(GetOrderUseCase getOrderUseCase) {
        this.getOrderUseCase = getOrderUseCase;
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details")
    public ResponseEntity<OrderReadModel> getOrder(@PathVariable String orderId) {
        OrderReadModel order = getOrderUseCase.execute(new GetOrderQuery(orderId));
        return ResponseEntity.ok(order);
    }
}
```

#### Driven Adapters (Repository)

```java
// infrastructure/adapter/outbound/persistence/OrderJpaEntity.java
package com.example.order.infrastructure.adapter.outbound.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", schema = "order_schema")
public class OrderJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;
    
    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;
    
    @Column(name = "buyer_name")
    private String buyerName;
    
    @Column(name = "buyer_email")
    private String buyerEmail;
    
    @Column(name = "product_id")
    private String productId;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "quantity")
    private int quantity;
    
    @Column(name = "amount")
    private BigDecimal amount;
    
    @Column(name = "currency")
    private String currency;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "card_last_four")
    private String cardLastFour;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "payment_id")
    private String paymentId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and setters
}

// infrastructure/adapter/outbound/persistence/JpaOrderRepository.java
package com.example.order.infrastructure.adapter.outbound.persistence;

import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.OrderId;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class JpaOrderRepository implements OrderRepository {
    
    private final SpringDataOrderRepository springDataRepository;
    private final OrderMapper mapper;
    
    public JpaOrderRepository(
            SpringDataOrderRepository springDataRepository,
            OrderMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }
    
    @Override
    public void save(Order order) {
        OrderJpaEntity entity = mapper.toJpaEntity(order);
        springDataRepository.save(entity);
    }
    
    @Override
    public Optional<Order> findById(OrderId orderId) {
        return springDataRepository.findByOrderId(orderId.value())
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey)
            .map(mapper::toDomain);
    }
}

// infrastructure/adapter/outbound/persistence/SpringDataOrderRepository.java
package com.example.order.infrastructure.adapter.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, Long> {
    Optional<OrderJpaEntity> findByOrderId(String orderId);
    Optional<OrderJpaEntity> findByIdempotencyKey(String idempotencyKey);
}

// infrastructure/adapter/outbound/persistence/OrderMapper.java
package com.example.order.infrastructure.adapter.outbound.persistence;

import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.*;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    
    public OrderJpaEntity toJpaEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setOrderId(order.getOrderId().value());
        entity.setIdempotencyKey(order.getIdempotencyKey());
        entity.setBuyerName(order.getBuyer().name());
        entity.setBuyerEmail(order.getBuyer().email());
        entity.setProductId(order.getOrderItem().productId());
        entity.setProductName(order.getOrderItem().productName());
        entity.setQuantity(order.getOrderItem().quantity());
        entity.setAmount(order.getMoney().amount());
        entity.setCurrency(order.getMoney().currency());
        entity.setPaymentMethod(order.getPaymentInfo().method());
        entity.setCardLastFour(maskCard(order.getPaymentInfo().cardNumber()));
        entity.setStatus(order.getStatus().name());
        entity.setPaymentId(order.getPaymentId());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        return entity;
    }
    
    public Order toDomain(OrderJpaEntity entity) {
        // Reconstruct domain object from entity
        // Use reflection or package-private factory method
        return Order.reconstitute(
            OrderId.of(entity.getOrderId()),
            entity.getIdempotencyKey(),
            new Buyer(entity.getBuyerName(), entity.getBuyerEmail()),
            new OrderItem(entity.getProductId(), entity.getProductName(), entity.getQuantity()),
            Money.of(entity.getAmount(), entity.getCurrency()),
            OrderStatus.valueOf(entity.getStatus()),
            entity.getPaymentId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
```

#### Driven Adapters (External Service)

```java
// infrastructure/adapter/outbound/external/PaymentServiceAdapter.java
package com.example.order.infrastructure.adapter.outbound.external;

import com.example.order.application.port.outbound.PaymentServicePort;
import com.example.order.application.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Component
public class PaymentServiceAdapter implements PaymentServicePort {
    
    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;
    
    public PaymentServiceAdapter(
            RestTemplate restTemplate,
            @Value("${services.payment.url}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }
    
    @Override
    public PaymentResponse authorize(PaymentRequest request) {
        String url = paymentServiceUrl + "/api/v1/payments/authorize";
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            url, request, PaymentResponse.class);
        return response.getBody();
    }
    
    @Override
    public PaymentResponse capture(String paymentId) {
        String url = paymentServiceUrl + "/api/v1/payments/" + paymentId + "/capture";
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            url, null, PaymentResponse.class);
        return response.getBody();
    }
    
    @Override
    public PaymentResponse voidPayment(String paymentId) {
        String url = paymentServiceUrl + "/api/v1/payments/" + paymentId + "/void";
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            url, null, PaymentResponse.class);
        return response.getBody();
    }
}
```

#### Bean Configuration

```java
// infrastructure/config/BeanConfiguration.java
package com.example.order.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class BeanConfiguration {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
```

---

## 5. 資料庫設計

### 5.1 Schema 隔離策略

| 微服務 | Schema 名稱 |
|--------|-------------|
| order-service | `order_schema` |
| payment-service | `payment_schema` |
| inventory-service | `inventory_schema` |

### 5.2 Order Schema

```sql
-- order-service/src/main/resources/schema.sql
CREATE SCHEMA IF NOT EXISTS order_schema;

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

### 5.3 Payment Schema

```sql
-- payment-service/src/main/resources/schema.sql
CREATE SCHEMA IF NOT EXISTS payment_schema;

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
    payment_id VARCHAR(50) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (payment_id) REFERENCES payment_schema.payments(payment_id)
);

CREATE INDEX idx_transactions_payment ON payment_schema.payment_transactions(payment_id);
```

### 5.4 Inventory Schema

```sql
-- inventory-service/src/main/resources/schema.sql
CREATE SCHEMA IF NOT EXISTS inventory_schema;

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

## 6. API 規格

### 6.1 通用規範

| 項目 | 規範 |
|------|------|
| 版本控制 | URL Path (`/api/v1/...`) |
| 內容類型 | `application/json` |
| CQRS 分離 | Command: POST, Query: GET |

### 6.2 Order Service API

#### Command: POST /api/v1/orders

**Request**
```json
{
  "buyer": { "name": "string", "email": "string" },
  "orderItem": { "productId": "string", "productName": "string", "quantity": 1 },
  "payment": {
    "method": "CREDIT_CARD",
    "amount": 35000,
    "currency": "TWD",
    "cardNumber": "string",
    "expiryDate": "string",
    "cvv": "string"
  }
}
```

#### Query: GET /api/v1/orders/{orderId}

**Response**
```json
{
  "orderId": "string",
  "buyerName": "string",
  "buyerEmail": "string",
  "productId": "string",
  "productName": "string",
  "quantity": 1,
  "amount": 35000,
  "currency": "TWD",
  "status": "COMPLETED",
  "createdAt": "string",
  "updatedAt": "string"
}
```

---

## 7. 測試策略

### 7.1 分層測試

| 層級 | 測試類型 | 工具 |
|------|----------|------|
| Domain | Unit Test | JUnit 5 |
| Application | Unit Test + Integration Test | JUnit 5 + Mockito |
| Infrastructure | Integration Test | Spring Boot Test + Testcontainers |

### 7.2 Domain Layer 測試範例

```java
// test/domain/model/aggregate/OrderTest.java
class OrderTest {
    
    @Test
    void shouldCreateOrderWithCreatedStatus() {
        Order order = Order.create(
            "idem-key-1",
            new Buyer("Rex Wang", "rex@example.com"),
            new OrderItem("PROD-001", "iPhone", 1),
            Money.of(BigDecimal.valueOf(35000), "TWD"),
            new PaymentInfo("CREDIT_CARD", "4111111111111111", "12/28", "123")
        );
        
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getDomainEvents()).hasSize(1);
        assertThat(order.getDomainEvents().get(0)).isInstanceOf(OrderCreated.class);
    }
    
    @Test
    void shouldTransitionToPaymentAuthorized() {
        Order order = createTestOrder();
        
        order.markPaymentAuthorized("PAY-001");
        
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_AUTHORIZED);
        assertThat(order.getPaymentId()).isEqualTo("PAY-001");
    }
    
    @Test
    void shouldNotAllowInvalidStateTransition() {
        Order order = createTestOrder();
        
        assertThatThrownBy(() -> order.markInventoryDeducted())
            .isInstanceOf(IllegalStateException.class);
    }
}
```

### 7.3 Cucumber Feature

```gherkin
# e2e-tests/src/test/resources/features/create-order.feature
Feature: Create Order

  Scenario: Successfully create an order
    Given a product "iPhone 17 Pro Max" with stock quantity 10
    And the acquirer will approve authorization
    And the acquirer will approve capture
    When I create an order for "Rex Wang" to buy 1 "iPhone 17 Pro Max" for 35000 TWD
    Then the order status should be "COMPLETED"
    And the product stock should be 9
    And the payment status should be "CAPTURED"

  Scenario: Order fails due to insufficient stock
    Given a product "iPhone 17 Pro Max" with stock quantity 0
    And the acquirer will approve authorization
    When I create an order for "Rex Wang" to buy 1 "iPhone 17 Pro Max" for 35000 TWD
    Then the order status should be "ROLLBACK_COMPLETED"
    And the payment status should be "VOIDED"
```

---

## 8. Gradle 設定

### 8.1 settings.gradle

```groovy
rootProject.name = 'order-system'

include 'order-service'
include 'payment-service'
include 'inventory-service'
include 'e2e-tests'
```

### 8.2 Root build.gradle

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.x' apply false
    id 'io.spring.dependency-management' version '1.1.x' apply false
    id 'org.springframework.cloud.contract' version '4.1.x' apply false
    id 'jacoco'
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'jacoco'
    
    group = 'com.example'
    version = '1.0.0-SNAPSHOT'
    
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    repositories {
        mavenCentral()
    }
    
    jacocoTestReport {
        reports {
            xml.required = true
            html.required = true
        }
    }
    
    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = 0.80
                }
            }
        }
    }
    
    test {
        useJUnitPlatform()
        finalizedBy jacocoTestReport
    }
}
```

---

## 9. 環境設定

### 9.1 application.yml

```yaml
spring:
  application:
    name: order-service

server:
  port: 8081

services:
  payment:
    url: http://localhost:8082
  inventory:
    url: http://localhost:8083
```

### 9.2 application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:orderdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

### 9.3 application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## 10. 建置與執行指令

```bash
# 建置所有模組
./gradlew build

# 執行測試
./gradlew test

# 測試覆蓋率報告
./gradlew jacocoTestReport

# 驗證覆蓋率 >= 80%
./gradlew jacocoTestCoverageVerification

# 啟動服務
./gradlew :order-service:bootRun --args='--spring.profiles.active=dev'
```

---

## 11. MVP 暫不實作項目

| 項目 | 規劃版本 |
|------|----------|
| Event Sourcing | v1.1 |
| 分散式追蹤 | v1.1 |
| API Gateway | v1.1 |
| 認證授權 | v1.1 |
| Read Model 分離儲存 | v1.2 |
