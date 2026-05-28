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

## 2026-05-28T07:06:20Z [tester] t20.1

CRITICAL: The Spring Boot 3 primary infra lane is not runnable. Exact failing command: `$env:JAVA_HOME = "$env:USERPROFILE\\scoop\\apps\\microsoft17-jdk\\current"; $env:Path = "$env:JAVA_HOME\\bin;" + $env:Path; $env:SPRING_DATASOURCE_URL = 'jdbc:db2://127.0.0.1:50000/tradedb'; $env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'com.ibm.db2.jcc.DB2Driver'; $env:SPRING_DATASOURCE_USERNAME = 'db2inst1'; $env:SPRING_DATASOURCE_PASSWORD = 'password'; java -jar target\\io.openliberty.sample.daytrader8.war --server.port=19095` -> `Cannot load driver class: com.ibm.db2.jcc.DB2Driver`. Expected: packaged Boot WAR starts against the canonical DB2 target. Actual: application context fails during datasource/Flyway creation before readiness.

## 2026-05-28T07:21:55Z [devops] t20.3

HIGH: The DB2 primary infra lane is now reproducible and the packaged WAR reaches real DB2 connectivity, but Boot startup still fails in Flyway with duplicate version `1` migrations from `src/main/resources/db/migration/common/V1__baseline.sql` and `src/main/resources/db/migration/db2/V1__baseline.sql`. Evidence and commands are recorded in `.github/modernize/rearchitecture/artifacts/t20.3-devops.md`.

## 2026-05-28T07:40:46Z [tester] t20.4

CRITICAL: Fresh DB2 primary lane boots but cannot reach parity because the preserved seed/bootstrap path is broken. `GET /daytrader/configure.html` is public (`200`), but its linked `config?action=buildDB` returns `401`, so `uid:0 / xxx` never exists and `/rest/quotes/s:1` returns `[null]`. Please restore a usable DB2 seed-data/bootstrap path, then tester can rerun the same DB2-backed browser lane.

## 2026-05-28T08:31:51Z [tester] t21.2

CRITICAL: Seeded DB2 runtime still fails parity on `/jaxrs/sync/echoText`, `/jaxrs/sync/echoJSON`, and `/jaxrs/sync/echoXML` with `404` responses. The scenario driver actions `l`, `r`, `h`, `a`, `u`, `q`, `p`, `b`, and `s` all fall back to the login surface. `config?action=buildDBTables` returns `401` after canonical operator login, and `config?action=resetTrade` reports success on the wrong surface.

## 2026-05-28T09:57:22Z [tester] t21.4

CRITICAL: Seeded DB2 runtime still has 33 live parity failures: 2 documentation assets return `404` (`docs/tradeTech.pdf`, `docs/tradeUML.pdf`), 30 primitive launcher destinations return `404`, and image-mode `GET /app?action=home` returns `500` through the `tradehomeImg.jsp` -> `marketSummary.jsp` nested include path.