# language: zh-TW
@inventory
功能: 庫存管理流程
  作為一個庫存系統
  我希望能正確管理商品庫存
  以便確保訂單履行的準確性

  背景:
    假設 商品 "IPHONE-17" 名稱為 "iPhone 17 Pro Max" 初始庫存為 100 件

  @positive
  場景: 成功扣減庫存
    當 為訂單 "ORD-12345678" 扣減商品 "IPHONE-17" 數量 5 件
    那麼 扣減應成功
    且 商品 "IPHONE-17" 庫存應為 95 件

  @positive
  場景: 成功回滾庫存
    假設 已為訂單 "ORD-12345678" 扣減商品 "IPHONE-17" 數量 5 件
    當 為訂單 "ORD-12345678" 回滾商品 "IPHONE-17" 數量 5 件
    那麼 回滾應成功
    且 商品 "IPHONE-17" 庫存應為 100 件

  @negative
  場景: 庫存不足時扣減失敗
    當 為訂單 "ORD-99999999" 扣減商品 "IPHONE-17" 數量 150 件
    那麼 扣減應失敗
    且 失敗原因應包含 "Insufficient stock"
    且 商品 "IPHONE-17" 庫存應維持 100 件

  @negative
  場景: 商品不存在時扣減失敗
    當 為訂單 "ORD-12345678" 扣減商品 "NON-EXISTENT" 數量 1 件
    那麼 扣減應失敗
    且 失敗原因應包含 "Product not found"

  @positive @idempotent
  場景: 重複扣減請求應具備冪等性
    當 為訂單 "ORD-IDEM0001" 扣減商品 "IPHONE-17" 數量 2 件
    且 再次為訂單 "ORD-IDEM0001" 扣減商品 "IPHONE-17" 數量 2 件
    那麼 兩次扣減都應成功
    且 商品 "IPHONE-17" 庫存應為 98 件
    且 只應有一筆扣減記錄

  @positive @concurrent
  場景: 並發扣減應正確處理
    當 同時有 3 個訂單各扣減商品 "IPHONE-17" 數量 10 件
    那麼 所有扣減都應成功
    且 商品 "IPHONE-17" 庫存應為 70 件
