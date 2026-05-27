# Project Structure

## Project Type

- Single-module Java EE 8 application packaged as a WAR
- Maven build with Liberty runtime plugin
- Primary deployable: `io.openliberty.sample.daytrader8.war`

## Surface Size

- Java classes: 141
- JSP views: 23
- XHTML views: 15
- HTML pages: 20
- Unit or integration test sources under `src/test`: none found

## Functional Domains

1. Trading application flows
   Login, registration, home, quote lookup, buy, sell, portfolio, account update, logout,
   market summary, and scenario-driving flows.
2. Multi-interface web presentation
   Servlet + JSP flows and JSF + XHTML flows coexist for the same trading capability set.
3. Quote and market streaming
   JAX-RS quote lookup, server-sent event style broadcasting, and WebSocket-based market summary
   and quote-change updates.
4. Asynchronous order processing
   Buy/sell flows can complete synchronously or through JMS queue/topic processing backed by MDBs.
5. Primitive benchmark endpoints
   Numerous servlet, EJB, CDI, bean validation, JDBC, HTTP/2, and WebSocket primitive endpoints
   exist for performance and container feature benchmarking.
6. Runtime configuration and data bootstrap
   Liberty server descriptors, Derby resource copying, DB2 alternative config, prebuilt Derby data,
   and shell scripts for server flavor switching.

## Layering

## Entry Points

- Servlet controllers under `web.servlet`
- JSF backing beans and validators under `web.jsf`
- JAX-RS resources under `jaxrs`
- WebSocket endpoints under `web.websocket` and primitive endpoints under `web.prims`
- Filters and listener classes for login gating, order alerts, and context lifecycle

## Application-Service Boundary

- `TradeServices` is the shared business contract used by servlet, JSF, REST, and WebSocket entry
  points.
- Runtime mode is selected centrally through CDI `Instance<TradeServices>` resolution using
  `TradeRunTimeModeLiteral` and `TradeConfig`.

## Service Implementations

- `impl.ejb3.TradeSLSBBean`: container-managed EJB/JPA/JMS implementation
- `impl.direct.TradeDirect`: direct JDBC + JMS + user transaction implementation
- `impl.session2direct.DirectSLSBBean`: session bean facade delegating to direct JDBC mode

## Persistence Layer

- JPA persistence unit `daytrader` using JTA datasource `jdbc/TradeDataSource`
- Five explicit entities: account, account profile, holding, order, quote
- Mixed access patterns: JPA entity manager and direct JDBC SQL coexist in different runtime modes

## Observed Boundary Quality

- Positive: application behavior is mostly routed through `TradeServices`, which limits the amount
  of controller rewrite coupling.
- Negative: runtime-specific behavior is spread through scheduling, JMS, CDI events, JNDI lookups,
  servlet session handling, and deployment descriptors, so replacing the container is still a
  cross-cutting rewrite.