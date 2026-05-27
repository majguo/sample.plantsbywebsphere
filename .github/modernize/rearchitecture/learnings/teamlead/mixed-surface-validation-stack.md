# Mixed Surface Validation Stack

Use a layered validation stack for DayTrader8 because parity spans HTML, streaming, operator flows, and async order semantics.

## What Happened
For sample.daytrader8 task t6, the repo facts and upstream artifacts showed a mixed-surface Spring
Boot 3 rewrite with no existing automated parity tests. The strategy had to define one validation
stack that covers session-auth HTML flows, REST, SSE, WebSocket, operator/config actions, and
mode-dependent async order behavior without assuming any one tool could prove all of it.

## Takeaway
Use MockMvc-centered Spring integration tests for HTML and REST contracts, dedicated streaming
tests for SSE/WebSocket behavior, Playwright for browser-visible parity, and Docker-backed real
infrastructure for the authoritative integration lane. Treat JMeter plans as supplemental
performance evidence only.

## History
- 2026-05-27 (sample.daytrader8/t6): initial