# Boot streaming and order work

Spring Boot 3 parity for DayTrader streaming and async orders is simplest when SSE and raw WebSocket share one payload hub and async completion uses one additive durable handoff table keyed through `KEYGENEJB`.

## What Happened
During `sample.daytrader8/t14`, the canonical Boot seam already owned quote and order writes, but REST/SSE/WebSocket surfaces still lived in legacy Java EE adapters and async order modes still failed fast. The stable replacement was to keep the request transaction behavior in `TradeOrderApplicationService`, persist async handoff rows in `ORDERWORKEJB`, and let a single Boot scheduler complete those orders later. A shared `StreamingHub` generated both the SSE array payload used by `quotes.html` and the flattened WebSocket payloads used by `marketSummary.html`, which avoided two competing serializers for the same market data.

During `sample.daytrader8/t16.1`, the tester harness exposed a follow-on defect in SSE disconnect handling. Once an SSE client had already aborted, a later quote publish could hit `IOException` from `SseEmitter.send(...)`, and calling `completeWithError(...)` from that path re-threw `IllegalStateException` on the application thread. The correct fix within the shared hub was to treat send failure as terminal disconnect cleanup: remove the emitter, attempt best-effort `complete()`, swallow `IllegalStateException`, and continue publishing to WebSocket listeners.

## Takeaway
For later DayTrader Boot slices, keep streaming payload formatting centralized and treat async order completion as persisted work owned by the application seam, not by ad hoc executors or controller logic. Additive queue tables plus `KEYGENEJB` allocation fit the existing Derby/DB2 schema strategy better than broker introduction or identity-column drift.

For SSE in this Boot/Tomcat runtime, disconnected clients should be handled as idempotent cleanup, not as a second application-level error. If `SseEmitter.send(...)` fails after the client is gone, drop the emitter and avoid `completeWithError(...)` from that path.

## History
- 2026-05-28 (sample.daytrader8/t14): initial
- 2026-05-28 (sample.daytrader8/t16.1): added SSE disconnect cleanup rule for shared hub publication