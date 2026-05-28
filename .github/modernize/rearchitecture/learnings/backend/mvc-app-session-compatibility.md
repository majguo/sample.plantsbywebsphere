# MVC app session compatibility

When migrating DayTrader `/app` flows to Spring Boot, keep the JSP contract behind a Spring MVC controller plus one explicit session facade instead of trying to reuse the legacy CDI servlet action layer.

## What Happened
In sample.daytrader8 task t11, the migrated business seam already existed, but the authenticated web flow still depended on `TradeAppServlet` and `TradeServletAction`, which were tied to CDI-style `Instance<TradeServices>` lookup and direct servlet request mutation. The stable Spring Boot 3 path was a Boot-owned `/app` controller that forwards to the existing JSPs selected by `TradeConfig.getPage(...)`, while a small `CompatibilitySessionFacade` owns `uidBean`, `sessionCreationDate`, and logout invalidation.

In sample.daytrader8 task t12, the same controller pattern scaled cleanly to the remaining trading actions. `quotes`, `buy`, `sell`, and `mksummary` could stay on `TradeAppCompatibilityController` by mapping the legacy action names to the canonical `TradeServices` methods and by populating the JSP-visible request attributes (`quoteDataBeans`, `orderData`, `results`, `closedOrders`, `marketSummaryData`) directly in MVC.

## Takeaway
For MVC/JSP parity tasks, preserve the page contract and request/session attribute names first. Keep session compatibility as an explicit web-layer dependency, extend the Boot controller action-by-action instead of reactivating the legacy servlet classes, and let the Boot controller own closed-order alert model population for the pages it renders.

## History
- 2026-05-28 (sample.daytrader8/t11): initial
- 2026-05-28 (sample.daytrader8/t12): extended the same `/app` controller pattern to quotes, buy, sell, and market-summary actions, including controller-owned `closedOrders` population