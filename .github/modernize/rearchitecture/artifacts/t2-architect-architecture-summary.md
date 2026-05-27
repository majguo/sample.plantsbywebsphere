# Architecture Summary

## Core Control Flow

The application has multiple presentation adapters that converge on one business contract:

- Servlet/JSP: `TradeAppServlet` dispatches `action` parameters to `TradeServletAction`
- JSF/XHTML: session-scoped backing beans such as `TradeAppJSF` call `TradeServices`
- REST: `JAXRSApplication` exposes `/rest`, and `QuoteResource` serves `/rest/quotes`
- WebSocket: `MarketSummaryWebSocket` exposes `/marketsummary` and pushes market updates

Each adapter resolves the active `TradeServices` implementation through CDI and `TradeConfig`
runtime mode selection. This is the strongest architectural seam in the codebase.

## Runtime Modes

`TradeConfig` supports three business-service implementations:

1. `Full EJB3`
   Primary container-managed implementation using EJB, JPA, JMS, and scheduled singleton logic.
2. `Direct (JDBC)`
   JDBC-first implementation using datasource access, manual SQL, `UserTransaction`, JMS, and
   managed executor services.
3. `Session to Direct`
   Session bean facade over the direct JDBC implementation.

This means the current application is already partly abstracted from one concrete runtime, but the
abstraction stops at the service layer. Infrastructure behavior remains container-defined.

## Cross-Cutting Runtime Behavior

- Scheduling: `MarketSummarySingleton` uses EJB `@Schedule`
- Async order completion: JMS messages are consumed by MDBs
- Event fan-out: CDI async events and managed executor services propagate quote/market updates
- Session state: servlet and JSF flows store `uidBean` and related state directly in HTTP session
- UI navigation: JSP dispatch tables and `faces-config.xml` navigation rules both carry user flow
  behavior
- Security/auth: application-managed login checks dominate; container auth in `web.xml` is present
  only as commented-out configuration

## Architectural Assessment

- Good reuse: business operations are centralized in `TradeServices`
- High runtime coupling: EJB, JMS, MDB, CDI events, managed executors, JNDI, and Liberty resources
  define current behavior
- Presentation duplication: JSP and JSF stacks implement overlapping flows, increasing parity
  workload for the rewrite
- Benchmark primitives are intermixed with production-like trading flows, so the target design will
  need an explicit keep-or-isolate decision during planning

## Most Important Architectural Conclusion

The migration should be organized around preserving contracts at the adapter boundary and replacing
container services underneath them. Rewriting controller endpoints first or persisting the Liberty
runtime model inside Spring Boot would both create unnecessary risk.