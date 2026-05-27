# Trade Services Migration Seam

Use `TradeServices` as the canonical business seam for DayTrader8 migration work.

## What Happened

During `sample.daytrader8/t2`, the main web, JSF, REST, and WebSocket entry points all resolved a
`TradeServices` implementation through CDI and `TradeConfig` runtime mode selection. The codebase
therefore already centralizes most business behavior behind one contract even though infrastructure
concerns remain Java EE specific.

## Takeaway

Downstream design and implementation should preserve adapter contracts above `TradeServices` and
replace runtime-specific infrastructure below it. Do not anchor the rewrite on servlet classes,
JSF beans, or Liberty descriptors when a stable business-service seam already exists.

## History
- 2026-05-27 (sample.daytrader8/t2): initial