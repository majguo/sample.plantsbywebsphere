# Streaming Auth Boundary Must Be Explicit

Boot-hosted SSE and raw WebSocket endpoints do not inherit DayTrader session-auth parity unless the rewrite adds an explicit session/security gate.

## What Happened
During `sample.daytrader8/t17`, the Spring Boot streaming adapters looked structurally correct but
were still publicly reachable because there was no Boot security layer and the SSE/WebSocket
handlers did not inspect `uidBean` or any authenticated principal.

## Takeaway
Future DayTrader review and implementation tasks must treat streaming auth as a first-class
compatibility contract. Preserving MVC login/logout is not enough; SSE subscriptions and WebSocket
handshakes need the same explicit session boundary.

## History
- 2026-05-28 (sample.daytrader8/t17): initial