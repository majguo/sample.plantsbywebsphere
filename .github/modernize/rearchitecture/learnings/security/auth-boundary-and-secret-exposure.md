# Auth boundary and secret exposure

Spring Boot migration reviews on DayTrader8 should treat missing centralized auth enforcement and legacy plaintext secret flows as separate findings, because they fail for different reasons and require different fixes.

## What Happened
During sample.daytrader8/t18, the migrated runtime kept `/app` session checks but left `/config`, REST, SSE, and WebSocket surfaces outside the same boundary. The auth path also preserved plaintext password and credit-card storage plus log and exception strings that can expose those values.

## Takeaway
Audit access-control first by comparing protected MVC behavior against admin, REST, and streaming surfaces. Audit secret handling separately by following where password and payment-like fields are stored, rendered, and logged. Do not let one finding hide the other.

## History
- 2026-05-28 (sample.daytrader8/t18): initial