建立開發準則，專注在程式碼品質，測試標準，測試驅動開發，行為驅動開發，領域驅動設計，遵守 SOLID
  原則，採用六角形架構，任何框架都應在外圈的 infrastructure 層 ， infrastructure 層可以直接使用 application 層 與 domain 層，但是  application 層 與 domain 層 需透過 interface 才能使用 infrastructure，遵守分層原則與反相相依原則，內圈與外圈的資料傳遞需透過 mapper 轉換
