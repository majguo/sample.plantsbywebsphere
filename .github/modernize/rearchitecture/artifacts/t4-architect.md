# t4 - Spring Boot 3 Target Architecture and Contract Preservation Approach

## Summary

The Spring Boot 3 target should be a single servlet-stack application that preserves all existing DayTrader8 URLs and visible behaviors above one canonical application-service seam, while replacing Liberty, EJB, JMS/MDB, CDI eventing, and static in-process configuration with explicit Boot-owned components. The target keeps the current context root `/daytrader`, session-auth contract, order-mode semantics, streaming endpoints, operator configuration surface, and benchmark primitives.

The design chooses an executable WAR-based Spring Boot deployment with embedded Tomcat, Spring MVC for HTTP adapters, Spring WebSocket/SSE for streaming adapters, one canonical transactional service layer beneath a `TradeServices`-compatible facade, and a database-backed durable order-work queue for async completion. JSF/XHTML parity remains in scope through a dedicated compatibility layer using a Spring Boot-compatible JSF integration rather than treating JSF pages as deprecated by default.

## Target Decisions

### D1. Packaging and runtime

- Use Spring Boot 3 on the servlet stack.
- Package the application as an executable WAR, not an executable JAR.
- Keep the default context path as `/daytrader` and preserve the current HTTP smoke port default of `9080` until a later artifact changes it explicitly.
- Retain `src/main/webapp` as the web-root source for JSP, XHTML, images, docs, and primitive static assets.

Rationale:
- Spring Boot's servlet reference states that `src/main/webapp` is only reliable with WAR packaging and that embedded-container JSP support does not work with executable JAR packaging.
- This avoids a design that would break direct JSP reachability or force an unnecessary view rewrite during the platform migration.

### D2. One canonical business seam

- Preserve a `TradeServices`-equivalent application-service facade as the only supported entry point for trading behavior.
- All HTTP, JSP, JSF, REST, SSE, WebSocket, scenario, and admin/config adapters depend on that seam.
- Legacy runtime mode names may survive only as compatibility inputs to the operator surface and tests; they do not survive as peer business-service implementations.

Target shape:
- `daytrader-boot-app`: Boot entrypoint, embedded container, external configuration, startup wiring.
- `daytrader-web-compat`: MVC controllers, JSP forwards, JSF bridge beans, servlet filters, primitive servlet registrations.
- `daytrader-application`: authenticated use cases, order orchestration, market-summary service, runtime-settings service.
- `daytrader-domain-events`: order-completed, quote-updated, market-summary-updated publication contracts.
- `daytrader-persistence`: JPA repositories plus narrowly scoped JDBC helpers for primitives/admin work where direct SQL is still required.
- `daytrader-streaming`: SSE emitters, WebSocket handlers, event fan-out cache.
- `daytrader-ops`: schema/bootstrap utilities, reset/rebuild flows, smoke/health support.

These can be implemented as Java packages first and split into Maven modules only if t7 needs stronger physical isolation.

### D3. Web and URL contract preservation

Use explicit compatibility adapters rather than broad rewrites:

- `/app` remains a query-parameter action controller.
- `/config` remains a query-parameter action controller.
- `/scenario` remains a query-parameter action controller.
- `/rest/quotes` and `/rest/broadcastevents` move to Spring MVC endpoints with unchanged paths, methods, parameters, and response shapes.
- `/marketsummary` remains a raw WebSocket endpoint without introducing STOMP or SockJS.
- `/jaxrs/sync/*` primitive endpoints are reimplemented as direct servlet or MVC handlers at the same paths rather than preserving JAX-RS as a second first-class programming model.
- Directly reachable JSP, XHTML, image JSP, HTML, and docs assets stay addressable at their current URLs.

Controller rules:
- Preserve current success and failure messages where they are rendered into the page model.
- Preserve current redirect and forward behavior for unauthenticated access, invalid login, registration errors, and account-update validation failures.
- Do not normalize query-parameter names, status codes, or page names during the migration.

### D4. Session-auth boundary

Use Spring Security as an implementation detail, but keep application-managed session behavior as the contract:

- Authentication success must set session attributes `uidBean` and `sessionCreationDate`.
- Protected routes must fall back to the welcome/login experience with current observable behavior instead of a generated Spring login page.
- Logout must invalidate the HTTP session and clear the authenticated security context.
- WebSocket handshake and SSE subscriptions must honor the same authenticated session boundary.

Target implementation pattern:
- Custom `AuthenticationProvider` or equivalent authentication service delegates credential validation to the canonical application service.
- Custom authentication success and failure handlers preserve the current redirects and result messages.
- CSRF defaults must not be allowed to break existing form posts; parity-sensitive routes require explicit configuration.
- A dedicated `CompatibilitySessionFacade` owns setting and reading `uidBean`, `sessionCreationDate`, and any order-alert/session metadata used by pages.

### D5. Canonical transaction and persistence model

- Choose one canonical write model beneath the service seam: Spring-managed JPA with `@Transactional` service methods.
- Preserve the current schema as the primary persistence contract; any schema changes must be additive and justified by t5.
- Direct JDBC stays allowed only for benchmark primitives, reset/build utilities, or narrow performance-sensitive helpers that do not create a second business-service mode.
- Repository access must not leak into controllers or view beans.

Why JPA-first:
- It best matches the current entity-centric account, holding, quote, and order model.
- It maps naturally to the current transactional behavior owned by EJB services.
- It keeps the target simpler than carrying forward both JPA and direct-JDBC business implementations.

### D6. Async order completion replacement

Replace JMS/MDB order completion with one explicit durable processing model:

- On `buy` and `sell`, the synchronous request transaction creates the order and persists an order-work record when the active compatibility mode is `Async` or `Async_2-Phase`.
- A single Boot-managed background worker polls and claims pending order-work records transactionally.
- Worker retries are bounded and explicit.
- Processing is idempotent on order ID.
- Failures are recorded as visible operational state instead of disappearing into best-effort async execution.
- Redelivery and rollback semantics are modeled by state transitions on the durable work record, not by in-memory executor behavior.

Recommended target contract:
- `Sync`: request completes the order before rendering the confirmation page.
- `Async`: request commits the order shell plus queued work; worker completes it after commit.
- `Async_2-Phase`: request commits phase-1 order state, worker completes phase-2 settlement, and cancellation/rollback semantics remain externally observable.

This design intentionally avoids introducing a new broker dependency just to replace Liberty JMS in a sample application that must continue to support Derby-based smoke environments.

### D7. Event fan-out and streaming

Use one canonical internal publication path for quote and market-summary changes:

- Application services publish domain events: `QuoteUpdated`, `MarketSummaryUpdated`, `OrderCompleted`.
- A `StreamingHub` maintains recent quote-change cache and active client registrations.
- SSE endpoint `/rest/broadcastevents` serves initial welcome-or-recent-data payload, then streams quote updates.
- WebSocket endpoint `/marketsummary` pushes summary snapshots and recent quote changes using the current browser contract.

Operational rules:
- Update cadence remains controlled by runtime settings, not hard-coded scheduler constants.
- Disconnect handling is explicit and does not leak emitters/sessions.
- Streaming adapters consume canonical domain events; they do not query the database independently to invent their own cadence.

### D8. Market summary scheduler

Replace `@Singleton + @Schedule + CDI async event` behavior with Boot scheduling plus explicit publication:

- A scheduled component runs on the interval defined by the runtime settings service.
- It computes the summary through the application layer, updates the cached snapshot, and publishes `MarketSummaryUpdated`.
- The scheduler can be disabled or slowed by the same operator configuration contract currently exposed through `/config`.

### D9. Configuration model

Replace `TradeConfig` and `server.xml` with a two-layer model:

- Immutable Boot configuration in `application.yml` and environment variables for server port, context path, datasource, SSL, and startup wiring.
- Mutable runtime settings in a `RuntimeSettingsService` that is initialized from Boot config at startup and then mutated in-process by `/config` actions.

This preserves current operator-visible semantics without silently introducing restart-persistent config writes that do not exist today.

Authoritative mapping:
- `server.servlet.context-path=/daytrader`
- `server.port=9080` default for smoke parity
- datasource settings replace Liberty `jdbc/TradeDataSource`
- runtime toggles replace `TradeConfig` static state
- streaming cadence and quote-publish toggles are read only from `RuntimeSettingsService`

### D10. JSF/XHTML compatibility policy

- JSF/XHTML pages remain in scope as supported parity surfaces.
- Implement them in a dedicated compatibility slice using a Spring Boot-compatible JSF integration aligned to the chosen Boot 3 minor.
- Prefer a Mojarra-based integration path to stay close to the current application's JSF behavior.
- Keep JSF-specific scope and servlet registration isolated from the MVC/JSP path so that later deprecation, if approved, is localized.

Reasoning:
- JoinFaces documents a supported path for JSF inside Spring Boot, including servlet-context parameter mapping and scope support.
- This is less risky than attempting to simulate JSF navigation with ad hoc MVC controllers while XHTML pages remain contractual parity surfaces.

## Contract Preservation Map

| Current contract | Source anchor | Target owner |
|---|---|---|
| `TradeServices` business seam | `interfaces/TradeServices.java` | `daytrader-application` facade |
| `uidBean` and `sessionCreationDate` session markers | `web/jsf/TradeAppJSF.java`, `web/jsf/JSFLoginFilter.java` | `CompatibilitySessionFacade` + security handlers |
| `/app?action=*` trading actions | `web/servlet/TradeServletAction.java` | MVC compatibility controllers |
| `/rest/quotes` GET/POST JSON contract | `jaxrs/QuoteResource.java` | Spring MVC REST controller |
| async order completion with rollback-sensitive redelivery | `mdb/DTBroker3MDB.java` | durable order-work queue + worker |
| quote and market-summary publication | `mdb/DTStreamer3MDB.java`, `impl/ejb3/MarketSummarySingleton.java` | `StreamingHub` + scheduler |
| operator-controlled runtime toggles | `util/TradeConfig.java` | `RuntimeSettingsService` |
| context path, datasource, JMS names, smoke ports | `src/main/liberty/config/server.xml` | Boot properties + wiring config |

## Recommended Package Boundaries

Use the existing base package and introduce explicit boundaries below it:

- `com.ibm.websphere.samples.daytrader.boot`
- `com.ibm.websphere.samples.daytrader.web.mvc`
- `com.ibm.websphere.samples.daytrader.web.jsfcompat`
- `com.ibm.websphere.samples.daytrader.web.filter`
- `com.ibm.websphere.samples.daytrader.application`
- `com.ibm.websphere.samples.daytrader.application.auth`
- `com.ibm.websphere.samples.daytrader.application.orders`
- `com.ibm.websphere.samples.daytrader.streaming`
- `com.ibm.websphere.samples.daytrader.persistence.jpa`
- `com.ibm.websphere.samples.daytrader.persistence.jdbc`
- `com.ibm.websphere.samples.daytrader.ops`
- `com.ibm.websphere.samples.daytrader.config`

Rule:
- code in `web.*` may depend on `application.*`
- code in `application.*` may depend on `persistence.*`, `streaming`, and `config`
- `persistence.*` must not depend on `web.*`
- `streaming` consumes published events and session context only through explicit contracts

## Migration Approach

### Phase A - platform shell

- Introduce Boot entrypoint, executable WAR packaging, servlet stack, and externalized config.
- Keep static web resources and view assets in place.

### Phase B - seam-first business migration

- Port `TradeServices` operations into a Spring-managed application facade.
- Establish the canonical JPA transaction model.

### Phase C - auth and trading surface compatibility

- Rebuild `/app`, `/config`, and `/scenario` on MVC controllers plus JSP forwards.
- Introduce the compatibility session facade and security handlers.

### Phase D - async and streaming replacement

- Replace JMS/MDB with durable order-work processing.
- Add SSE and raw WebSocket adapters on the canonical event stream.

### Phase E - JSF and primitives

- Reattach JSF/XHTML compatibility on the isolated JSF slice.
- Recreate primitive servlets, websocket demos, and admin utilities at the same paths.

## Risks and Mitigations

### CRITICAL - async order semantics drift

Risk:
Flattening `Async` and `Async_2-Phase` into an in-memory executor would lose redelivery, rollback, and restart-safety semantics.

Mitigation:
Use a durable order-work queue with explicit state transitions, retry policy, and idempotent completion.

### HIGH - auth/session drift from Spring defaults

Risk:
Generated login pages, CSRF protection, different logout flow, or missing session markers would break REQ-002 through REQ-005 and REQ-030.

Mitigation:
Use custom success/failure/logout handling and an explicit session compatibility facade. Characterize redirect and message behavior before swapping adapters.

### HIGH - JSP/JSF reachability drift

Risk:
Executable JAR packaging or premature removal of JSF support would silently drop directly reachable parity surfaces.

Mitigation:
Use executable WAR packaging and keep a dedicated JSF compatibility slice using a Boot-compatible JSF integration.

### HIGH - status-code drift on REST and admin endpoints

Risk:
Boot error handling and MVC defaults can change observable error responses.

Mitigation:
Add compatibility-focused exception handling and characterization tests for current status-code behavior before replacing adapters.

### MEDIUM - configuration authority split

Risk:
Allowing both Boot properties and ad hoc mutable statics to act as sources of truth will recreate the current ambiguity.

Mitigation:
All runtime reads go through `RuntimeSettingsService`; all immutable bootstrapping reads go through Boot configuration properties.

## Inputs for Downstream Tasks

### For t5 [dba]

- Design the additive schema needed for the durable order-work queue if the chosen persistence strategy requires new tables.
- Preserve current business tables and keys as the canonical domain schema.
- Validate transaction and lock behavior for order creation, completion, cancellation, and runtime reset actions.

### For t6 [teamlead]

- Characterize `uidBean` and `sessionCreationDate` behavior in login, protected-route fallback, and logout.
- Test all three order modes independently.
- Test current `/rest/broadcastevents` initial payload behavior and `/marketsummary` live refresh behavior.
- Include direct JSP, XHTML, primitive servlet, and WebSocket reachability in parity evidence.

### For t7 [teamlead]

- Plan implementation in seam-first order: packaging -> application facade -> auth/session -> MVC compatibility -> async queue -> streaming -> JSF/primitives.
- Keep MVC, JSF compatibility, async worker, and streaming as separate workstreams to control risk.

## Applied References

- Spring Boot servlet reference: servlet stack behavior, WAR-vs-JAR implications for `src/main/webapp`, and embedded-container JSP limitations.
- JoinFaces reference: supported Spring Boot integration for JSF, servlet-context property mapping, and JSF/CDI scope support.

## Smoke Evidence

- Build command: `pwsh -File scripts/smoke-clean-build.ps1`
- Build result: success
- Startup command: `mvn -B liberty:run -DskipTests`
- Startup result: Liberty feature installation remained in progress during the task probe window.
- HTTP probe: `http://localhost:9080/daytrader`
- HTTP result: connection refused during the probe window, so fresh in-task readiness remained inconclusive rather than confirmed.

## Verdict

The target architecture is feasible without changing the product contract, but only if the migration treats runtime behavior as explicit compatibility contracts instead of assuming container equivalence. The non-negotiable design choices are: executable WAR packaging, one application-service seam, one canonical JPA transaction model, one durable async order-processing mechanism, one canonical streaming publication path, and one authoritative Boot configuration model.