# Explicit Runtime Contracts For Boot Target

Spring Boot 3 target design must preserve session, async, event, and config contracts explicitly instead of mirroring Liberty/EJB structure.

## What Happened
During `sample.daytrader8/t2.1`, the real migration risk was not the existence of `javax.*` APIs by itself. The parity-sensitive behavior lives in concrete runtime contracts: session keys (`uidBean`, `sessionCreationDate`), deferred order modes (`Sync`, `Async`, `Async_2-Phase`), quote and market-summary push fan-out, and Liberty-owned datasource/messaging wiring.

## Takeaway
Downstream Spring Boot design should collapse legacy runtime modes behind one application-service seam, but only after those runtime contracts are written down as explicit target constraints and tests.

## History
- 2026-05-27 (sample.daytrader8/t2.1): initial
- 2026-05-27 (sample.daytrader8/t2.1): expanded into explicit downstream constraints for auth,
  async order completion, event fan-out, canonical transaction ownership, and authoritative Boot
  configuration