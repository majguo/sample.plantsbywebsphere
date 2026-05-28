## 2026-05-28T02:10:37Z [coordinator] t9

WARNING: Workspace Maven defaults to Java 11. Spring Boot 3 packaging and startup validated only after overriding `JAVA_HOME` to Microsoft JDK 17. Downstream backend and test execution must use JDK 17+.

## 2026-05-28T05:06:51Z [tester] t16

CRITICAL: Streaming validation uncovered a runtime defect in `src/main/java/com/ibm/websphere/samples/daytrader/streaming/StreamingHub.java`. After an SSE client disconnects, a later quote-change publish can throw `IllegalStateException` from the `emitter.completeWithError(...)` path. Repro command: `mvn -Dtest=DayTraderJourneyIntegrationTest,DayTraderStreamingIntegrationTest test`. Failing test: `DayTraderStreamingIntegrationTest.websocketEndpointReturnsRecentQuoteChangesAndMarketSummaryPayloads`.

## 2026-05-28T05:59:21Z [architect] t17

HIGH: Repair the Boot-owned conformance gaps before validation can advance. Preserve the session/auth boundary for `/rest/broadcastevents` and `/marketsummary`, move market-summary publication cadence under `RuntimeSettingsService` / `/config` authority rather than a fixed Boot property, and remove active `javaee-api` / remaining compile-path `javax.*` dependency drift from the main build.

## 2026-05-28T05:59:21Z [coordinator] t18

CRITICAL: Security review also requires backend fixes in the same slice: operator/admin surfaces cannot remain public, anonymous quote access must follow the intended policy, and stored plaintext password / credit-card exposure paths must be removed or masked so they cannot leak through exceptions or logs.

## 2026-05-28T06:32:30Z [security] t18.1

WARNING: The prior auth-boundary and secret-handling findings are cleared, but dependency posture remains open. Active runtime still resolves Derby 10.14.2.0 below Hibernate's supported minimum, and the OWASP rerun did not leave a usable report artifact.