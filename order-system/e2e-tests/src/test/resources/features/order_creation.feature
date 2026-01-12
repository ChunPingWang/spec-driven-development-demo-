# language: zh-TW
@order
功能: 訂單建立流程
  作為一個電商平台
  我希望能處理客戶的訂單
  以便完成商品銷售

  背景:
    假設 系統中有商品 "IPHONE-17" 庫存為 10 件
    並且 支付服務正常運作

  @positive @happy-path
  場景: 成功建立訂單
    假設 買家 "王小明" 的電子郵件為 "ming@example.com"
    並且 購買商品 "IPHONE-17" 數量為 1 件
    並且 使用信用卡 "4111111111111111" 有效期 "12/26" CVV "123" 支付 35900 元
    當 買家提交訂單
    那麼 訂單應建立成功
    並且 訂單狀態應為 "COMPLETED"
    並且 支付狀態應為 "CAPTURED"
    並且 庫存應扣減至 9 件

  @positive
  場景: 多件商品訂單成功建立
    假設 買家 "李四" 的電子郵件為 "li@example.com"
    並且 購買商品 "IPHONE-17" 數量為 3 件
    並且 使用信用卡 "4111111111111111" 有效期 "12/26" CVV "123" 支付 107700 元
    當 買家提交訂單
    那麼 訂單應建立成功
    並且 訂單狀態應為 "COMPLETED"
    並且 庫存應扣減至 7 件

  @negative @payment-failure
  場景: 支付授權失敗時訂單應標記為失敗
    假設 買家 "張三" 的電子郵件為 "zhang@example.com"
    並且 購買商品 "IPHONE-17" 數量為 1 件
    並且 使用信用卡 "4000000000000002" 有效期 "12/26" CVV "123" 支付 35900 元
    並且 支付授權將會失敗並返回 "Card declined"
    當 買家提交訂單
    那麼 訂單應建立失敗
    並且 訂單狀態應為 "FAILED"
    並且 失敗原因應包含 "Card declined"
    並且 庫存應維持為 10 件

  @negative @insufficient-stock
  場景: 庫存不足時訂單應失敗並取消支付
    假設 買家 "王小明" 的電子郵件為 "ming@example.com"
    並且 購買商品 "IPHONE-17" 數量為 15 件
    並且 使用信用卡 "4111111111111111" 有效期 "12/26" CVV "123" 支付 538500 元
    當 買家提交訂單
    那麼 訂單應建立失敗
    並且 訂單狀態應為 "ROLLED_BACK"
    並且 失敗原因應包含 "Insufficient stock"
    並且 支付授權應已取消
    並且 庫存應維持為 10 件

  @negative @capture-failure
  場景: 請款失敗時應執行補償交易
    假設 買家 "陳大文" 的電子郵件為 "chen@example.com"
    並且 購買商品 "IPHONE-17" 數量為 1 件
    並且 使用信用卡 "4111111111111111" 有效期 "12/26" CVV "123" 支付 35900 元
    並且 請款將會失敗並返回 "Capture timeout"
    當 買家提交訂單
    那麼 訂單應建立失敗
    並且 訂單狀態應為 "ROLLED_BACK"
    並且 失敗原因應包含 "Capture timeout"
    並且 庫存應已回滾至 10 件
    並且 支付授權應已取消

  @negative @invalid-card
  場景: 無效信用卡格式應返回驗證錯誤
    假設 買家 "測試用戶" 的電子郵件為 "test@example.com"
    並且 購買商品 "IPHONE-17" 數量為 1 件
    並且 使用信用卡 "1234" 有效期 "12/26" CVV "123" 支付 35900 元
    當 買家提交訂單
    那麼 應返回驗證錯誤
    並且 錯誤訊息應包含 "Invalid card number"

  @negative @product-not-found
  場景: 商品不存在時應返回錯誤
    假設 買家 "王小明" 的電子郵件為 "ming@example.com"
    並且 購買商品 "NON-EXISTENT" 數量為 1 件
    並且 使用信用卡 "4111111111111111" 有效期 "12/26" CVV "123" 支付 35900 元
    當 買家提交訂單
    那麼 訂單應建立失敗
    並且 失敗原因應包含 "Product not found"
