## 2026-05-27T06:41:26Z [teamlead] t1

INFO: Constitution v1.0.0 is adopted for the DayTrader8 migration. Downstream work must treat this effort as a Spring Boot 3 rewrite with strict parity requirements, Jakarta-only forward development, same-schema/same-auth preservation, and evidence-backed acceptance.

## 2026-05-27T06:51:40Z [architect] t2

INFO: Use `TradeServices` as the canonical migration seam for DayTrader8. Preserve servlet, JSF, REST, and WebSocket contracts above it; replace Liberty, EJB, JMS/MDB, CDI eventing, scheduling, and resource wiring beneath it in the Spring Boot 3 design.

## 2026-05-27T06:59:32Z [architect] t2.1

INFO: t2.1 fixes the Spring Boot 3 target around explicit runtime contracts: one application-service seam above trading logic, session-auth compatibility, explicit Sync/Async/Async_2-Phase order semantics, unified SSE/WebSocket event fan-out, one canonical transactional write model, and one authoritative Boot configuration model.

## 2026-05-27T07:11:52Z [architect] t2.1

INFO: t2.1 fixes the Spring Boot 3 target around explicit runtime contracts. Downstream design must preserve one TradeServices-centered application-service seam, one auth/session boundary, explicit Sync/Async/Async_2-Phase order semantics, one canonical event fan-out path for SSE/WebSocket updates, one transactional write model beneath the seam, and one authoritative Boot configuration model.

## 2026-05-28T07:06:20Z [tester] t20.1

WARNING: The container-backed critical path required by t6 remains unverified. Browser parity is still proven only on the embedded-Derby path from t20; the real-infra lane for sign-off remains open. See `.github/modernize/rearchitecture/artifacts/t20.1-tester.md`.

## 2026-05-28T08:31:51Z [tester] t21.2

CRITICAL: The remaining parity gaps are still live on the seeded DB2 runtime. REQ-019, REQ-020, REQ-021, REQ-025, and REQ-029 remain unproven due to 404 JAX-RS echo endpoints, scenario-driver actions collapsing to login, and broken `buildDBTables` / `resetTrade` operator flows.

## 2026-05-28T09:57:22Z [tester] t21.4

WARNING: Direct JSF/XHTML alternate pages passed, but the docs, primitive, and image inventories still contain concrete runtime failures. PM sign-off should remain blocked until those surface proofs are closed or explicitly restated.

## 2026-05-27T07:30:05Z [teamlead] t4.1

INFO: Readiness evidence for DayTrader8 is now governed as staged states: build-ready, startup-ready, surface-ready, journey-ready, and release-ready. Downstream planning and validation must treat missing evidence as FAIL, must disclose per-capability fallbacks explicitly, and must not use startup proof as parity proof.