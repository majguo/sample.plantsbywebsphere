# t17 - Architecture Review Against Spring Boot 3 Target Design

## Summary

The Spring Boot 3 rewrite is structurally close to the t4 target design on packaging, the canonical
`TradeServices` seam, durable async order processing, and the JSF compatibility slice. The review
does not pass yet because three target-design requirements remain violated in the implemented code:
streaming endpoints are outside the authenticated session boundary, market-summary publication
cadence is not controlled by the mutable runtime settings model, and the build still compiles a
substantial `javax.*` surface through Java EE 8 compatibility dependencies.

## Inputs Reviewed

- Target design and constraints: `t4`, `t5`, `t6`, `clarification.md`, `t1`
- Implementation evidence: `t10` through `t16`
- Source anchors reviewed directly:
  - `pom.xml`
  - `src/main/resources/application.yml`
  - `src/main/java/com/ibm/websphere/samples/daytrader/boot/DayTraderApplication.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/application/TradeServicesFacade.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/TradeOrderApplicationService.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/OrderWorkProcessor.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/BroadcastEventsController.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/MarketSummaryWebSocketConfig.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/MarketSummaryWebSocketHandler.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/streaming/StreamingHub.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/streaming/MarketSummaryPublisher.java`
  - `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/CompatibilitySessionFacade.java`

## Conformance Confirmed

### PASS - Packaging, context root, and JSF compatibility layer

- `pom.xml` packages the app as a WAR and keeps Tomcat/JoinFaces wiring aligned with the t4
  executable-WAR decision.
- `application.yml` preserves `server.port=9080`, `server.servlet.context-path=/daytrader`, and the
  JoinFaces servlet mappings required for XHTML parity.
- `DayTraderApplication` excludes the legacy `web.jsf` package from Spring scanning and keeps the
  Boot-owned `web.jsfcompat` layer as the active compatibility seam.

### PASS - Canonical application seam and persistence-owned async replacement

- `TradeServicesFacade` is the Boot-owned `TradeServices` implementation and remains the canonical
  business seam above the migration.
- The async replacement uses `TradeOrderApplicationService`, `OrderWorkProcessor`, and
  `ORDERWORKEJB` state rather than reviving Liberty JMS/MDB behavior.
- Retry state, attempt counts, and failure persistence exist in the durable work-record model,
  matching the t4/t5 direction for explicit async lifecycle ownership.

### PASS - Runtime settings authority exists for mutable operator state

- `RuntimeSettingsService` exists and is used by the Boot-owned MVC and JSF compatibility slices as
  the mutable configuration boundary above the legacy `TradeConfig` mirror.

## Findings

### HIGH - Streaming endpoints are outside the authenticated session boundary

Target requirement violated:
- t4 D4 requires SSE subscriptions and the `/marketsummary` WebSocket handshake to honor the same
  authenticated session boundary as the rest of the application.

Evidence:
- `pom.xml` has no Spring Security dependency.
- No Boot security configuration is present under `src/main/java`.
- `BroadcastEventsController.register()` accepts every request and returns an emitter without
  consulting `uidBean` or any authenticated principal.
- `MarketSummaryWebSocketConfig` exposes `/marketsummary` with `setAllowedOriginPatterns("*")`.
- `MarketSummaryWebSocketHandler.afterConnectionEstablished()` registers any session immediately.
- `StreamingHub.registerBroadcastEmitter()` and `registerMarketSummarySession()` track clients
  without session validation.

Impact:
- Anonymous clients can subscribe to streaming surfaces that the target design explicitly placed
  behind the authenticated session boundary.
- Logout invalidates the HTTP session for MVC flows, but there is no Boot security context to clear
  or enforce for streaming adapters.
- t20 cannot claim parity on auth/session behavior while streaming remains publicly reachable.

Required follow-up:
- Add an explicit Boot security/session boundary for SSE and WebSocket access, or equivalent
  interceptor/handshake enforcement tied to `CompatibilitySessionFacade` and the preserved session
  contract.

### HIGH - Market-summary publication cadence is not controlled by the runtime settings model

Target requirement violated:
- t4 D7 through D9 require market-summary update cadence to remain operator-controlled through the
  same mutable runtime settings surface exposed by `/config`.

Evidence:
- `MarketSummaryPublisher.publishSummary()` is scheduled with
  `${daytrader.streaming.market-summary-publish-delay-ms:5000}`.
- `MarketSummaryPublisher` does not depend on `RuntimeSettingsService`.
- `/config` updates `marketSummaryInterval` through `RuntimeSettingsService`, but that value only
  influences `TradeServicesFacade.getMarketSummary()` cache refresh, not the publication scheduler.

Impact:
- WebSocket push cadence is fixed at a Boot property value rather than the operator-visible runtime
  configuration contract.
- Changing `marketSummaryInterval` no longer fully governs the externally observable streaming
  behavior described in the target design.

Required follow-up:
- Derive market-summary publication timing from `RuntimeSettingsService`, or collapse publication to
  a runtime-controlled trigger so `/config` remains authoritative for both summary computation and
  push cadence.

### HIGH - The build still compiles against Java EE 8 compatibility APIs instead of a Jakarta-only boundary

Target requirement violated:
- The constitution and clarification artifacts fixed this migration on a Spring Boot 3,
  Jakarta-only forward boundary.

Evidence:
- `pom.xml` still includes `javax:javaee-api` and `javax.xml.bind:jaxb-api`.
- Active `src/main/java` sources still import `javax.*` across entities, interfaces, JAX-RS,
  WebSocket helpers, and legacy implementation packages.
- The review found `javax.*` imports in types that remain on the main compile path, not only in
  discarded archived sources.

Impact:
- The project still depends on Java EE 8 umbrella compatibility to compile, which masks incomplete
  namespace migration and leaves Liberty-era programming models on the active build surface.
- This increases future breakage risk as the codebase is tightened around Spring Boot 3 and Jakarta
  APIs.

Required follow-up:
- Remove Java EE umbrella dependencies from the main Boot build.
- Isolate, delete, or fully migrate remaining `javax.*` classes that still sit on the active main
  compilation path.

## Verdict

FAIL.

The implementation matches the target design on the major structural choices that mattered most for
the rewrite: executable WAR packaging, preserved `/daytrader` context root, a canonical
`TradeServices`-owned application seam, a Boot-owned MVC/JSF compatibility slice, and a durable
database-backed async order worker. The review still fails because the current implementation is not
yet conformant on three design-critical boundaries: authenticated streaming access, runtime-owned
streaming cadence, and the Jakarta-only migration boundary.

## Downstream Impact

- `t18` should treat the public streaming surfaces as an input to the security audit rather than a
  hypothetical risk.
- `t20` should expect auth/session parity gaps until the streaming boundary is fixed.
- Backend follow-up should stay local to the Boot-owned streaming and dependency surface; the core
  application seam and async persistence design do not need re-architecture.