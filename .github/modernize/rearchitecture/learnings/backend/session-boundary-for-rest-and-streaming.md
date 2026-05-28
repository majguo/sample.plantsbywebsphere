# Session boundary for REST and streaming

Keep DayTrader's Boot-owned REST, SSE, and raw WebSocket surfaces behind the same `uidBean` session boundary, with operator-only routes layered on top where needed.

## What Happened
In sample.daytrader8 task t17.1, the review findings centered on whether the migrated app still enforced one coherent authenticated boundary across `/config`, `/rest/quotes`, `/rest/broadcastevents`, and `/marketsummary`. The stable Boot shape was not full Spring Security adoption; it was one explicit `CompatibilitySessionFacade`-based boundary reused in MVC interceptors for HTTP routes and in the WebSocket handshake interceptor for `/marketsummary`, while keeping same-origin checks local to the handshake.

## Takeaway
For this repo, preserve auth/session parity by treating `uidBean` as the authoritative compatibility marker until a broader security redesign is approved. Use MVC interceptors for HTTP controllers, reuse the same session facade in handshake validation, and lock the behavior down with focused anonymous/non-operator/cross-origin regression tests.

## History
- 2026-05-28 (sample.daytrader8/t17.1): initial