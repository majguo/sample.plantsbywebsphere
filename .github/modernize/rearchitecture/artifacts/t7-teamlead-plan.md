# t7 - Spring Boot 3 Implementation Plan

## Summary

This plan turns the approved DayTrader8 target design into an execution blueprint for a Spring Boot 3 rewrite. It sequences the work so downstream agents can establish the Boot runtime shell first, preserve the `TradeServices` business seam and same-schema data contract, then rebuild user-facing capability slices in parity order while producing staged readiness evidence.

The plan is anchored to:

- rewrite governance from `constitution.md`
- runtime-contract preservation from `t4`
- same-schema persistence from `t5`
- mixed-surface validation and readiness policy from `t6` and `t4.1`

## Technical Context

- Runtime: Spring Boot 3.x on JDK 17 with servlet-stack WAR packaging and embedded Tomcat.
- Context contract: preserve `/daytrader` and the current smoke-port default of `9080` until a later artifact changes it.
- Web surface: keep `src/main/webapp` as the authoritative source for JSP, XHTML, static HTML, docs, images, and launcher assets.
- Business seam: preserve a `TradeServices`-compatible application facade as the only business entry point above trading logic.
- Auth/session: preserve `uidBean` and `sessionCreationDate`, protected-route fallback behavior, and logout semantics while using Spring Security as an implementation detail.
- Persistence: keep the current logical schema and `KEYGENEJB` strategy, move to Spring-managed Jakarta Persistence as the canonical write model, and use JDBC only for narrow infrastructure cases.
- Async model: replace JMS/MDB order completion with a durable database-backed order-work queue and one Boot-managed worker.
- Streaming model: replace CDI/JMS fan-out with one internal event path powering REST/SSE/WebSocket updates.
- Alternate surfaces: JSF/XHTML, primitive servlet/JAX-RS/WebSocket demos, docs, and image JSP variants remain in scope.
- Validation: downstream tasks must report staged readiness states only; startup evidence is not parity evidence.

## Constitution Check

- Rewrite mode is preserved: the plan replaces the Java EE/Open Liberty runtime rather than attempting an in-place upgrade.
- Functional parity remains mandatory: every plan item maps to contractual REQ coverage from `t3`.
- Same-schema and auth preservation remain fixed constraints: no parity-phase redesign of the schema or login contract is allowed.
- Execution is vertical: tasks are grouped by capability slices and business outcomes, not by entity/service/controller layer alone.
- Evidence-first delivery is enforced: dedicated validation tasks are part of the plan, and readiness states are scoped per surface or journey.

## Applied Guidelines

- No migration-specific guideline pack was available under `skills/guidelines/` for this stack combination.
- The plan therefore applies the governing constitution plus the upstream architecture, persistence, and readiness artifacts as the binding transformation rules.

## Implementation Steps

### Phase 1 - Platform Shell

#### 1.1 Establish the Spring Boot WAR runtime shell

Create the Boot entrypoint, WAR packaging, servlet initializer, Boot property model, and authoritative runtime wiring while preserving `/daytrader`, the legacy web-root layout, and the current smoke defaults. This step produces build-ready evidence only. Refs: `REQ-001`, `REQ-027`, `REQ-028`.

- [ ] T001 [Plan:1.1] Create the Spring Boot WAR shell in `pom.xml`, `src/main/java/com/ibm/websphere/samples/daytrader/boot/DayTraderApplication.java`, `src/main/java/com/ibm/websphere/samples/daytrader/boot/DayTraderServletInitializer.java`, and `src/main/resources/application.yml` [Source: src/main/java/com/ibm/websphere/samples/daytrader/interfaces/TradeServices.java] [REQ: REQ-001, REQ-027, REQ-028] [Evidence: build-ready Boot WAR packaging preserves `/daytrader`, startup wiring, and current smoke defaults] [Fallback: if container smoke is unavailable, prove packaging and property binding with a focused Maven package run plus config inspection]

#### 1.2 Preserve static assets, JSP reachability, and the JSF bootstrapping baseline

Carry forward `src/main/webapp` unchanged as the authoritative asset root, register the servlet/JSP infrastructure needed by Boot WAR packaging, and isolate the JSF/XHTML compatibility baseline so later slices do not silently drop alternate surfaces. Refs: `REQ-001`, `REQ-026`, `REQ-027`, `REQ-028`.

- [ ] T002 [Plan:1.2] Preserve Boot web-root and alternate-surface scaffolding in `src/main/webapp/**`, `src/main/webapp/WEB-INF/web.xml`, `src/main/java/com/ibm/websphere/samples/daytrader/web/jsfcompat/JsfCompatibilityConfig.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/web/config/WebMvcCompatibilityConfig.java` [Source: src/main/webapp, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/TradeAppJSF.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/JSFLoginFilter.java] [REQ: REQ-001, REQ-026, REQ-027, REQ-028] [Evidence: surface-ready asset, JSP, and JSF/XHTML scaffolding remains reachable under Boot WAR packaging] [Fallback: if JSF runtime validation is blocked, prove preserved asset inventory and servlet registration wiring by direct web-root and descriptor review]

### Phase 2 - Foundational Business and Data Seams

#### 2.1 Port the canonical `TradeServices` facade and Spring application seam

Create the Spring-owned application facade that preserves the DayTrader business contract for login, register, quote lookup, buy, sell, portfolio, account, and market-summary operations. Controllers, JSF beans, streaming handlers, and admin utilities must call this seam rather than direct repositories. Refs: `REQ-003` through `REQ-017`, `REQ-021` through `REQ-024`, `REQ-029`, `REQ-030`.

- [ ] T003 [P] [Plan:2.1] Implement the canonical application seam in `src/main/java/com/ibm/websphere/samples/daytrader/application/TradeServicesFacade.java`, `src/main/java/com/ibm/websphere/samples/daytrader/application/auth/AuthenticationApplicationService.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/TradeOrderApplicationService.java` [Source: src/main/java/com/ibm/websphere/samples/daytrader/interfaces/TradeServices.java, src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/TradeDirect.java, src/main/java/com/ibm/websphere/samples/daytrader/impl/ejb3/TradeSLSBBean.java] [REQ: REQ-003 through REQ-017, REQ-021 through REQ-024, REQ-029, REQ-030] [Evidence: build-ready application facade preserves the canonical business-entry seam for auth, trading, account, and streaming callers] [Fallback: if full integration wiring is unavailable, prove contract coverage with seam-to-source method mapping and focused service-slice tests]

#### 2.2 Freeze same-schema persistence, Boot-owned migrations, and mutable runtime settings

Port entities to `jakarta.persistence`, keep table and column names stable, add repository and JDBC helpers where required, baseline the existing schema under Flyway, and replace `TradeConfig` statics with one runtime-settings service. This step reaches build-ready and startup-ready, not journey-ready. Refs: `REQ-018` through `REQ-020`, `REQ-029`.

- [ ] T004 [P] [Plan:2.2] Implement the persistence and configuration baseline in `src/main/java/com/ibm/websphere/samples/daytrader/persistence/jpa/*.java`, `src/main/java/com/ibm/websphere/samples/daytrader/persistence/jdbc/KeySequenceJdbcRepository.java`, `src/main/java/com/ibm/websphere/samples/daytrader/config/RuntimeSettingsService.java`, `src/main/resources/db/migration/common/V1__baseline.sql`, and `src/main/resources/db/migration/db2/V1__baseline.sql` [Source: src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountProfileDataBean.java, src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountDataBean.java, src/main/java/com/ibm/websphere/samples/daytrader/entities/HoldingDataBean.java, src/main/java/com/ibm/websphere/samples/daytrader/entities/OrderDataBean.java, src/main/java/com/ibm/websphere/samples/daytrader/entities/QuoteDataBean.java, src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/KeySequenceDirect.java, src/main/java/com/ibm/websphere/samples/daytrader/util/TradeConfig.java, src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/TradeDirectDBUtils.java] [REQ: REQ-018, REQ-019, REQ-020, REQ-029] [Evidence: startup-ready same-schema persistence, key generation, and runtime-settings baseline is established under Boot] [Fallback: if live database startup is unavailable, prove entity and migration parity with schema diff review plus repository smoke coverage]

### Phase 3 - User Story Slice: Session Entry, Registration, Account, and Portfolio

#### 3.1 Rebuild the welcome, login, registration, home, and logout flow on Boot-compatible MVC and security adapters

Preserve the `/app` entry surface and its session-based behaviors, including success and failure messaging, immediate post-registration sign-in, protected-route fallback, and logout invalidation. Refs: `REQ-002`, `REQ-003`, `REQ-004`, `REQ-005`, `REQ-006`, `REQ-015`, `REQ-029`, `REQ-030`.

- [ ] T005 [US1] [Plan:3.1] Rebuild the `/app` entry and auth compatibility flow in `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/AppActionController.java`, `src/main/java/com/ibm/websphere/samples/daytrader/web/security/SecurityConfig.java`, `src/main/java/com/ibm/websphere/samples/daytrader/web/security/CompatibilityAuthenticationHandlers.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/web/session/CompatibilitySessionFacade.java` [Source: src/main/java/com/ibm/websphere/samples/daytrader/web/servlet/TradeServletAction.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/TradeAppJSF.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/JSFLoginFilter.java] [REQ: REQ-002, REQ-003, REQ-004, REQ-005, REQ-006, REQ-015, REQ-029, REQ-030] [Evidence: surface-ready `/app` login, registration, home, and logout flows preserve session keys and status semantics] [Fallback: if browser automation is unavailable, prove the flow with MockMvc session tests and direct handler assertions]

#### 3.2 Rebuild account history, profile update, portfolio, and sell flows

Preserve account detail rendering, show-all-orders expansion, profile validation, empty-portfolio behavior, and sell-order submission semantics. Refs: `REQ-011`, `REQ-012`, `REQ-013`, `REQ-014`, `REQ-029`.

- [ ] T006 [US1] [P] [Plan:3.2] Rebuild account and portfolio capability slices in `src/main/java/com/ibm/websphere/samples/daytrader/application/account/AccountApplicationService.java`, `src/main/java/com/ibm/websphere/samples/daytrader/application/portfolio/PortfolioApplicationService.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/AppActionController.java` [Source: src/main/java/com/ibm/websphere/samples/daytrader/web/servlet/TradeServletAction.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/AccountDataJSF.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/PortfolioJSF.java] [REQ: REQ-011, REQ-012, REQ-013, REQ-014, REQ-029] [Evidence: surface-ready account, profile, holdings, and sell-entry flows preserve empty-state and validation behavior] [Fallback: if end-to-end session coverage is unavailable, prove parity with service-slice and controller contract tests]

### Phase 4 - User Story Slice: Trading, Orders, and Market Summary

#### 4.1 Rebuild quote lookup, buy, sell confirmation, and completed-order alert behavior across all order modes

Preserve quote deep links, buy submission, order confirmation details, closed-order acknowledgement, and alert visibility while routing mode-specific completion through the canonical seam. Refs: `REQ-007`, `REQ-008`, `REQ-009`, `REQ-010`, `REQ-017`, `REQ-029`.

- [ ] T007 [US2] [Plan:4.1] Rebuild trading and order-confirmation flows in `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/TradeOrderApplicationService.java`, `src/main/java/com/ibm/websphere/samples/daytrader/application/quotes/QuoteApplicationService.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/AppActionController.java` [Source: src/main/java/com/ibm/websphere/samples/daytrader/web/servlet/TradeServletAction.java, src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/TradeDirect.java, src/main/java/com/ibm/websphere/samples/daytrader/impl/ejb3/TradeSLSBBean.java] [REQ: REQ-007, REQ-008, REQ-009, REQ-010, REQ-017, REQ-029] [Evidence: surface-ready quote lookup, buy, confirmation, and completed-order alert behavior is preserved across order modes] [Fallback: if async infrastructure is not yet available, prove sync-path parity and capture deferred-mode gaps with focused application-service tests]

#### 4.2 Rebuild market-summary rendering and shared trading-page summary integration

Preserve the market-summary page, shared navigation/header rendering, and completed-order banner integration used across the trading UI. Refs: `REQ-006`, `REQ-016`, `REQ-017`, `REQ-029`.

- [ ] T008 [US2] [P] [Plan:4.2] Rebuild market-summary rendering in `src/main/java/com/ibm/websphere/samples/daytrader/application/market/MarketSummaryApplicationService.java`, `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/AppActionController.java`, and `src/main/webapp/marketSummary.jsp` [Source: src/main/java/com/ibm/websphere/samples/daytrader/impl/ejb3/MarketSummarySingleton.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/MarketSummaryJSF.java, src/main/java/com/ibm/websphere/samples/daytrader/web/filter/OrdersAlertFilter.java] [REQ: REQ-006, REQ-016, REQ-017, REQ-029] [Evidence: surface-ready market-summary rendering preserves shared navigation state and alert integration] [Fallback: if dynamic summary refresh is blocked, prove parity with deterministic page-model assertions and fixture-backed summary snapshots]

### Phase 5 - User Story Slice: Operator Utilities and Scenario Driver

#### 5.1 Rebuild runtime configuration, reset, rebuild, and repopulate actions under Boot-owned operations services

Preserve the `/config` surface, editable runtime parameters, explicit success/failure messaging, and vendor-aware database utility behavior without leaving DDL execution on startup. Refs: `REQ-018`, `REQ-019`, `REQ-020`, `REQ-029`.

- [ ] T009 [US3] [Plan:5.1] Rebuild the operator configuration slice in `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/ConfigActionController.java`, `src/main/java/com/ibm/websphere/samples/daytrader/ops/TradeDatabaseAdminService.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/config/RuntimeSettingsService.java` [Source: src/main/java/com/ibm/websphere/samples/daytrader/web/servlet/TradeConfigServlet.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/TradeConfigJSF.java, src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/TradeDirectDBUtils.java] [REQ: REQ-018, REQ-019, REQ-020, REQ-029] [Evidence: surface-ready `/config` operations preserve editable settings and operator-visible success or failure messaging] [Fallback: if destructive DB operations cannot run in the preferred environment, prove controller-to-service parity with dry-run admin tests and vendor-aware SQL review]

#### 5.2 Rebuild the scenario driver and run-statistics result path

Preserve `/scenario`, its supported synthetic actions, and the result pages used for reset and synthetic trade execution. Refs: `REQ-019`, `REQ-020`, `REQ-021`, `REQ-029`.

- [ ] T010 [US3] [P] [Plan:5.2] Rebuild the scenario and run-stats slice in `src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/ScenarioActionController.java`, `src/main/java/com/ibm/websphere/samples/daytrader/application/scenario/ScenarioApplicationService.java`, and `src/main/webapp/runStats.jsp` [Source: src/main/java/com/ibm/websphere/samples/daytrader/web/servlet/TradeScenarioServlet.java, src/main/java/com/ibm/websphere/samples/daytrader/web/servlet/TradeAppServlet.java, src/main/webapp/runStats.jsp] [REQ: REQ-019, REQ-020, REQ-021, REQ-029] [Evidence: surface-ready scenario execution and run-statistics pages preserve supported actions and result rendering] [Fallback: if synthetic trade execution cannot hit a live backing store, prove action routing and result-page parity with fixture-driven controller tests]

### Phase 6 - User Story Slice: REST, Streaming, and Async Replacement

#### 6.1 Rebuild the REST quote and SSE contracts at their current paths

Preserve `/rest/quotes` GET and POST behavior plus `/rest/broadcastevents` initial payload and update stream semantics under Spring MVC/SSE adapters. Refs: `REQ-022`, `REQ-023`, `REQ-029`.

- [ ] T011 [US4] [Plan:6.1] Rebuild REST and SSE compatibility endpoints in `src/main/java/com/ibm/websphere/samples/daytrader/web/rest/QuoteController.java`, `src/main/java/com/ibm/websphere/samples/daytrader/streaming/BroadcastEventsController.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/streaming/StreamingHub.java` [Source: src/main/java/com/ibm/websphere/samples/daytrader/jaxrs/QuoteResource.java, src/main/java/com/ibm/websphere/samples/daytrader/jaxrs/BroadcastResource.java] [REQ: REQ-022, REQ-023, REQ-029] [Evidence: surface-ready REST quote and SSE broadcast contracts preserve paths, payloads, and status semantics] [Fallback: if long-lived streaming validation is unavailable, prove compatibility with contract tests and recorded event-shape assertions]

#### 6.2 Replace JMS/MDB async order completion and market-summary fan-out with durable Boot-native components

Implement the database-backed order-work queue, worker retry policy, event publication, scheduler, and raw WebSocket contract while preserving `Sync`, `Async`, and `Async_2-Phase` behavior. Refs: `REQ-016`, `REQ-017`, `REQ-022`, `REQ-023`, `REQ-024`, `REQ-029`.

- [ ] T012 [US4] [Plan:6.2] Replace async order and streaming infrastructure in `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/OrderWorkService.java`, `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/OrderWorkWorker.java`, `src/main/java/com/ibm/websphere/samples/daytrader/streaming/MarketSummaryScheduler.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/web/websocket/MarketSummaryWebSocketHandler.java` [Source: src/main/java/com/ibm/websphere/samples/daytrader/mdb/DTBroker3MDB.java, src/main/java/com/ibm/websphere/samples/daytrader/mdb/DTStreamer3MDB.java, src/main/java/com/ibm/websphere/samples/daytrader/impl/ejb3/MarketSummarySingleton.java, src/main/java/com/ibm/websphere/samples/daytrader/web/websocket/MarketSummaryWebSocket.java] [REQ: REQ-016, REQ-017, REQ-022, REQ-023, REQ-024, REQ-029] [Evidence: startup-ready durable async completion, event fan-out, scheduler, and WebSocket compatibility components are in place] [Fallback: if brokerless end-to-end validation is blocked, prove order-mode handling and event publication with worker, scheduler, and WebSocket contract tests]

### Phase 7 - User Story Slice: JSF, Primitives, and Alternate Surfaces

#### 7.1 Reattach JSF/XHTML parity on an isolated compatibility slice

Preserve directly reachable XHTML and JSF trading/configuration pages with a dedicated compatibility layer and equivalent auth-filter behavior rather than silently redirecting those surfaces to MVC-only pages. Refs: `REQ-026`, `REQ-028`, `REQ-029`, `REQ-030`.

- [ ] T013 [US5] [Plan:7.1] Rebuild JSF/XHTML compatibility in `src/main/java/com/ibm/websphere/samples/daytrader/web/jsfcompat/TradeAppJsfBridge.java`, `src/main/java/com/ibm/websphere/samples/daytrader/web/jsfcompat/AccountJsfBridge.java`, and `src/main/java/com/ibm/websphere/samples/daytrader/web/filter/JsfCompatibilityLoginFilter.java` [Source: src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/TradeAppJSF.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/AccountDataJSF.java, src/main/java/com/ibm/websphere/samples/daytrader/web/jsf/JSFLoginFilter.java] [REQ: REQ-026, REQ-028, REQ-029, REQ-030] [Evidence: surface-ready XHTML and JSF compatibility layer preserves direct reachability and auth-filter behavior] [Fallback: if a Boot-compatible JSF runtime is temporarily unavailable, prove bridge contracts and protected-route behavior with adapter tests plus route inventory review]

#### 7.2 Recreate primitive servlet, JAX-RS, WebSocket, launcher, and docs reachability surfaces

Preserve the primitive launcher and representative servlet/JAX-RS/WebSocket/EJB-demo reachability surfaces, plus static docs and informational pages, using Boot-native servlet registrations and controller adapters where needed. Refs: `REQ-001`, `REQ-025`, `REQ-026`, `REQ-027`, `REQ-028`, `REQ-029`.

- [ ] T014 [US5] [P] [Plan:7.2] Recreate the primitive and documentation slice in `src/main/java/com/ibm/websphere/samples/daytrader/web/prims/**/*.java`, `src/main/java/com/ibm/websphere/samples/daytrader/web/rest/PrimitiveEchoController.java`, `src/main/java/com/ibm/websphere/samples/daytrader/web/websocket/PrimitiveWebSocketHandlers.java`, and `src/main/webapp/docs/**` [Source: src/main/java/com/ibm/websphere/samples/daytrader/web/prims, src/main/java/com/ibm/websphere/samples/daytrader/web/prims/jaxrs/JAXRSSyncService.java, src/main/java/com/ibm/websphere/samples/daytrader/web/websocket/MarketSummaryWebSocket.java, src/main/webapp/web_prmtv.html] [REQ: REQ-001, REQ-025, REQ-026, REQ-027, REQ-028, REQ-029] [Evidence: surface-ready primitive launcher, demo endpoints, WebSocket samples, and docs remain directly reachable] [Fallback: if full primitive runtime coverage is unavailable, prove reachability with targeted HTTP probes and static asset inventory checks]

### Phase 8 - Validation Harness and Readiness Evidence

#### 8.1 Build the deterministic test harness and environment bootstrap required by `t6`

Add the Boot `test` profile, seed/reset fixtures, MockMvc bases, streaming harnesses, real-infrastructure test configuration, and the dedicated Playwright workspace needed for browser parity. Refs: `REQ-002` through `REQ-030`.

- [ ] T015 [Plan:8.1] Build the validation harness in `src/test/resources/application-test.yml`, `src/test/java/com/ibm/websphere/samples/daytrader/support/**`, `src/test/java/com/ibm/websphere/samples/daytrader/integration/**`, `e2e/package.json`, and `e2e/playwright.config.ts` [Source: src/main/webapp/welcome.jsp, src/main/webapp/tradehome.jsp, src/main/java/com/ibm/websphere/samples/daytrader/jaxrs/QuoteResource.java, src/main/java/com/ibm/websphere/samples/daytrader/web/websocket/MarketSummaryWebSocket.java] [Source: .github/modernize/rearchitecture/artifacts/t6-teamlead.md, .github/modernize/rearchitecture/artifacts/t4.1-teamlead.md] [REQ: REQ-002 through REQ-030] [Evidence: build-ready and startup-ready validation harness covers HTML, REST, SSE, WebSocket, and browser parity lanes] [Fallback: if Docker or browser tooling is unavailable, preserve deterministic coverage with MockMvc and streaming harnesses while documenting blocked lanes explicitly]

#### 8.2 Implement journey suites and evidence publication for staged readiness

Create executable coverage for `J1` through `J7`, including order-mode variants, REST/SSE/WebSocket contracts, and alternate-surface reachability, then publish build/startup/surface/journey evidence in the format required by `t4.1` and `t6`. Refs: `REQ-001` through `REQ-030`.

- [ ] T016 [Plan:8.2] Implement the parity suites and evidence publishing in `src/test/java/com/ibm/websphere/samples/daytrader/integration/journeys/**`, `src/test/java/com/ibm/websphere/samples/daytrader/integration/streaming/**`, `e2e/tests/**`, and `scripts/ci/publish-readiness-evidence.ps1` [Source: src/main/webapp/welcome.jsp, src/main/webapp/account.jsp, src/main/webapp/portfolio.jsp, src/main/webapp/quote.jsp, src/main/java/com/ibm/websphere/samples/daytrader/jaxrs/BroadcastResource.java, src/main/java/com/ibm/websphere/samples/daytrader/web/websocket/MarketSummaryWebSocket.java] [Source: .github/modernize/rearchitecture/artifacts/t6-teamlead.md, .github/modernize/rearchitecture/artifacts/t4.1-teamlead.md] [REQ: REQ-001 through REQ-030] [Evidence: journey-ready and release-ready suites publish staged readiness evidence for J1 through J7 and all alternate surfaces] [Fallback: if the preferred full-stack lane is unavailable, publish per-surface fallback evidence and blocked-lane disclosure without overstating readiness]

## Project Structure

The implementation should remain a single Maven module unless execution proves stronger physical separation is required. Within that module, use these package boundaries:

- `com.ibm.websphere.samples.daytrader.boot` - Boot entrypoint, WAR initializer, startup wiring
- `com.ibm.websphere.samples.daytrader.config` - immutable Boot properties plus mutable runtime settings
- `com.ibm.websphere.samples.daytrader.application` - canonical `TradeServices` facade and business use cases
- `com.ibm.websphere.samples.daytrader.persistence.jpa` - Jakarta entities and repositories mapped to the existing schema
- `com.ibm.websphere.samples.daytrader.persistence.jdbc` - narrow JDBC helpers for key allocation, admin utilities, and lock-sensitive SQL
- `com.ibm.websphere.samples.daytrader.web.mvc` - `/app`, `/config`, and `/scenario` compatibility controllers
- `com.ibm.websphere.samples.daytrader.web.security` and `web.session` - Spring Security integration and session-compatibility handlers
- `com.ibm.websphere.samples.daytrader.streaming` - SSE, event hub, scheduler, async publication support
- `com.ibm.websphere.samples.daytrader.web.websocket` - raw WebSocket handlers at existing paths
- `com.ibm.websphere.samples.daytrader.web.jsfcompat` - isolated JSF/XHTML compatibility bridge
- `com.ibm.websphere.samples.daytrader.ops` - reset, rebuild, repopulate, and administrative operations
- `src/test/java/...` and `e2e/` - validation harness and browser parity suites

## Requirement Mapping

| REQ ID | Description | Plan Items | Implementation Evidence |
|--------|-------------|------------|------------------------|
| REQ-001 | Preserve framed shell and top-level navigation | 1.1, 1.2, 7.2 | `DayTraderApplication`, `WebMvcCompatibilityConfig`, launcher and docs assets |
| REQ-002 | Preserve welcome/login page and status messaging | 3.1, 8.1, 8.2 | `AppActionController`, `SecurityConfig`, login journey tests |
| REQ-003 | Preserve credential authentication and session creation | 2.1, 3.1, 8.2 | `AuthenticationApplicationService`, `CompatibilitySessionFacade`, J1 tests |
| REQ-004 | Preserve invalid-login rejection behavior | 3.1, 8.2 | auth handlers, login parity tests |
| REQ-005 | Preserve registration page and immediate sign-in | 3.1, 8.2 | registration branch in `AppActionController`, J2 tests |
| REQ-006 | Preserve authenticated home page and shared trading nav | 3.1, 4.2, 8.2 | home-page model assembly, shared JSP fragments, J1 tests |
| REQ-007 | Preserve quote lookup by symbols | 2.1, 4.1, 8.2 | `QuoteApplicationService`, trading flow tests |
| REQ-008 | Preserve quote symbol deep-link behavior | 4.1, 8.2 | `/app?action=quotes` adapter, J3 tests |
| REQ-009 | Preserve buy submission contract | 2.1, 4.1, 6.2, 8.2 | `TradeOrderApplicationService`, async worker, J3 tests |
| REQ-010 | Preserve order-confirmation surface | 4.1, 8.2 | order model/view assembly, J3 and J4 tests |
| REQ-011 | Preserve portfolio page and holdings detail | 3.2, 8.2 | `PortfolioApplicationService`, J4 tests |
| REQ-012 | Preserve empty-portfolio behavior | 3.2, 8.2 | empty-state rendering in portfolio flow tests |
| REQ-013 | Preserve account page and show-all-orders behavior | 3.2, 8.2 | `AccountApplicationService`, J4 tests |
| REQ-014 | Preserve account-profile editing and validation | 3.2, 8.2 | profile update path, validation assertions |
| REQ-015 | Preserve logout behavior | 3.1, 8.2 | logout handler and J1 tests |
| REQ-016 | Preserve market-summary page | 4.2, 6.2, 8.2 | `MarketSummaryApplicationService`, scheduler, J6 tests |
| REQ-017 | Preserve completed-order alerts | 4.1, 4.2, 6.2, 8.2 | order-alert compatibility logic, J3 tests |
| REQ-018 | Preserve editable runtime configuration | 2.2, 5.1, 8.2 | `RuntimeSettingsService`, `ConfigActionController`, J5 tests |
| REQ-019 | Preserve update/reset/rebuild/repopulate actions | 2.2, 5.1, 5.2, 8.2 | `TradeDatabaseAdminService`, `ScenarioActionController`, J5/J7 tests |
| REQ-020 | Preserve operator-visible success and failure messaging | 5.1, 5.2, 8.2 | result-page rendering, admin journey tests |
| REQ-021 | Preserve scenario-driver endpoint and actions | 5.2, 8.2 | `ScenarioApplicationService`, J7 tests |
| REQ-022 | Preserve REST quote API contract | 2.1, 6.1, 6.2, 8.2 | `QuoteController`, REST contract tests |
| REQ-023 | Preserve SSE broadcast endpoint | 6.1, 6.2, 8.2 | `BroadcastEventsController`, `StreamingHub`, SSE tests |
| REQ-024 | Preserve market-summary WebSocket endpoint and browser behavior | 6.2, 8.1, 8.2 | `MarketSummaryWebSocketHandler`, WebSocket and Playwright tests |
| REQ-025 | Preserve primitive JAX-RS echo endpoints | 7.2, 8.2 | `PrimitiveEchoController`, primitive API tests |
| REQ-026 | Preserve primitive launcher and linked demo surfaces | 1.2, 7.1, 7.2, 8.2 | primitive servlet registrations, JSF bridges, reachability tests |
| REQ-027 | Preserve reachable docs and informational pages | 1.1, 1.2, 7.2, 8.2 | static docs assets, reachability tests |
| REQ-028 | Preserve image JSP and directly reachable XHTML surfaces | 1.2, 7.1, 7.2, 8.2 | JSF compatibility slice and alternate-surface probes |
| REQ-029 | Preserve status-code semantics across surfaces | 2.1, 3.1, 3.2, 4.1, 5.1, 6.1, 7.1, 7.2, 8.2 | compatibility exception handling and parity assertions |
| REQ-030 | Preserve session-dependent access control | 2.1, 3.1, 7.1, 8.2 | `SecurityConfig`, `CompatibilitySessionFacade`, auth regression tests |