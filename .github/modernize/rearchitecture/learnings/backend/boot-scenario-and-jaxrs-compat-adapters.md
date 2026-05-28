# Boot scenario and JAX-RS compat adapters

For DayTrader Boot parity, keep `/jaxrs/sync/*` and `/scenario` as explicit Spring MVC adapters instead of depending on legacy `javax.ws.rs` resources or servlet dispatcher query-string behavior.

## What Happened
In sample.daytrader8 task t21.2.1, the seeded DB2 lane still failed parity even though the core `TradeServices` seam and MVC `/app` surface were already working. The remaining defects lived entirely in compatibility adapters: the primitive JAX-RS echo URLs still pointed at inactive `javax.ws.rs` classes, and the scenario driver still relied on legacy include-style dispatch with query-string parameters that did not behave reliably on the Boot stack. A small Boot MVC echo controller plus direct scenario orchestration through the Boot `/app` controller closed the gap. On seeded lanes, scenario login also needed a deterministic fallback to the canonical seeded account when the random scenario deck selected a user that was not usable on the current lane state.

## Takeaway
When Boot has already replaced the business seam, preserve these DayTrader compatibility surfaces by reimplementing the edge adapter, not by reviving the legacy programming model. For `/scenario`, treat Spring MVC controller invocation plus request-parameter overlays as the stable compatibility mechanism, and keep one canonical seeded-user fallback for login so live parity does not depend on random deck state.

## History
- 2026-05-28 (sample.daytrader8/t21.2.1): initial