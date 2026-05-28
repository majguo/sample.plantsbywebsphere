## 2026-05-27T06:41:26Z [teamlead] t1

INFO: Constitution v1.0.0 is adopted for the DayTrader8 migration. Downstream work must treat this effort as a Spring Boot 3 rewrite with strict parity requirements, Jakarta-only forward development, same-schema/same-auth preservation, and evidence-backed acceptance.

## 2026-05-27T06:51:40Z [architect] t2

INFO: Use `TradeServices` as the canonical migration seam for DayTrader8. Preserve servlet, JSF, REST, and WebSocket contracts above it; replace Liberty, EJB, JMS/MDB, CDI eventing, scheduling, and resource wiring beneath it in the Spring Boot 3 design.

## 2026-05-27T06:59:32Z [architect] t2.1

INFO: t2.1 fixes the Spring Boot 3 target around explicit runtime contracts: one application-service seam above trading logic, session-auth compatibility, explicit Sync/Async/Async_2-Phase order semantics, unified SSE/WebSocket event fan-out, one canonical transactional write model, and one authoritative Boot configuration model.

## 2026-05-27T07:05:21Z [devops] t2.1.1

INFO: Deterministic clean-build smoke blocker resolved. Added `scripts/smoke-clean-build.ps1` and documented the evidence in `.github/modernize/rearchitecture/artifacts/t2.1.1-devops.md`.

## 2026-05-27T07:11:52Z [architect] t2.1

INFO: t2.1 fixes the Spring Boot 3 target around explicit runtime contracts. Downstream design must preserve one TradeServices-centered application-service seam, one auth/session boundary, explicit Sync/Async/Async_2-Phase order semantics, one canonical event fan-out path for SSE/WebSocket updates, one transactional write model beneath the seam, and one authoritative Boot configuration model.

## 2026-05-27T07:30:05Z [teamlead] t4.1

INFO: Readiness evidence for DayTrader8 is now governed as staged states: build-ready, startup-ready, surface-ready, journey-ready, and release-ready. Downstream planning and validation must treat missing evidence as FAIL, must disclose per-capability fallbacks explicitly, and must not use startup proof as parity proof.

## 2026-05-28T02:10:37Z [coordinator] t9

WARNING: Workspace Maven defaults to Java 11. Spring Boot 3 packaging and startup validated only after overriding `JAVA_HOME` to Microsoft JDK 17. Downstream backend and test execution must use JDK 17+.