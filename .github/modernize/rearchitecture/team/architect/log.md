## [t2] Current architecture and migration risks analyzed
- `TradeServices` is the strongest reusable seam in the codebase; servlet, JSF, REST, and WebSocket
  adapters all resolve it through runtime-mode selection.
- The hard part of the migration is replacing container behavior: EJB transactions, MDB/JMS,
  scheduled singleton work, CDI async events, and Liberty-managed resources.
- Presentation parity scope is larger than it first appears because JSP and JSF flows coexist for
  the same trading capabilities, alongside REST and WebSocket endpoints.
- Smoke baseline: `mvn -B -DskipTests clean package` succeeds; first-run `liberty:run` startup was
  still provisioning features when probed, so HTTP readiness remained inconclusive in-task.
- Learnings consumed: [(none)]

## [t2.1] Resolved runtime replacement risks into Spring Boot 3 design constraints
- The source-controlled runtime seam is still `TradeServices`, but the parity-sensitive behavior is
  defined one layer lower by session attributes, deferred order semantics, event fan-out, and
  Liberty resource contracts.
- Current async behavior is split: EJB mode uses JMS/MDB rollback-sensitive processing, while the
  direct path uses managed executors and cached refresh logic. The target design cannot flatten
  those into a vague “async service” without explicit lifecycle rules.
- Session compatibility is concrete: `uidBean`, `sessionCreationDate`, protected-page fallback, and
  logout session invalidation are the observable contract.
- Smoke evidence: `mvn -B -DskipTests clean package` failed because a live Java process locked the
  embedded Derby file under `target`; `mvn -B -DskipTests package` succeeded; `http://localhost:9080/daytrader`
  returned 200.
- Learnings consumed: [architect/trade-services-migration-seam]

## [t2.1] Runtime risks converted into downstream design constraints
- The constraint set is now explicit: one application-service seam, one auth/session boundary, one
  canonical transactional write model, one explicit async order-processing model, one canonical
  domain-event fan-out path, and one authoritative Boot configuration model.
- `TradeConfig`, `TradeAppServlet`, `TradeServletAction`, `TradeAppJSF`, `QuoteResource`,
  `DTBroker3MDB`, and Liberty `server.xml` were used as source anchors for the preserved runtime
  contracts.
- Fresh smoke validation: `pwsh -File scripts/smoke-clean-build.ps1` passed; a new
  `mvn -B liberty:run -DskipTests` startup attempt stayed in Liberty feature installation long
  enough that `http://localhost:9080/daytrader` still refused connections during the probe window,
  so startup remains time-window sensitive rather than disproven.
- Learnings consumed: [architect/explicit-runtime-contracts-for-boot-target,
  architect/trade-services-migration-seam]

## [t4] Spring Boot 3 target architecture fixed around compatibility-owned seams
- The Boot target is now explicit: executable WAR packaging, one `TradeServices`-compatible
  application facade, MVC compatibility controllers for `/app`, `/config`, and `/scenario`, a
  dedicated JSF compatibility slice, one canonical JPA transaction model, a durable DB-backed
  async order-work queue, and one streaming hub for SSE/WebSocket fan-out.
- The design deliberately preserves session markers `uidBean` and `sessionCreationDate`, the
  existing `/daytrader` context root, current operator-visible runtime toggles, and direct
  reachability of JSP/XHTML/primitive surfaces.
- External references confirmed two non-obvious constraints worth carrying forward: Spring Boot
  servlet apps need WAR packaging for reliable `src/main/webapp` + JSP support, and JSF parity on
  Boot requires an explicit compatibility integration rather than hoping MVC can absorb XHTML pages.
- Fresh smoke validation: `pwsh -File scripts/smoke-clean-build.ps1` passed; `mvn -B liberty:run
  -DskipTests` again remained in feature installation long enough that the in-task probe to
  `http://localhost:9080/daytrader` still saw connection refused.
- Learnings consumed: [architect/explicit-runtime-contracts-for-boot-target,
  architect/trade-services-migration-seam]

## [t17] Architecture review found remaining conformance gaps in the Boot rewrite
- Structural alignment is good on WAR packaging, `/daytrader` context root, the canonical
  `TradeServices` seam, the JSF compatibility slice, and the durable `ORDERWORKEJB` async worker.
- The review failed on three HIGH findings: streaming adapters are still outside the authenticated
  session boundary, market-summary push cadence is fixed to a Boot property instead of the mutable
  runtime settings model, and the main build still compiles a broad `javax.*` surface through
  Java EE 8 compatibility dependencies.
- The highest-value local follow-up is to fix streaming auth/session enforcement in the Boot-owned
  SSE and WebSocket path instead of reopening the application seam or persistence design.
- Learnings consumed: [architect/explicit-runtime-contracts-for-boot-target,
  architect/spring-boot-war-and-jsf-compat-layer, architect/trade-services-migration-seam]

## [t19.1] Smoke rerun stayed green after remediation
- Post-remediation smoke evidence is still green on the packaged Spring Boot WAR under JDK 17:
  full root packaging passed, Tomcat bound `/daytrader` on port `19087`, and the HTTP probe
  returned `200`.
- The first rerun using `-DskipTests` was not reliable in this shell session because Maven still
  entered the test phase; the clean architect-owned build verdict came from rerunning with
  `-Dmaven.test.skip=true`.
- The only residual runtime risk remains the existing Derby `10.14` versus Hibernate `6.4.8.Final`
  compatibility warning; it did not block build or startup.
- Learnings consumed: [architect/smoke-verify-windows-jdk17-war,
  architect/explicit-runtime-contracts-for-boot-target,
  architect/spring-boot-war-and-jsf-compat-layer]

## [t19] Smoke verification passed on the packaged Spring Boot WAR under JDK 17
- Full root build passed with `cmd /d /c "...&& mvn -DskipTests clean package"`, producing the
  repackaged executable WAR without plugin failures.
- Independent runtime verification passed on port `19086`: Tomcat bound `/daytrader`, startup
  completed in `18.868 s`, and `http://127.0.0.1:19086/daytrader/` returned `200`.
- The main residual risk is environment compatibility rather than startup correctness: Hibernate
  `6.4.8.Final` still warns that Derby `10.14` is below its supported minimum `10.15.2`.
- On Windows, the deterministic smoke-build path is `cmd /d /c` with JDK 17 exported first;
  interrupted `mvn.cmd` runs from PowerShell can drop into `Terminate batch job` and muddy the
  verdict.
- Learnings consumed: [architect/explicit-runtime-contracts-for-boot-target,
  architect/spring-boot-war-and-jsf-compat-layer]

## [t17.2] Re-review confirmed the remediation closes the three architecture blockers
- The Boot rewrite now enforces one `CompatibilitySessionFacade`-owned boundary across `/config`,
  `/rest/quotes/**`, `/rest/broadcastevents`, and the `/marketsummary` WebSocket handshake.
- `MarketSummaryPublisher` now derives publication timing from `RuntimeSettingsService`, restoring
  `/config` as the authoritative operator surface for streaming cadence.
- The active Boot compilation path no longer carries non-JDK `javax.*` usage; dormant legacy
  packages remain only behind Maven compiler exclusions.
- Focused validation under JDK 17 passed with `BUILD SUCCESS`; Maven exercised 24 tests in the
  targeted slice despite the narrow `-Dtest` selector.
- Learnings consumed: [architect/explicit-runtime-contracts-for-boot-target,
  architect/streaming-auth-boundary-must-be-explicit, backend/session-boundary-for-rest-and-streaming]