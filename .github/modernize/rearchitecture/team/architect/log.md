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