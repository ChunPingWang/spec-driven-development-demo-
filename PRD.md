# 微服務訂購系統 - 產品需求文件 (PRD)

## 文件資訊

| 項目 | 內容 |
|------|------|
| 文件版本 | 2.0 |
| 建立日期 | 2026-01-11 |
| 專案名稱 | 微服務訂購系統 MVP |
| 架構模式 | DDD + Hexagonal Architecture + CQRS |

---

## 1. 產品概述

### 1.1 產品簡介

本系統為一個基於微服務架構的訂購服務平台，包含訂單管理、支付處理、庫存管理三大核心模組。系統採用 Orchestration SAGA 模式處理分散式交易，確保跨服務的資料一致性。

### 1.2 架構設計理念

本系統採用以下設計模式：

| 設計模式 | 目的 |
|----------|------|
| Domain-Driven Design (DDD) | 以領域為核心，將業務邏輯封裝於領域層 |
| Hexagonal Architecture | 隔離核心業務與技術實作，提升可測試性與可替換性 |
| CQRS | 讀寫分離，優化查詢效能與命令處理 |

### 1.3 目標用戶

外部系統（B2B 介接）

### 1.4 MVP 範圍

實現完整的商品購買流程，包含：
- 訂單建立（Command）
- 訂單查詢（Query）
- 信用卡兩階段支付（預扣款 + 確認扣款）
- 庫存扣減
- 失敗時的補償機制

---

## 2. 領域模型

### 2.1 Bounded Context 識別

本系統識別出三個 Bounded Context，各自對應一個微服務：

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Order System                                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│  │  Order Context  │  │ Payment Context │  │Inventory Context│     │
│  │                 │  │                 │  │                 │     │
│  │  • Order        │  │  • Payment      │  │  • Product      │     │
│  │  • OrderItem    │  │  • Transaction  │  │  • StockEntry   │     │
│  │  • Buyer        │  │  • CardInfo     │  │  • InventoryLog │     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 Context Map

```
┌─────────────────┐         ┌─────────────────┐
│  Order Context  │────────▶│ Payment Context │
│   (Upstream)    │   ACL   │  (Downstream)   │
└─────────────────┘         └─────────────────┘
        │
        │ ACL
        ▼
┌─────────────────┐
│Inventory Context│
│  (Downstream)   │
└─────────────────┘

關係類型：
Order → Payment: Customer-Supplier (訂單為客戶，支付為供應商)
Order → Inventory: Customer-Supplier (訂單為客戶，庫存為供應商)
整合模式：Anti-Corruption Layer (ACL) 防止外部概念污染領域模型
```

---

## 3. Order Context 領域模型

### 3.1 Aggregate

**Order Aggregate（訂單聚合根）**

Order 是此 Context 的聚合根，負責維護訂單的一致性邊界。

### 3.2 Entity

| Entity | 說明 |
|--------|------|
| Order | 訂單聚合根，包含訂單狀態與購買資訊 |

### 3.3 Value Object

| Value Object | 說明 |
|--------------|------|
| OrderId | 訂單識別碼 |
| Buyer | 購買人資訊（姓名、Email） |
| OrderItem | 訂購商品項目（商品代碼、名稱、數量） |
| Money | 金額（數值 + 幣別） |
| PaymentInfo | 支付資訊（方式、卡號等） |

### 3.4 Domain Event

| Event | 說明 | 觸發時機 |
|-------|------|----------|
| OrderCreated | 訂單已建立 | 訂單初始建立時 |
| PaymentAuthorized | 支付已授權 | 預扣款成功時 |
| InventoryDeducted | 庫存已扣減 | 庫存扣減成功時 |
| OrderCompleted | 訂單已完成 | Capture 成功時 |
| OrderFailed | 訂單已失敗 | 任一環節失敗時 |
| OrderRolledBack | 訂單已回滾 | 補償完成時 |

### 3.5 Order 狀態機

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

---

## 4. Payment Context 領域模型

### 4.1 Aggregate

**Payment Aggregate（支付聚合根）**

### 4.2 Entity

| Entity | 說明 |
|--------|------|
| Payment | 支付聚合根，管理單筆支付的生命週期 |
| PaymentTransaction | 支付交易記錄 |

### 4.3 Value Object

| Value Object | 說明 |
|--------------|------|
| PaymentId | 支付識別碼 |
| Money | 金額 |
| CardInfo | 信用卡資訊（脫敏） |
| AuthorizationCode | 授權碼 |

### 4.4 Domain Event

| Event | 說明 |
|-------|------|
| PaymentAuthorized | 預扣款成功 |
| PaymentAuthorizationFailed | 預扣款失敗 |
| PaymentCaptured | 確認扣款成功 |
| PaymentCaptureFailed | 確認扣款失敗 |
| PaymentVoided | 授權已取消 |

### 4.5 Payment 狀態機

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

---

## 5. Inventory Context 領域模型

### 5.1 Aggregate

**Product Aggregate（商品聚合根）**

### 5.2 Entity

| Entity | 說明 |
|--------|------|
| Product | 商品聚合根，管理商品與庫存 |

### 5.3 Value Object

| Value Object | 說明 |
|--------------|------|
| ProductId | 商品識別碼 |
| StockQuantity | 庫存數量 |
| InventoryOperation | 庫存操作（扣減/回滾） |

### 5.4 Domain Event

| Event | 說明 |
|-------|------|
| StockDeducted | 庫存已扣減 |
| StockDeductionFailed | 庫存扣減失敗 |
| StockRolledBack | 庫存已回滾 |

---

## 6. 業務流程

### 6.1 CQRS 命令與查詢分離

#### Command（命令 - 寫入操作）

| Command | 說明 | 所屬 Context |
|---------|------|--------------|
| CreateOrderCommand | 建立訂單 | Order |
| AuthorizePaymentCommand | 預扣款 | Payment |
| CapturePaymentCommand | 確認扣款 | Payment |
| VoidPaymentCommand | 取消授權 | Payment |
| DeductStockCommand | 扣減庫存 | Inventory |
| RollbackStockCommand | 回滾庫存 | Inventory |

#### Query（查詢 - 讀取操作）

| Query | 說明 | 所屬 Context |
|-------|------|--------------|
| GetOrderQuery | 查詢訂單詳情 | Order |
| GetPaymentQuery | 查詢支付狀態 | Payment |
| GetStockQuery | 查詢庫存數量 | Inventory |

### 6.2 核心購買流程（Use Case）

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ 外部系統  │────▶│  訂單微服務   │────▶│  支付微服務   │────▶│   收單行     │
└──────────┘     └──────────────┘     └──────────────┘     └──────────────┘
                        │                                         
                        │              ┌──────────────┐           
                        └─────────────▶│  庫存微服務   │           
                                       └──────────────┘           
```

### 6.3 正常流程（Happy Path）

1. 外部系統發送 `CreateOrderCommand` 至訂單微服務
2. Order Aggregate 建立，發出 `OrderCreated` 事件，狀態為 `CREATED`
3. Application Service 透過 Payment Port 發送 `AuthorizePaymentCommand`
4. Payment Aggregate 處理預扣款，發出 `PaymentAuthorized` 事件
5. Order Aggregate 收到成功回應，發出 `PaymentAuthorized` 事件，狀態為 `PAYMENT_AUTHORIZED`
6. Application Service 透過 Inventory Port 發送 `DeductStockCommand`
7. Product Aggregate 扣減庫存，發出 `StockDeducted` 事件
8. Order Aggregate 收到成功回應，發出 `InventoryDeducted` 事件，狀態為 `INVENTORY_DEDUCTED`
9. Application Service 透過 Payment Port 發送 `CapturePaymentCommand`
10. Payment Aggregate 確認扣款，發出 `PaymentCaptured` 事件
11. Order Aggregate 收到成功回應，發出 `OrderCompleted` 事件，狀態為 `COMPLETED`
12. 回覆外部系統「訂購成功」

### 6.4 異常流程與補償機制

#### 場景 A：預扣款失敗

| 步驟 | 動作 | Domain Event |
|------|------|--------------|
| 1 | Payment Aggregate 回覆失敗 | PaymentAuthorizationFailed |
| 2 | Order Aggregate 更新狀態 | OrderFailed |
| 3 | 回覆外部系統「訂購失敗」 | - |

#### 場景 B：庫存扣減失敗

| 步驟 | 動作 | Domain Event |
|------|------|--------------|
| 1 | Product Aggregate 回覆失敗 | StockDeductionFailed |
| 2 | Application Service 發送 VoidPaymentCommand | - |
| 3 | Payment Aggregate 取消授權 | PaymentVoided |
| 4 | Order Aggregate 更新狀態 | OrderRolledBack |
| 5 | 回覆外部系統「訂購失敗」 | - |

#### 場景 C：確認扣款（Capture）失敗

| 步驟 | 動作 | Domain Event |
|------|------|--------------|
| 1 | Payment Aggregate 回覆 Capture 失敗 | PaymentCaptureFailed |
| 2 | Application Service 發送 RollbackStockCommand | - |
| 3 | Product Aggregate 回滾庫存 | StockRolledBack |
| 4 | Application Service 發送 VoidPaymentCommand | - |
| 5 | Payment Aggregate 取消授權 | PaymentVoided |
| 6 | Order Aggregate 更新狀態 | OrderRolledBack |
| 7 | 回覆外部系統「訂購失敗」 | - |

#### 場景 D：服務超時或不可用

任一微服務呼叫發生超時或連線失敗時，視同該步驟失敗，立即執行對應的補償流程。

---

## 7. MVP 使用案例

### 7.1 案例資料

| 項目 | 內容 |
|------|------|
| 購買人 | Rex Wang |
| 商品名稱 | iPhone 17 Pro Max |
| 商品金額 | 35,000 新台幣 |
| 付款方式 | 信用卡 |

### 7.2 Command 輸入範例

```json
{
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
}
```

### 7.3 成功回應範例

```json
{
  "orderId": "ORD-20260111-001",
  "status": "COMPLETED",
  "message": "訂購成功",
  "createdAt": "2026-01-11T10:30:00Z"
}
```

### 7.4 Query 回應範例

```json
{
  "orderId": "ORD-20260111-001",
  "buyer": {
    "name": "Rex Wang",
    "email": "rex.wang@example.com"
  },
  "orderItem": {
    "productId": "IPHONE-17-PRO-MAX",
    "productName": "iPhone 17 Pro Max",
    "quantity": 1
  },
  "money": {
    "amount": 35000,
    "currency": "TWD"
  },
  "status": "COMPLETED",
  "createdAt": "2026-01-11T10:30:00Z",
  "updatedAt": "2026-01-11T10:30:05Z"
}
```

---

## 8. 微服務職責（以 Hexagonal 視角）

### 8.1 訂單微服務（Order Service）

| 層級 | 職責 |
|------|------|
| Domain | Order Aggregate、領域事件、業務規則 |
| Application | CreateOrderUseCase、GetOrderUseCase、SAGA 編排 |
| Infrastructure | REST Controller、JPA Repository、Payment/Inventory Client |

### 8.2 支付微服務（Payment Service）

| 層級 | 職責 |
|------|------|
| Domain | Payment Aggregate、交易狀態機、領域事件 |
| Application | AuthorizeUseCase、CaptureUseCase、VoidUseCase |
| Infrastructure | REST Controller、JPA Repository、Acquirer Adapter |

### 8.3 庫存微服務（Inventory Service）

| 層級 | 職責 |
|------|------|
| Domain | Product Aggregate、庫存規則、領域事件 |
| Application | DeductStockUseCase、RollbackStockUseCase、GetStockUseCase |
| Infrastructure | REST Controller、JPA Repository |

---

## 9. 冪等性設計

### 9.1 設計原則

所有 Command 皆需支援冪等性，透過 Idempotency Key 識別重複請求。

### 9.2 實作方式

| 微服務 | Idempotency Key | 說明 |
|--------|-----------------|------|
| 訂單微服務 | Request Header: `X-Idempotency-Key` | 由外部系統提供 |
| 支付微服務 | `orderId` | 同一訂單僅能有一筆預扣款 |
| 庫存微服務 | `orderId` | 同一訂單僅能扣減一次庫存 |

---

## 10. 錯誤處理

### 10.1 錯誤回應

MVP 階段，錯誤回應不區分細部原因，統一回傳：
- 支付失敗：「支付失敗」
- 庫存失敗：「庫存扣減失敗」

### 10.2 超時處理

服務呼叫超時或不可用時，視同失敗，立即執行補償流程。

---

## 11. 非功能性需求

### 11.1 MVP 階段暫不實作

| 項目 | 說明 |
|------|------|
| 認證授權 | 後續版本加入 |
| API Gateway | 後續版本加入 |
| 分散式追蹤 | 後續版本加入 |
| Event Sourcing | 後續版本加入（目前僅 CQRS） |

### 11.2 MVP 階段實作

| 項目 | 說明 |
|------|------|
| DDD | 完整領域模型 |
| Hexagonal Architecture | Ports & Adapters |
| CQRS | Command/Query 分離 |
| 冪等性 | 所有 Command 支援 |
| 補償機制 | 完整實作 |

---

## 12. 術語定義

| 術語 | 說明 |
|------|------|
| Aggregate | 聚合，一組相關物件的集合，由聚合根管理一致性 |
| Aggregate Root | 聚合根，外部只能透過它存取聚合內的物件 |
| Entity | 實體，具有唯一識別的物件 |
| Value Object | 值物件，由屬性定義，不具唯一識別 |
| Domain Event | 領域事件，表示領域中發生的重要事實 |
| Bounded Context | 限界上下文，領域模型的邊界 |
| Port | 埠，定義應用程式與外部互動的介面 |
| Adapter | 適配器，實作 Port 的具體技術方案 |
| Command | 命令，表示改變狀態的意圖 |
| Query | 查詢，表示讀取資料的請求 |
| Use Case | 使用案例，Application Layer 的服務，協調領域物件完成業務流程 |
| ACL | Anti-Corruption Layer，防腐層，隔離外部系統概念 |
