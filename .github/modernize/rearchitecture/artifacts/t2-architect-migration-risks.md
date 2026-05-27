# Migration Risks

## CRITICAL

1. Java EE container replacement is cross-cutting
   Current behavior depends on EJB transactions, MDB listeners, Liberty JMS resources, CDI events,
   scheduled singleton execution, JPA injection, managed executors, and servlet/JSF runtime glue.
   Spring Boot 3 replaces the execution model, not just the API packages.
   Mitigation: design the target around Spring-managed equivalents by concern and validate each
   parity slice vertically.

2. Async order-processing semantics may drift
   Buy/sell flows can run sync, async, or async 2-phase and depend on JMS queue delivery and MDB
   completion behavior.
   Mitigation: capture order state transitions, queue semantics, and failure behavior before any
   rewrite; design explicit Spring messaging/scheduling replacements instead of implicit async
   helpers.

3. Mixed presentation technologies expand parity scope
   Equivalent trading flows exist across servlet/JSP and JSF/XHTML paths, alongside REST and
   WebSocket surfaces.
   Mitigation: inventory parity expectations per surface and decide in target design whether both UI
   stacks are preserved, consolidated, or one is treated as compatibility-only.

## HIGH

1. `javax.*` to Jakarta migration affects nearly every source file
   The codebase uses `javax.servlet`, `javax.persistence`, `javax.ejb`, `javax.ws.rs`,
   `javax.websocket`, `javax.enterprise`, and related namespaces throughout.
   Mitigation: treat package migration as part of the rewrite foundation, not as isolated cleanup.

2. Persistence behavior is split between JPA and direct JDBC
   The same business contract can execute through entity manager logic or direct SQL against the
   same schema.
   Mitigation: preserve one canonical transactional model in the Spring target and characterize any
   behavior that differs by runtime mode before collapsing implementations.

3. Session/auth behavior is application-managed and implicit
   Login state is stored directly in HTTP session attributes and protected via filters and manual
   checks instead of an actively configured container auth contract.
   Mitigation: treat existing behavior as a custom session-auth contract to preserve, then map it
   intentionally onto Spring Security or an equivalent filter chain.

4. Liberty configuration is the source of truth for operational resources
   Datasource, JMS queue/topic, activation specs, port bindings, and app deployment live in
   `server.xml`.
   Mitigation: extract resource contracts explicitly before target design so Boot configuration does
   not silently change names, pool semantics, or messaging destinations.

5. Benchmark primitives share the deployable with core trading flows
   Primitive endpoints cover CDI, EJB, bean validation, HTTP/2, session, JDBC, and WebSocket test
   scenarios.
   Mitigation: keep them in scope per constitution, but isolate them in planning so they do not
   obscure the main trading migration sequence.

## MEDIUM

1. Startup baseline was not fully observed in-task
   Build succeeded, but Liberty first-run startup was still provisioning features when probed.
   Mitigation: rerun startup verification in a longer-lived validation step and capture final bind
   and health evidence.

2. Test automation is performance-heavy rather than behavior-focused
   JMeter assets exist, but unit and integration tests are absent.
   Mitigation: downstream plan must add characterization and smoke coverage before invasive rewrites.