# 微服務訂單系統 MVP

基於 SAGA 編排模式的微服務訂單系統，採用領域驅動設計（DDD）與六角形架構。

## 目錄

- [系統架構](#系統架構)
- [技術棧](#技術棧)
- [快速開始](#快速開始)
- [服務說明](#服務說明)
- [類別圖](#類別圖)
- [循序圖](#循序圖)
- [ER Diagram](#er-diagram)
- [API 清單](#api-清單)
- [Swagger 文件](#swagger-文件)
- [測試案例](#測試案例)
- [BDD Gherkin 規格](#bdd-gherkin-規格)
- [H2 資料庫使用教學](#h2-資料庫使用教學)

---

## 系統架構

### 整體架構

```mermaid
flowchart TB
    subgraph Client
        C[Client Application]
    end

    subgraph OrderService["Order Service :8081"]
        OC[REST Controller]
        SAGA[SAGA Orchestrator]
        OD[Order Domain]
        OR[(H2 Database)]

        OC --> SAGA
        SAGA --> OD
        OD --> OR
    end

    subgraph PaymentService["Payment Service :8082"]
        PC[REST Controller]
        PD[Payment Domain]
        PR[(H2 Database)]
        ACQ[Mock Acquirer]

        PC --> PD
        PD --> PR
        PD --> ACQ
    end

    subgraph InventoryService["Inventory Service :8083"]
        IC[REST Controller]
        ID[Product Domain]
        IR[(H2 Database)]

        IC --> ID
        ID --> IR
    end

    C -->|POST /api/v1/orders| OC
    SAGA -->|1. POST /payments/authorize| PC
    SAGA -->|2. POST /inventory/deduct| IC
    SAGA -->|3. POST /payments/capture| PC
```

### 六角形架構（單一服務）

```mermaid
flowchart LR
    subgraph Adapters_In["Inbound Adapters"]
        REST[REST Controller]
    end

    subgraph Application["Application Layer"]
        UC[Use Cases]
        CMD[Command Handlers]
        QRY[Query Handlers]
        SAGA_L[SAGA Orchestrator]
    end

    subgraph Domain["Domain Layer"]
        AGG[Aggregates]
        VO[Value Objects]
        EVT[Domain Events]
    end

    subgraph Ports["Ports"]
        IP[Inbound Ports]
        OP[Outbound Ports]
    end

    subgraph Adapters_Out["Outbound Adapters"]
        REPO[JPA Repository]
        HTTP[HTTP Client]
    end

    subgraph Infra["Infrastructure"]
        DB[(Database)]
        EXT[External Services]
    end

    REST --> IP
    IP --> UC
    UC --> CMD
    UC --> QRY
    CMD --> SAGA_L
    CMD --> AGG
    QRY --> AGG
    AGG --> VO
    AGG --> EVT
    AGG --> OP
    OP --> REPO
    OP --> HTTP
    REPO --> DB
    HTTP --> EXT
```

### SAGA 流程狀態機

```mermaid
stateDiagram-v2
    [*] --> CREATED: 建立訂單

    CREATED --> PAYMENT_AUTHORIZED: 支付授權成功
    CREATED --> FAILED: 支付授權失敗

    PAYMENT_AUTHORIZED --> INVENTORY_DEDUCTED: 庫存扣減成功
    PAYMENT_AUTHORIZED --> ROLLBACK_COMPLETED: 庫存扣減失敗<br/>→ 取消支付授權

    INVENTORY_DEDUCTED --> COMPLETED: 支付請款成功
    INVENTORY_DEDUCTED --> ROLLBACK_COMPLETED: 支付請款失敗<br/>→ 回滾庫存 + 取消支付

    COMPLETED --> [*]
    FAILED --> [*]
    ROLLBACK_COMPLETED --> [*]
```

### 架構特點

- **領域驅動設計 (DDD)**：每個服務有獨立的限界上下文
- **六角形架構**：端口與適配器模式，業務邏輯與基礎設施分離
- **SAGA 編排模式**：同步編排式分散式交易
- **CQRS**：命令與查詢責任分離

---

## 技術棧

| 類別 | 技術 |
|------|------|
| 語言 | Java 17 |
| 框架 | Spring Boot 3.2.1 |
| 建構工具 | Gradle 8.5 |
| 資料庫 | H2 (開發)、PostgreSQL (生產) |
| API 文件 | OpenAPI 3.0 / Swagger UI |
| 測試 | JUnit 5、Cucumber (BDD) |

---

## 快速開始

### 前置需求

- Java 17+
- Gradle 8.x (或使用 Gradle Wrapper)

### 啟動服務

```bash
# 進入專案目錄
cd order-system

# 編譯專案
./gradlew build -x test

# 啟動三個服務（在不同終端機）
./gradlew :inventory-service:bootRun  # Port 8083
./gradlew :payment-service:bootRun    # Port 8082
./gradlew :order-service:bootRun      # Port 8081
```

### 測試 API

```bash
# 建立訂單（成功案例）
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: test-001" \
  -d '{
    "buyer": {"name": "王小明", "email": "ming@example.com"},
    "orderItem": {"productId": "IPHONE-17-PRO-MAX", "productName": "iPhone 17 Pro Max", "quantity": 1},
    "payment": {"method": "CREDIT_CARD", "amount": 35900, "currency": "TWD", "cardNumber": "4111111111111111", "expiryDate": "12/26", "cvv": "123"}
  }'

# 預期回應
# {"orderId":"ORD-XXXXXXXX","status":"COMPLETED","message":"訂購成功","createdAt":"..."}
```

---

## 服務說明

### Order Service (Port 8081)

訂單服務，負責訂單生命週期管理與 SAGA 編排。

| 職責 | 說明 |
|------|------|
| 訂單建立 | 接收訂單請求，啟動 SAGA 流程 |
| SAGA 編排 | 協調 Payment 與 Inventory 服務 |
| 訂單查詢 | 提供訂單狀態查詢 |
| 冪等性處理 | 透過 idempotency key 避免重複建立 |

### Payment Service (Port 8082)

支付服務，負責支付授權、請款與取消。

| 職責 | 說明 |
|------|------|
| 支付授權 | 預授權扣款 |
| 支付請款 | 確認扣款 |
| 支付取消 | 取消授權（補償操作） |

### Inventory Service (Port 8083)

庫存服務，負責庫存管理。

| 職責 | 說明 |
|------|------|
| 庫存扣減 | 扣減商品庫存 |
| 庫存回滾 | 回滾已扣減庫存（補償操作） |

---

## 類別圖

### Order Service 領域模型

```mermaid
classDiagram
    class Order {
        -OrderId orderId
        -String idempotencyKey
        -Buyer buyer
        -OrderItem orderItem
        -Money money
        -PaymentInfo paymentInfo
        -OrderStatus status
        -String paymentId
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +create() Order
        +markPaymentAuthorized(paymentId)
        +markInventoryDeducted()
        +complete()
        +fail(reason)
        +markRolledBack(reason)
    }

    class OrderId {
        -String value
        +generate() OrderId
        +of(value) OrderId
    }

    class Buyer {
        -String name
        -String email
        +of(name, email) Buyer
    }

    class OrderItem {
        -String productId
        -String productName
        -int quantity
        +of(productId, productName, quantity) OrderItem
    }

    class Money {
        -BigDecimal amount
        -String currency
        +of(amount, currency) Money
    }

    class PaymentInfo {
        -String method
        -String cardNumber
        -String expiryDate
        -String cvv
        +of(method, cardNumber, expiryDate, cvv) PaymentInfo
    }

    class OrderStatus {
        <<enumeration>>
        CREATED
        PAYMENT_AUTHORIZED
        INVENTORY_DEDUCTED
        COMPLETED
        FAILED
        ROLLBACK_COMPLETED
    }

    Order --> OrderId
    Order --> Buyer
    Order --> OrderItem
    Order --> Money
    Order --> PaymentInfo
    Order --> OrderStatus
```

### Payment Service 領域模型

```mermaid
classDiagram
    class Payment {
        -PaymentId paymentId
        -String orderId
        -Money money
        -CardInfo cardInfo
        -PaymentStatus status
        -String authorizationCode
        +create() Payment
        +authorize(authCode)
        +capture()
        +voidPayment()
        +failAuthorization(reason)
    }

    class PaymentStatus {
        <<enumeration>>
        PENDING
        AUTHORIZED
        CAPTURED
        VOIDED
        FAILED
    }

    Payment --> PaymentStatus
```

### Inventory Service 領域模型

```mermaid
classDiagram
    class Product {
        -ProductId productId
        -String productName
        -StockQuantity stockQuantity
        +create() Product
        +deductStock(orderId, quantity)
        +rollbackStock(orderId, quantity)
        +hasSufficientStock(quantity) boolean
    }

    class StockQuantity {
        -int value
        +of(value) StockQuantity
        +deduct(quantity) StockQuantity
        +add(quantity) StockQuantity
        +hasSufficientStock(quantity) boolean
    }

    Product --> StockQuantity
```

---

## 循序圖

### 成功案例：建立訂單

```mermaid
sequenceDiagram
    participant C as Client
    participant O as Order Service
    participant P as Payment Service
    participant I as Inventory Service

    C->>O: POST /api/v1/orders
    O->>O: 檢查冪等性 Key
    O->>O: 建立 Order (CREATED)

    rect rgb(200, 255, 200)
        Note over O,I: SAGA 執行
        O->>P: POST /payments/authorize
        P->>P: 建立 Payment
        P->>P: 呼叫 Acquirer 授權
        P-->>O: {authorized: true, paymentId}
        O->>O: 更新 Order (PAYMENT_AUTHORIZED)

        O->>I: POST /inventory/deduct
        I->>I: 扣減庫存
        I-->>O: {success: true}
        O->>O: 更新 Order (INVENTORY_DEDUCTED)

        O->>P: POST /payments/capture
        P->>P: 請款確認
        P-->>O: {captured: true}
        O->>O: 更新 Order (COMPLETED)
    end

    O-->>C: {status: COMPLETED, message: 訂購成功}
```

### 失敗案例 1：支付授權失敗

```mermaid
sequenceDiagram
    participant C as Client
    participant O as Order Service
    participant P as Payment Service

    C->>O: POST /api/v1/orders (卡號 4000...)
    O->>O: 建立 Order (CREATED)

    rect rgb(255, 200, 200)
        Note over O,P: 支付失敗 - 無需補償
        O->>P: POST /payments/authorize
        P->>P: Acquirer 拒絕
        P-->>O: {authorized: false}
        O->>O: 更新 Order (FAILED)
    end

    O-->>C: {status: FAILED, message: 支付失敗}
```

### 失敗案例 2：庫存不足（需補償）

```mermaid
sequenceDiagram
    participant C as Client
    participant O as Order Service
    participant P as Payment Service
    participant I as Inventory Service

    C->>O: POST /api/v1/orders (quantity: 100)
    O->>O: 建立 Order (CREATED)

    O->>P: POST /payments/authorize
    P-->>O: {authorized: true, paymentId}
    O->>O: 更新 Order (PAYMENT_AUTHORIZED)

    rect rgb(255, 200, 200)
        Note over O,I: 庫存不足
        O->>I: POST /inventory/deduct
        I-->>O: {success: false, message: 庫存不足}
    end

    rect rgb(255, 255, 200)
        Note over O,P: 補償：取消支付授權
        O->>P: POST /payments/void
        P-->>O: {voided: true}
        O->>O: 更新 Order (ROLLBACK_COMPLETED)
    end

    O-->>C: {status: ROLLBACK_COMPLETED, message: 庫存扣減失敗}
```

### 失敗案例 3：請款失敗（完整補償）

```mermaid
sequenceDiagram
    participant C as Client
    participant O as Order Service
    participant P as Payment Service
    participant I as Inventory Service

    C->>O: POST /api/v1/orders
    O->>O: 建立 Order (CREATED)

    O->>P: POST /payments/authorize
    P-->>O: {authorized: true}
    O->>O: 更新 Order (PAYMENT_AUTHORIZED)

    O->>I: POST /inventory/deduct
    I-->>O: {success: true}
    O->>O: 更新 Order (INVENTORY_DEDUCTED)

    rect rgb(255, 200, 200)
        Note over O,P: 請款失敗
        O->>P: POST /payments/capture
        P-->>O: {captured: false}
    end

    rect rgb(255, 255, 200)
        Note over O,I: 完整補償
        O->>I: POST /inventory/rollback
        I-->>O: {success: true}
        O->>P: POST /payments/void
        P-->>O: {voided: true}
        O->>O: 更新 Order (ROLLBACK_COMPLETED)
    end

    O-->>C: {status: ROLLBACK_COMPLETED, message: 支付確認失敗}
```

---

## ER Diagram

### Order Service 資料模型

```mermaid
erDiagram
    ORDERS {
        bigint id PK
        varchar order_id UK
        varchar idempotency_key UK
        varchar buyer_name
        varchar buyer_email
        varchar product_id
        varchar product_name
        int quantity
        decimal amount
        varchar currency
        varchar payment_method
        varchar card_last_four
        varchar status
        varchar payment_id
        timestamp created_at
        timestamp updated_at
    }
```

### Payment Service 資料模型

```mermaid
erDiagram
    PAYMENTS {
        bigint id PK
        varchar payment_id UK
        varchar order_id UK
        decimal amount
        varchar currency
        varchar card_last_four
        varchar status
        varchar authorization_code
        timestamp created_at
        timestamp updated_at
    }

    PAYMENT_TRANSACTIONS {
        bigint id PK
        varchar payment_id FK
        varchar transaction_type
        varchar status
        text request_payload
        text response_payload
        timestamp created_at
    }

    PAYMENTS ||--o{ PAYMENT_TRANSACTIONS : has
```

### Inventory Service 資料模型

```mermaid
erDiagram
    PRODUCTS {
        bigint id PK
        varchar product_id UK
        varchar product_name
        int stock_quantity
        timestamp created_at
        timestamp updated_at
    }

    INVENTORY_LOGS {
        bigint id PK
        varchar order_id
        varchar product_id
        varchar operation_type
        int quantity
        varchar status
        timestamp created_at
    }

    PRODUCTS ||--o{ INVENTORY_LOGS : tracks
```

---

## API 清單

### Order Service API

| Method | Endpoint | 說明 |
|--------|----------|------|
| POST | `/api/v1/orders` | 建立訂單 |
| GET | `/api/v1/orders/{orderId}` | 查詢訂單 |

#### POST /api/v1/orders

**Request Headers:**
```
Content-Type: application/json
X-Idempotency-Key: {unique-key}  (選填)
```

**Request Body:**
```json
{
  "buyer": {
    "name": "王小明",
    "email": "ming@example.com"
  },
  "orderItem": {
    "productId": "IPHONE-17-PRO-MAX",
    "productName": "iPhone 17 Pro Max",
    "quantity": 1
  },
  "payment": {
    "method": "CREDIT_CARD",
    "amount": 35900,
    "currency": "TWD",
    "cardNumber": "4111111111111111",
    "expiryDate": "12/26",
    "cvv": "123"
  }
}
```

**Response (201 Created):**
```json
{
  "orderId": "ORD-A1B2C3D4",
  "status": "COMPLETED",
  "message": "訂購成功",
  "createdAt": "2026-01-12T20:50:00"
}
```

#### GET /api/v1/orders/{orderId}

**Response (200 OK):**
```json
{
  "orderId": "ORD-A1B2C3D4",
  "status": "COMPLETED",
  "buyer": {
    "name": "王小明",
    "email": "ming@example.com"
  },
  "orderItem": {
    "productId": "IPHONE-17-PRO-MAX",
    "productName": "iPhone 17 Pro Max",
    "quantity": 1
  },
  "totalAmount": {
    "amount": 35900.00,
    "currency": "TWD"
  },
  "paymentId": "PAY-X1Y2Z3W4",
  "createdAt": "2026-01-12T20:50:00",
  "updatedAt": "2026-01-12T20:50:01"
}
```

### Payment Service API

| Method | Endpoint | 說明 |
|--------|----------|------|
| POST | `/api/v1/payments/authorize` | 支付授權 |
| POST | `/api/v1/payments/capture` | 支付請款 |
| POST | `/api/v1/payments/void` | 取消授權 |

### Inventory Service API

| Method | Endpoint | 說明 |
|--------|----------|------|
| POST | `/api/v1/inventory/deduct` | 扣減庫存 |
| POST | `/api/v1/inventory/rollback` | 回滾庫存 |

---

## Swagger 文件

各服務啟動後，可透過以下網址存取 Swagger UI：

| 服務 | Swagger UI | API Docs |
|------|------------|----------|
| Order Service | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| Payment Service | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| Inventory Service | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs |

### Swagger UI 截圖範例

存取 http://localhost:8081/swagger-ui.html 可查看：
- 所有 API 端點清單
- Request/Response Schema
- 線上測試 API 功能

---

## 測試案例

### 測試卡號規則

| 卡號開頭 | 行為 |
|----------|------|
| `4111` | 授權成功 |
| `4000` | 授權失敗（餘額不足） |
| `5000` | 授權成功，請款失敗 |

### 手動測試案例

#### 案例 1：成功建立訂單
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: success-test-001" \
  -d '{
    "buyer": {"name": "測試用戶", "email": "test@example.com"},
    "orderItem": {"productId": "IPHONE-17-PRO-MAX", "productName": "iPhone 17 Pro Max", "quantity": 1},
    "payment": {"method": "CREDIT_CARD", "amount": 35900, "currency": "TWD", "cardNumber": "4111111111111111", "expiryDate": "12/26", "cvv": "123"}
  }'

# 預期：status=COMPLETED, message=訂購成功
```

#### 案例 2：支付授權失敗
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: payment-fail-001" \
  -d '{
    "buyer": {"name": "測試用戶", "email": "test@example.com"},
    "orderItem": {"productId": "IPHONE-17-PRO-MAX", "productName": "iPhone 17 Pro Max", "quantity": 1},
    "payment": {"method": "CREDIT_CARD", "amount": 35900, "currency": "TWD", "cardNumber": "4000111122223333", "expiryDate": "12/26", "cvv": "123"}
  }'

# 預期：status=FAILED, message=支付失敗
```

#### 案例 3：庫存不足
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: inventory-fail-001" \
  -d '{
    "buyer": {"name": "測試用戶", "email": "test@example.com"},
    "orderItem": {"productId": "IPHONE-17-PRO-MAX", "productName": "iPhone 17 Pro Max", "quantity": 9999},
    "payment": {"method": "CREDIT_CARD", "amount": 35900, "currency": "TWD", "cardNumber": "4111111111111111", "expiryDate": "12/26", "cvv": "123"}
  }'

# 預期：status=ROLLBACK_COMPLETED, message=庫存扣減失敗
```

#### 案例 4：冪等性測試
```bash
# 第一次請求
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: idempotency-test-001" \
  -d '{...}'

# 第二次請求（相同 idempotency key）
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: idempotency-test-001" \
  -d '{...}'

# 預期：兩次回應相同的 orderId
```

---

## BDD Gherkin 規格

### Feature: 建立訂單

```gherkin
Feature: 建立訂單
  作為一個購買者
  我想要建立訂單購買商品
  以便完成購物流程

  Background:
    Given 庫存中有商品 "IPHONE-17-PRO-MAX" 數量為 10

  Scenario: 成功建立訂單
    Given 我是一個有效的購買者 "王小明" 電子郵件 "ming@example.com"
    And 我選擇了商品 "IPHONE-17-PRO-MAX" 數量 1
    And 我使用信用卡 "4111111111111111" 支付 35900 TWD
    When 我提交訂單
    Then 訂單狀態應為 "COMPLETED"
    And 我應收到訊息 "訂購成功"
    And 庫存應減少 1

  Scenario: 支付授權失敗
    Given 我是一個有效的購買者 "李小華" 電子郵件 "hua@example.com"
    And 我選擇了商品 "IPHONE-17-PRO-MAX" 數量 1
    And 我使用被拒絕的信用卡 "4000111122223333" 支付 35900 TWD
    When 我提交訂單
    Then 訂單狀態應為 "FAILED"
    And 我應收到訊息 "支付失敗"
    And 庫存應保持不變

  Scenario: 庫存不足觸發補償
    Given 我是一個有效的購買者 "張大明" 電子郵件 "ming@example.com"
    And 我選擇了商品 "IPHONE-17-PRO-MAX" 數量 100
    And 我使用信用卡 "4111111111111111" 支付 3590000 TWD
    When 我提交訂單
    Then 訂單狀態應為 "ROLLBACK_COMPLETED"
    And 我應收到訊息 "庫存扣減失敗"
    And 支付授權應被取消
```

### Feature: 查詢訂單

```gherkin
Feature: 查詢訂單
  作為一個購買者
  我想要查詢我的訂單狀態
  以便追蹤訂單進度

  Scenario: 成功查詢訂單
    Given 存在一筆訂單 "ORD-12345678" 狀態為 "COMPLETED"
    When 我查詢訂單 "ORD-12345678"
    Then 我應看到訂單詳細資訊
    And 訂單狀態應為 "COMPLETED"

  Scenario: 查詢不存在的訂單
    When 我查詢訂單 "ORD-NOTEXIST"
    Then 我應收到 404 錯誤
    And 錯誤訊息應為 "Order not found: ORD-NOTEXIST"
```

### Feature: 冪等性處理

```gherkin
Feature: 冪等性處理
  作為系統
  我需要確保重複請求不會建立多筆訂單
  以便保持資料一致性

  Scenario: 重複請求返回相同結果
    Given 我使用冪等性金鑰 "unique-key-001"
    And 我第一次提交訂單
    And 訂單成功建立為 "ORD-XXXXXXXX"
    When 我使用相同冪等性金鑰再次提交訂單
    Then 我應收到相同的訂單編號 "ORD-XXXXXXXX"
    And 不應建立新訂單
```

---

## H2 資料庫使用教學

### 存取 H2 Console

各服務啟動後，可透過以下網址存取 H2 Console：

| 服務 | H2 Console URL |
|------|----------------|
| Order Service | http://localhost:8081/h2-console |
| Payment Service | http://localhost:8082/h2-console |
| Inventory Service | http://localhost:8083/h2-console |

### 連線設定

| 設定項目 | 值 |
|----------|-----|
| Driver Class | `org.h2.Driver` |
| JDBC URL (Order) | `jdbc:h2:mem:orderdb` |
| JDBC URL (Payment) | `jdbc:h2:mem:paymentdb` |
| JDBC URL (Inventory) | `jdbc:h2:mem:inventorydb` |
| User Name | `sa` |
| Password | (空白) |

### 常用 SQL 查詢

#### 查詢所有訂單
```sql
SELECT * FROM orders ORDER BY created_at DESC;
```

#### 查詢特定狀態的訂單
```sql
SELECT * FROM orders WHERE status = 'COMPLETED';
SELECT * FROM orders WHERE status = 'FAILED';
SELECT * FROM orders WHERE status = 'ROLLBACK_COMPLETED';
```

#### 查詢訂單與支付關聯
```sql
-- 在 Order Service H2
SELECT order_id, status, payment_id FROM orders;

-- 在 Payment Service H2
SELECT payment_id, order_id, status, authorization_code FROM payments;
```

#### 查詢庫存狀況
```sql
-- 在 Inventory Service H2
SELECT product_id, product_name, stock_quantity FROM products;

-- 查詢庫存變動記錄
SELECT * FROM inventory_logs ORDER BY created_at DESC;
```

#### 清除測試資料
```sql
-- Order Service
DELETE FROM orders;

-- Payment Service
DELETE FROM payment_transactions;
DELETE FROM payments;

-- Inventory Service
DELETE FROM inventory_logs;
UPDATE products SET stock_quantity = 10 WHERE product_id = 'IPHONE-17-PRO-MAX';
```

### H2 Console 截圖說明

1. 開啟瀏覽器，輸入 `http://localhost:8081/h2-console`
2. 在 JDBC URL 輸入 `jdbc:h2:mem:orderdb`
3. User Name 輸入 `sa`，Password 留空
4. 點擊 Connect
5. 左側會顯示資料表列表，點擊可查看結構
6. 在上方 SQL 輸入區輸入查詢語句
7. 點擊 Run 執行查詢

---

## 專案結構

```
order-system/
├── build.gradle                 # 根專案建構檔
├── settings.gradle              # 多模組設定
├── gradle.properties            # Gradle 屬性
│
├── order-service/               # 訂單服務
│   └── src/main/java/com/example/order/
│       ├── domain/              # 領域層
│       │   ├── model/           # 聚合根、值物件
│       │   ├── event/           # 領域事件
│       │   └── exception/       # 領域例外
│       ├── application/         # 應用層
│       │   ├── command/         # 命令處理器
│       │   ├── query/           # 查詢處理器
│       │   ├── saga/            # SAGA 編排器
│       │   ├── dto/             # 資料傳輸物件
│       │   └── port/            # 端口介面
│       └── infrastructure/      # 基礎設施層
│           ├── adapter/         # 適配器
│           └── config/          # 配置
│
├── payment-service/             # 支付服務
│   └── src/main/java/com/example/payment/
│       ├── domain/
│       ├── application/
│       └── infrastructure/
│
├── inventory-service/           # 庫存服務
│   └── src/main/java/com/example/inventory/
│       ├── domain/
│       ├── application/
│       └── infrastructure/
│
└── e2e-tests/                   # 端對端測試
    └── src/test/
        ├── java/                # Step Definitions
        └── resources/features/  # Gherkin Features
```

---

## 授權

MIT License
