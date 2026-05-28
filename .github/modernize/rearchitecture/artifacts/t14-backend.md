# t14 - REST, SSE, WebSocket, and async order completion infrastructure

## Summary

Implemented the Boot-owned protocol and async completion slice for DayTrader8. The application now serves `/rest/quotes` and `/rest/broadcastevents` through Spring MVC/SSE, serves `/marketsummary` through a raw Spring WebSocket handler, maintains a shared streaming hub for market summary and recent quote-change payloads, and replaces the t10 async fail-fast seam with a durable `ORDERWORKEJB` queue plus scheduled completion worker.

## Deliverables

- Added Boot-native REST and SSE adapters in `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/QuoteRestController.java` and `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/BroadcastEventsController.java`.
- Added raw WebSocket support for `/marketsummary` in `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/MarketSummaryWebSocketConfig.java`, `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/MarketSummaryWebSocketHandler.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/streaming/StreamingHub.java`.
- Added durable async handoff storage in `src/main/java/com/ibm/websphere/samples/daytrader/entities/OrderWorkItemEntity.java`, `src/main/java/com/ibm/websphere/samples/daytrader/persistence/jpa/OrderWorkItemJpaRepository.java`, and `src/main/resources/db/migration/common/V2__order_work_queue.sql`.
- Added the Boot-managed order completion worker in `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/OrderWorkProcessor.java`.
- Replaced async placeholders in `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/TradeOrderApplicationService.java` so async and async-2-phase order modes now persist durable work instead of failing fast.
- Converted `src/main/java/com/ibm/websphere/samples/daytrader/util/RecentQuotePriceChangeList.java` into a Spring-managed publication source for SSE/WebSocket fan-out.
- Added focused async-order coverage in `src/test/java/com/ibm/websphere/samples/daytrader/application/orders/TradeOrderApplicationServiceTest.java` and enabled Boot WebSocket/test dependencies in `pom.xml`.

## Decisions

- Used one additive `ORDERWORKEJB` table plus `KEYGENEJB` allocation for durable async handoff instead of introducing a broker or schema-breaking sequence change. This keeps t5’s same-schema guidance intact while making async completion restart-safe.
- Centralized SSE and raw WebSocket payload generation in one `StreamingHub` so `/rest/broadcastevents` and `/marketsummary` stay aligned with the legacy browser payload shapes.
- Treated `Async` and `Async_2-Phase` as the same durable worker mechanism with a persisted `twoPhase` flag, preserving the architect’s contract that both modes commit the request transaction before background completion while keeping the handoff explicit.

## Issues Found

- Runtime startup still warns that Derby `10.14` is below Hibernate’s preferred minimum `10.15.2`. Startup, migration, and packaging succeeded anyway, so this remains an environment risk rather than a blocker.

## Test Results

- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; & mvn.cmd -Dtest=TradeOrderApplicationServiceTest test`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; & mvn.cmd package`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19080`
- Passed: 12
- Failed: 0
- Skipped: 0

## Downstream Notes

- The runtime startup probe reached `Started DayTraderApplication` on port `19080` with context path `/daytrader`, and Flyway applied `V2__order_work_queue.sql` successfully against the shipped Derby database.
- The new worker is single-process and scheduler-driven. If later phases require clustered delivery guarantees, they will need an explicit claim/lease protocol rather than this single-node queue consumer.