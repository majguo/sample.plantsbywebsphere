## [t9] Scaffolded the Spring Boot 3 WAR shell and web bootstrap baseline
- Replaced the Liberty-centric build shell with a Boot 3 WAR while preserving `src/main/webapp` as the authoritative asset root.
- The workspace default Maven runtime was Java 11; packaging and startup validation required switching to the installed Microsoft JDK 17.
- JoinFaces-based starters activated unmigrated legacy JSF/CDI beans too early; plain Mojarra plus descriptor/bootstrap scaffolding was the stable shell boundary for this phase.
- Spring component scanning had to exclude `com.ibm.websphere.samples.daytrader.web.jsf.*` so the shell could boot without crossing into the later JSF compatibility task.
- Learnings consumed: [(none)]

## [t10] Implemented the canonical Boot trade seam and same-schema JPA baseline
- The existing `TradeServices` contract could move into Boot without touching web adapters by centering the migration on one Spring `TradeServicesFacade` plus auth/order services.
- Boot repository scanning did not follow the explicit component-scan package automatically because the application class lives under `.boot`; `@EntityScan` and `@EnableJpaRepositories` were required for startup.
- Hibernate 6 table generators expected a `BIGINT`-style `KEYGENEJB.KEYVAL`, which broke schema validation against the shipped `INTEGER` table. The stable fix was to remove table generators from the entities and allocate keys through a small JDBC helper instead of changing the schema.
- Derby startup needed `hibernate.jdbc.lob.non_contextual_creation=true` to avoid the unsupported `createNClob` path during bootstrap.
- Async and 2-phase order processing were left as explicit fail-fast placeholders in the canonical seam so t14 can add the durable worker without undoing sync-path behavior.
- Learnings consumed: [backend/boot-war-shell-and-jsf-scan-boundary]

## [t11] Rebuilt Spring MVC-owned auth, account, and portfolio compatibility on `/app`
- The stable replacement for the legacy `TradeAppServlet`/`TradeServletAction` slice is a Spring MVC controller that forwards to the existing JSPs chosen by `TradeConfig.getPage(...)`, not a partial attempt to wire the CDI servlet classes into Boot.
- The authenticated JSP contract depends on request attributes and session markers more than on servlet mechanics: `uidBean`, `sessionCreationDate`, `accountData`, `accountProfileData`, `holdingDataBeans`, `quoteDataBeans`, and `results` were the minimum compatibility surface for this slice.
- `@WebMvcTest` could not auto-discover the Boot app because `DayTraderApplication` lives under `.boot`; the controller tests needed explicit `@ContextConfiguration` rather than relying on package-based discovery.
- Keeping logout tolerant of missing container-managed auth avoids breaking the visible flow before the later Spring Security work is wired in.
- Learnings consumed: [backend/boot-war-shell-and-jsf-scan-boundary, backend/jdbc-keygen-for-boot-jpa]

## [t13] Rebuilt Boot MVC-owned operator configuration and scenario compatibility entrypoints
- The stable migration seam for `/config` was not the JSP page but the mutable runtime settings authority. Expanding `RuntimeSettingsService` to mirror all operator-tunable values kept the legacy JSP contract intact while removing direct controller writes to ad hoc statics.
- `TradeDirectDBUtils` could be reused for reset/build behavior once it was moved to Spring-managed `DataSource` and `TradeServices` injection; no second SQL implementation was necessary for the Boot admin slice.
- `/scenario` can stay a compatibility router that forwards to `/app?action=*`; it does not need its own business implementation as long as the scenario action selection logic remains in one place.
- The original `TradeConfigServlet` did not stop at the DB utility stream for `buildDB`/`buildDBTables`; it re-rendered `/config` afterward with operator status, and the Boot controller needed the same post-build handoff to preserve the admin flow.
- Focused validation passed with `TradeConfigControllerTest` after tightening the regression test to assert the controller contract instead of mocked response-body content.
- Learnings consumed: [backend/boot-war-shell-and-jsf-scan-boundary]

## [t14] Implemented Boot streaming adapters and durable async order completion
- REST quote lookup, SSE broadcast, and raw market-summary WebSocket now sit on Spring MVC/WebSocket adapters while preserving the legacy path and payload shapes.
- The stable async replacement was one additive `ORDERWORKEJB` table keyed through `KEYGENEJB`, with a single scheduled worker consuming persisted order work inside the Boot service seam.
- Reusing one `StreamingHub` for SSE and WebSocket payload serialization avoided contract drift between `quotes.html` and `marketSummary.html`.
- The first validation failure was local, not architectural: `spring-boot-starter-websocket` had not actually landed in `pom.xml`, and the new test had one stale mock name. Fixing those immediately made the focused test meaningful.
- Runtime startup proved the Flyway `V2` migration and new streaming wiring boot cleanly on the shipped Derby data after packaging.
- Learnings consumed: [backend/boot-war-shell-and-jsf-scan-boundary]

## [t12] Rebuilt Boot MVC-owned trading, quotes, order confirmation, and market summary flows on `/app`
- The existing `TradeServices` seam already owned the quote and order behavior; the missing migration work was the Spring MVC action wiring and the JSP request-model contract, not a second service or persistence path.
- Keeping `quotes`, `buy`, `sell`, and `mksummary` on `TradeAppCompatibilityController` preserved the legacy query-parameter action contract without reviving `TradeAppServlet` or `TradeServletAction`.
- Boot-owned pages that still render the legacy closed-order alert block should populate `closedOrders` directly in the controller rather than relying on the old Java EE `OrdersAlertFilter` path.
- Focused validation first failed on a Mockito wildcard stub for `getClosedOrders`; switching that single stub to `doReturn(...)` restored a meaningful controller test run.
- On Windows, a reused terminal can still surface a stale Maven batch prompt and produce a false failure; rerunning the focused controller test through a clean `cmd /d /c ... mvn` shell gave deterministic validation.
- Learnings consumed: [backend/boot-streaming-and-order-work, backend/mvc-app-session-compatibility, backend/jdbc-keygen-for-boot-jpa]

## [t13] Verified Boot MVC-owned operator configuration, scenario routing, and run-stats slice
- The existing `/config` and `/scenario` Boot controllers already matched the planned migration seam: mutable operator state is owned by `RuntimeSettingsService`, reset/build logic stays on `TradeDirectDBUtils`, and scenario routing continues to forward through `/app?action=*`.
- Focused validation for this slice is the MVC controller layer, not packaged-war startup; `TradeConfigControllerTest` and `TradeScenarioControllerTest` passed together under JDK 17.
- The integrated terminal reused stale startup output during validation, so the reliable evidence came from the refreshed Surefire reports rather than the raw terminal transcript.
- Learnings consumed: [backend/boot-mvc-operator-surfaces, backend/boot-war-shell-and-jsf-scan-boundary, backend/jdbc-keygen-for-boot-jpa, backend/mvc-app-session-compatibility]

## [t15] Revalidated the JSF/XHTML bridge and primitive compatibility surfaces on the Boot WAR
- The existing `web.jsfcompat` bridge package was already the correct migration seam for Faces/XHTML parity; the local defect was that `TradeConfigJsfBridge` exposed `getMaxQuotes()` but misspelled the corresponding setter, which breaks `config.xhtml` postback binding.
- A minimal bean-introspection regression test was enough to lock that JSF contract in place without needing a heavier container-level JSF test harness.
- The first widened packaging failure was environmental, not architectural: a stale `java -jar target\io.openliberty.sample.daytrader8.war` process on Windows held the WAR open and blocked Spring Boot repackage until it was stopped.
- Representative runtime probes on `/welcome.faces`, `/PingJsf.faces`, `/PingCDIJSF.faces`, and `/servlet/PingServlet` all returned `200` from the packaged Boot app after the fix.
- Learnings consumed: [backend/boot-war-shell-and-jsf-scan-boundary, backend/mvc-app-session-compatibility, backend/boot-mvc-operator-surfaces]

## [t16.1] Fixed SSE disconnect cleanup so later WebSocket and quote-change publication stay valid
- The tester repro was controlled entirely by `StreamingHub` emitter lifecycle handling: the shared hub design from t14 was correct, but calling `completeWithError(...)` after a disconnected emitter send failure re-threw against an already failed async context.
- The stable fix was to treat `IOException` from `SseEmitter.send(...)` as disconnect cleanup, remove the emitter, and attempt a plain `complete()` while swallowing `IllegalStateException` if the emitter was already terminal.
- A narrow hub regression test was enough to lock the cleanup rule down, and the tester's live `DayTraderStreamingIntegrationTest` passed immediately afterward under JDK 17.
- Tomcat still logs an expected client-aborted socket `IOException` when the SSE test closes the stream; that noise does not represent a backend contract failure once publication continues cleanly.
- Learnings consumed: [backend/boot-streaming-and-order-work]

## [t17.1] Revalidated and locked the repaired auth/runtime/secret review slice
- The current workspace already carried the substantive backend remediation for this review slice: `/config` is operator-gated through the shared session interceptor, REST/SSE use the authenticated `uidBean` boundary, `/marketsummary` enforces auth plus same-origin at handshake time, market-summary publication cadence comes from `RuntimeSettingsService`, and auth writes now hash passwords while masking credit-card values.
- The local defect found during focused validation was only in the new regression harness: the anonymous REST POST probe needed `application/x-www-form-urlencoded` so the auth interceptor, not media-type negotiation, owned the outcome.
- The durable value added in this task was focused regression coverage for operator-only config access, REST/SSE auth enforcement, WebSocket handshake auth/origin rules, and secret redaction so later review reruns can rely on executable evidence instead of source inspection alone.
- The repo still contains legacy `javax.*` source in excluded compatibility packages, but the active Boot build surface validated here no longer relies on the Java EE umbrella dependencies that previously masked migration drift.
- Learnings consumed: [backend/boot-mvc-operator-surfaces, backend/boot-streaming-and-order-work, backend/mvc-app-session-compatibility]

## [t20.2] Added Boot DB2 runtime wiring for the primary infrastructure lane
- The local root cause from `t20.1` was exactly at the packaged Boot runtime boundary: the WAR only carried Derby and Flyway 9, so DB2 startup failed before any real infrastructure handshake.
- Supporting DB2 in the Boot lane required both the IBM `jcc` runtime driver and a Flyway 10.x line with explicit Derby and DB2 database plugins; adding only the driver would have moved the failure one step later.
- Removing the hard-coded Derby `spring.datasource.driver-class-name` lets Boot auto-detect Derby locally and DB2 in the primary infra lane from the datasource URL/runtime classpath.
- The discriminating runtime check is now green for this slice: the packaged WAR reaches Hikari/Flyway DB2 connectivity and fails only on `Connection refused` when no DB2 listener is present, which hands the remaining blocker cleanly to `t20.3`.
- Learnings consumed: [backend/jdbc-keygen-for-boot-jpa, backend/boot-war-shell-and-jsf-scan-boundary]

## [t20.3.1] Split vendor-specific Flyway baselines from shared follow-on migrations for DB2 startup
- The DB2 startup blocker from `t20.3` was controlled entirely by Flyway location composition: Boot loaded `common/V1__baseline.sql` and `db2/V1__baseline.sql` together, so startup failed before any application code ran.
- The durable fix was to keep vendor-specific baseline folders (`common` for Derby, `db2` for DB2) and move vendor-neutral follow-on migrations into a separate shared folder that both vendors load.
- Focused validation passed immediately once the config stopped combining both baseline folders, and the packaged WAR then started cleanly against the repo-provided DB2 container.
- DB2 still logs non-fatal read-only warning `4474` from the scheduled summary path after startup; that is separate from the Flyway ownership slice.
- Learnings consumed: [backend/flyway-10-db2-and-derby-plugins]

## [t20.4.1] Restored the DB2 bootstrap path without reopening normal config/admin access
- The local regression was at the shared `/config` interceptor, not in the DB utility itself: after the auth hardening slice, anonymous `buildDB` requests were stopped before the preserved bootstrap action could seed `uid:0` and `s:*` on a fresh lane.
- The stable fix was to keep `/config` operator-only by default and carve out one narrow anonymous bypass for `action=buildDB` while the canonical seed markers are absent; once `uid:0` and `s:1` exist, anonymous `buildDB` returns to `401`.
- Focused MVC validation passed in `TradeConfigControllerTest`, and live DB2 validation showed the packaged WAR could seed the lane, return the preserved `Welcome to DayTrader` login surface for `uid:0 / xxx`, and serve populated `s:1` quotes afterward.
- The long-running utility response makes direct anonymous `buildDB` probes look idle for a while; the discriminating live signal was that the request reached the controller and subsequent login/quote probes succeeded against the same fresh lane.
- Learnings consumed: [backend/boot-mvc-operator-surfaces, backend/session-boundary-for-rest-and-streaming]

## [t21.2.1] Repaired seeded DB2 parity at the remaining Boot compatibility edges
- The last seeded-lane defects were controlled by compatibility adapters, not by the application-service seam: `/jaxrs/sync/*` still depended on inactive `javax.ws.rs` resources, and `/scenario` still relied on legacy dispatcher-query behavior that was too brittle on the Boot stack.
- The stable fix was to own those preserved URLs explicitly in Spring MVC: add a Boot echo controller for the primitive JAX-RS paths, and have `TradeScenarioController` call the Boot `/app` controller directly with overlaid request parameters instead of building query-string includes.
- Live DB2 validation showed `/config?action=buildDBTables` reached the controller instead of returning `401`, `/config?action=resetTrade` returned success on the run-stats JSP body, `/jaxrs/sync/*` echoed correctly, and `/scenario?action=l` rendered the DayTrader home surface after adding a canonical seeded-user fallback.
- The first live scenario rerun still landed on the login surface because the random scenario user failed authentication on the current seeded lane; retrying the preserved login path with canonical `uid:0 / xxx` fixed the parity gap without reopening the trading seam.
- Learnings consumed: [backend/boot-mvc-operator-surfaces, backend/db2-bootstrap-only-public-before-seeding, backend/mvc-app-session-compatibility, backend/session-boundary-for-rest-and-streaming]

## [t21.4.1] Closed the final docs, primitive-route, and image-home parity gaps on the Boot WAR
- The missing primitive launcher URLs were controlled by the active Boot compatibility boundary, not by servlet scanning: `pom.xml` intentionally excludes `web/prims/**`, so the correct fix was to extend `PrimitiveCompatibilityController` instead of reviving the legacy `javax.servlet` sources.
- The missing docs failure was literal asset absence in `src/main/webapp/docs`; packaged PDF placeholders on the preserved legacy paths were enough to restore the runtime contract expected by the launcher and docs page.
- The `JSP-Images` home `500` came from `tradehomeImg.jsp` including `marketSummary.jsp`, which then performed a runtime `jsp:include` of `marketSummary.html`; switching that second hop to a static JSP include kept rendering on the writer pipeline and removed the output-stream conflict.
- Focused validation passed first with `PrimitiveCompatibilityControllerTest`, then on the live DB2 proof lane with `t21.4-runtime-proof.ps1` reporting `74/74` checks green.
- Learnings consumed: [backend/boot-war-shell-and-jsf-scan-boundary, backend/boot-scenario-and-jaxrs-compat-adapters, backend/mvc-app-session-compatibility]