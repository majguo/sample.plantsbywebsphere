# Primitive launcher routes stay on Boot compatibility controller

When legacy primitive sources are excluded from the Boot build, restore missing launcher URLs by extending the active compatibility controller rather than re-enabling the old servlet package.

## What Happened
In sample.daytrader8 task t21.4.1, the remaining primitive launcher failures were all `404`s on `/servlet/*` and `/ejb3/*` URLs even though the static launcher page still linked to them. The controlling detail was in `pom.xml`: `**/web/prims/**` is intentionally excluded from compilation, so enabling servlet scanning or reviving the legacy `javax.servlet` classes would not have matched the current Spring Boot 3 boundary. The stable fix was to keep those URLs on `PrimitiveCompatibilityController`, add Boot-owned handlers for the missing routes, and let `PingServlet2PDF` stream a packaged web-root asset.

## Takeaway
For DayTrader Boot parity, treat primitive launcher reachability as a compatibility-adapter responsibility. If the old primitive package is outside the active build, extend the Boot MVC controller on the preserved URLs instead of trying to reactivate the original servlet/EJB/CDI implementation.

## History
- 2026-05-28 (sample.daytrader8/t21.4.1): initial