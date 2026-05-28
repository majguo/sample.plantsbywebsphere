## 2026-05-28T02:10:37Z [coordinator] t9

WARNING: Workspace Maven defaults to Java 11. Spring Boot 3 packaging and startup validated only after overriding `JAVA_HOME` to Microsoft JDK 17. Downstream backend and test execution must use JDK 17+.

## 2026-05-28T05:06:51Z [tester] t16

CRITICAL: Streaming validation uncovered a runtime defect in `src/main/java/com/ibm/websphere/samples/daytrader/streaming/StreamingHub.java`. After an SSE client disconnects, a later quote-change publish can throw `IllegalStateException` from the `emitter.completeWithError(...)` path. Repro command: `mvn -Dtest=DayTraderJourneyIntegrationTest,DayTraderStreamingIntegrationTest test`. Failing test: `DayTraderStreamingIntegrationTest.websocketEndpointReturnsRecentQuoteChangesAndMarketSummaryPayloads`.