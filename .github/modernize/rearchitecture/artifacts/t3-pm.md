# t3 - Feature Inventory and Parity Requirements

> Governed by: `./constitution.md`

**Feature Branch**: `brownfield-daytrader8-spring-boot-3`  
**Created**: 2026-05-27  
**Status**: Draft  
**Input**: Inventory user flows, pages, endpoints, and parity requirements for the DayTrader8 migration to Spring Boot 3.

## Scope Baseline

- **Discovery method**: Read governing artifacts, then traced the user-visible surface from `/app`, `/config`, `/scenario`, REST/SSE/WebSocket entry points, `TradeConfig` page mappings, and directly addressable `src/main/webapp` pages.
- **Total items discovered**: 60+ addressable pages/endpoints across 6 surface families.
- **Items in scope**: All discovered surfaces. The clarification and constitution both require full parity with no exclusions.
- **Surface families in scope**:
  - Primary trading experience: login, registration, home, account, portfolio, quotes, buy, sell, market summary, logout.
  - Benchmark and administration: runtime configuration, reset/rebuild database utilities, scenario driver, run statistics.
  - Streaming/API surfaces: quote REST API, broadcast/SSE feed, market-summary WebSocket, primitive JAX-RS echo services.
  - Primitive servlet and JSF test pages: all directly linked workload primitives under `web_prmtv.html`.
  - Landing and navigation shell: framed landing page, header/footer navigation, overview content, FAQ/docs links.
  - Alternate presentation variants: image-based JSP variants and directly addressable JSF/XHTML pages where present.

## Assumptions

- The migration must preserve the current URL structure and observable request/response behavior for all publicly addressable surfaces under the application context.
- Alternate presentation variants that are not the default web interface still count toward parity if they remain directly addressable today.
- Primitive and benchmark surfaces are part of the product scope because they are shipped in the current application and are explicitly linked from the UI.
- Authentication must preserve the current session-based login/logout behavior and the requirement that protected trading actions redirect or fall back to the welcome/login experience when the user session is missing.

## User Scenarios & Testing

### User Story 1 - Trader Signs In and Reaches a Ready-to-Trade Home Screen (Priority: P1)

An existing trader opens the trading application, authenticates with user ID and password, and lands on a personalized home page that shows account statistics, holdings summary, and navigation to the rest of the trading experience.

**Why this priority**: This is the entry point for the benchmark’s main user journey and gates every protected trading action.

**Independent Test**: Can be tested by loading the welcome page, submitting credentials, and confirming the authenticated home page renders personalized account data and trading navigation.

**Acceptance Scenarios**:

1. **Given** an unauthenticated visitor on the welcome page, **When** they submit valid credentials, **Then** the system starts an authenticated session and shows the home page with account summary and trade navigation.
2. **Given** an unauthenticated visitor, **When** they submit invalid credentials, **Then** the system keeps them on the login/welcome surface and shows a visible failure message instead of entering the trading area.
3. **Given** a visitor requests a protected trading action without a valid session, **When** the request is processed, **Then** the system falls back to the welcome/login experience and reports that the user is not logged in.

---

### User Story 2 - New Trader Registers and Immediately Becomes a Signed-In User (Priority: P1)

A first-time user completes the registration form, creates an account with opening balance and profile details, and is taken into the trading experience without needing a separate manual sign-in step.

**Why this priority**: Registration is a first-class entry path and is also exercised by the scenario driver.

**Independent Test**: Can be tested by opening the registration page, submitting all required fields, and confirming the new account is created and the user lands in the authenticated trading area.

**Acceptance Scenarios**:

1. **Given** a first-time user on the registration page, **When** they submit a complete form with matching passwords, **Then** the system creates the account and transitions them into the signed-in trading experience.
2. **Given** a first-time user on the registration page, **When** the passwords do not match, **Then** the system keeps them on the registration page and shows a visible registration failure message.
3. **Given** a registration attempt fails server-side, **When** the response returns, **Then** the user remains on the registration surface with a clear failure result and their entered data preserved where the current UI preserves it.

---

### User Story 3 - Trader Looks Up Quotes and Places Buy Orders (Priority: P1)

An authenticated trader requests one or more stock symbols, reviews quote data, and submits a buy order for a selected symbol and quantity.

**Why this priority**: Quote lookup and buy submission are core benchmark behaviors and part of the canonical trade loop.

**Independent Test**: Can be tested by signing in, requesting a known quote list, and submitting a buy request from the quotes page.

**Acceptance Scenarios**:

1. **Given** an authenticated trader, **When** they open the quotes/trade page with one or more symbols, **Then** the system shows quote rows including symbol, company, volume, open price, current price, and gain/loss.
2. **Given** an authenticated trader viewing quotes, **When** they submit a buy with symbol and quantity, **Then** the system creates an order and shows an order confirmation/details page.
3. **Given** a trader clicks any quote symbol link, **When** the request is processed, **Then** the system shows the quote/trade surface for that symbol so the user can inspect it or trade it.

---

### User Story 4 - Trader Reviews Portfolio and Sells Holdings (Priority: P1)

An authenticated trader opens their portfolio, reviews holdings and gain/loss values, and sells a holding from that portfolio.

**Why this priority**: Portfolio review and sell are the other half of the core trade loop and are used by the scenario mix.

**Independent Test**: Can be tested by signing in as a user with holdings, loading the portfolio page, and invoking a sell action from a holding row.

**Acceptance Scenarios**:

1. **Given** an authenticated trader with holdings, **When** they open the portfolio page, **Then** the system lists holdings with purchase date, quantity, purchase price, current price, market value, gain/loss, and a sell action.
2. **Given** an authenticated trader with no holdings, **When** they open the portfolio page, **Then** the system still renders the portfolio surface and shows that the portfolio is empty.
3. **Given** an authenticated trader selects sell for a holding, **When** the request is processed, **Then** the system submits the sell order and shows the order details/confirmation surface.

---

### User Story 5 - Trader Reviews Account History and Updates Profile (Priority: P1)

An authenticated trader opens the account page, inspects account balances and recent orders, optionally expands to all orders, and updates editable profile fields.

**Why this priority**: Account management is a core authenticated flow and contains visible validation rules that must survive migration.

**Independent Test**: Can be tested by signing in, loading the account page, toggling the all-orders view, editing profile fields, and verifying success and validation failures.

**Acceptance Scenarios**:

1. **Given** an authenticated trader on the account page, **When** the page renders, **Then** it shows account metadata, balances, recent orders, and the editable profile form.
2. **Given** an authenticated trader on the account page, **When** they request all orders, **Then** the system expands the order history beyond the default recent subset.
3. **Given** an authenticated trader editing profile data, **When** they submit matching passwords and all required fields, **Then** the system persists the changes and shows a success result on the account page.
4. **Given** an authenticated trader editing profile data, **When** passwords do not match or required fields are blank, **Then** the system keeps them on the account page and shows the corresponding validation error message.

---

### User Story 6 - Trader Monitors Market Summary and Order Alerts (Priority: P2)

An authenticated trader views the market summary surface and sees market index, trading volume, top gainers, top losers, and recent price changes, while completed-order alerts remain visible across trading pages when enabled.

**Why this priority**: This is a shared read-only market intelligence surface that appears throughout the main experience and has both page and streaming representations.

**Independent Test**: Can be tested by signing in, opening market summary, and verifying the summary tables and order-alert banner behavior after order completion.

**Acceptance Scenarios**:

1. **Given** an authenticated trader, **When** they open the market summary page, **Then** the system shows market index, trading volume, top gainers, top losers, and recent price changes.
2. **Given** completed orders exist and order alerts are enabled, **When** any main trading page renders, **Then** the page shows the completed-order alert table.
3. **Given** the market-summary streaming surface is used, **When** market data updates arrive, **Then** the displayed summary values refresh without requiring a full page reload.

---

### User Story 7 - Administrator or Benchmark Operator Prepares and Resets the Environment (Priority: P2)

An operator opens the configuration utilities, reviews current runtime settings, updates allowed runtime parameters, resets the trade runtime, recreates database tables, or repopulates the sample database.

**Why this priority**: These surfaces are required to initialize and repeatedly exercise the sample/benchmark environment.

**Independent Test**: Can be tested by opening the configuration utilities and invoking each supported admin action separately.

**Acceptance Scenarios**:

1. **Given** an operator opens the configuration page, **When** the page renders, **Then** it shows the current runtime settings and editable controls for the supported parameters.
2. **Given** an operator submits configuration changes, **When** the update succeeds, **Then** the page redisplays the current configuration with a visible success status.
3. **Given** an operator invokes reset, rebuild tables, or repopulate database, **When** the action completes, **Then** the system returns an explicit status/result page for that action.
4. **Given** the database product cannot be identified for table creation, **When** the rebuild-tables action runs, **Then** the system returns a visible failure message instead of silently succeeding.

---

### User Story 8 - Benchmark Operator Exercises Scenario and Primitive Test Surfaces (Priority: P2)

An operator uses the scenario driver and the linked primitive pages to step through synthetic trade activity and exercise servlet, JSP, JSF, WebSocket, JSON, JDBC, CDI, EJB, async, and related runtime behaviors shipped with the application.

**Why this priority**: These surfaces are explicitly shipped for workload generation and functional verification and are part of the current product footprint.

**Independent Test**: Can be tested by loading the scenario URL and a representative sample from each primitive family, then verifying that each page or endpoint still responds with its advertised behavior.

**Acceptance Scenarios**:

1. **Given** an operator opens `/scenario`, **When** they refresh repeatedly or specify a scenario action, **Then** the system executes the corresponding synthetic trade step and renders the resulting trading surface.
2. **Given** an operator opens `web_prmtv.html`, **When** they select a primitive link, **Then** the corresponding primitive surface or endpoint responds with the documented test behavior.
3. **Given** an operator opens a WebSocket or JSON streaming primitive page, **When** they connect and send the page’s built-in test messages, **Then** the page reflects live response counters or echoed data.

---

### Edge Cases

- What happens when a protected `/app?action=...` request arrives without a valid session? The system must return to the welcome/login surface and display a “User Not Logged in” style message.
- What happens when login credentials do not resolve to an account? The system must remain on the login/welcome surface and show a visible failure result.
- What happens when registration passwords do not match? The system must stay on the registration page and show the registration error.
- What happens when account profile submission has mismatched passwords or blank required fields? The system must stay on the account page and show a specific update error message.
- What happens when a portfolio is empty? The portfolio page must still render and indicate that the user has no holdings to sell.
- What happens when a user requests all orders while long-run support suppresses the expensive query? The visible order-history behavior must match the current runtime mode and configuration.
- What happens when config-driven database actions cannot determine the database type or fail to access the database? The operator must get an explicit failure result in the response body.
- What happens when a primitive streaming connection closes or errors? The page must reflect the disconnected state and stop updating counters until the user reconnects.

## Requirements

### Functional Requirements

- **REQ-001**: The migrated system MUST preserve the framed landing shell and top-level navigation to overview content, trading, configuration utilities, primitives, and FAQ/documentation surfaces.
- **REQ-002**: The migrated system MUST preserve the unauthenticated welcome/login page at the current trading entry URL, including visible status/error messaging.
- **REQ-003**: The migrated system MUST authenticate existing traders using the current credential fields and establish a session that grants access to protected trading actions.
- **REQ-004**: The migrated system MUST reject invalid or unresolved login attempts without entering the authenticated trading area.
- **REQ-005**: The migrated system MUST preserve the registration page, required input fields, password-confirmation rule, opening-balance input, and immediate post-registration sign-in behavior.
- **REQ-006**: The migrated system MUST preserve the authenticated home page, including account statistics, holdings summary, quote lookup form, and the shared trading navigation bar.
- **REQ-007**: The migrated system MUST preserve the `quotes` trading flow so that one or more comma-delimited symbols return a quote/trade page with symbol, company, volume, price range, open price, current price, and gain/loss.
- **REQ-008**: The migrated system MUST preserve quote symbol deep-link behavior so that clicking a symbol from trading pages opens the corresponding quote/trade surface.
- **REQ-009**: The migrated system MUST preserve buy-order submission from the quote surface using the current symbol and quantity request contract.
- **REQ-010**: The migrated system MUST preserve the order-confirmation surface for newly submitted buy and sell orders, including order ID, status, timestamps, fee, type, symbol, and quantity.
- **REQ-011**: The migrated system MUST preserve the portfolio page, including holdings list, purchase metrics, market value, gain/loss, and per-holding sell actions.
- **REQ-012**: The migrated system MUST preserve empty-portfolio behavior by rendering the portfolio page with an explicit empty-state result rather than failing the request.
- **REQ-013**: The migrated system MUST preserve the account page, including account metadata, balances, recent-order history, and the “show all orders” expansion behavior.
- **REQ-014**: The migrated system MUST preserve account profile editing with the current editable fields and current validation rules for password mismatch and missing required values.
- **REQ-015**: The migrated system MUST preserve logout behavior by terminating the current authenticated session and returning the user to the welcome/login surface.
- **REQ-016**: The migrated system MUST preserve the market summary page, including stock index, trading volume, top gainers, top losers, and recent price changes.
- **REQ-017**: The migrated system MUST preserve completed-order alert visibility across trading pages when completed orders are present and alerts are enabled.
- **REQ-018**: The migrated system MUST preserve the configuration page and all currently editable runtime parameters, including order-processing mode, web interface mode, max users, max quotes, market-summary interval, primitive iterations, publish-quote-updates toggle, quote-change frequency, long-run toggle, and display-order-alerts toggle.
- **REQ-019**: The migrated system MUST preserve the configuration utility actions for updating runtime settings, resetting the trade runtime, rebuilding database tables/indexes, and repopulating the database.
- **REQ-020**: The migrated system MUST preserve operator-visible success and failure messages for all configuration and database utility actions.
- **REQ-021**: The migrated system MUST preserve the scenario-driver endpoint and its ability to execute login, logout, registration, home, account, profile update, quote, portfolio, buy, and sell steps through repeated requests.
- **REQ-022**: The migrated system MUST preserve the REST quote API contract at `/rest/quotes`, including GET by path symbols and POST by form field, returning JSON quote data for the requested symbol set.
- **REQ-023**: The migrated system MUST preserve the server-sent-events broadcast endpoint at `/rest/broadcastevents`, including initial welcome-or-recent-data behavior and ongoing streamed updates.
- **REQ-024**: The migrated system MUST preserve the market-summary WebSocket endpoint and the associated browser page behavior that refreshes summary data and recent quote changes without full-page reloads.
- **REQ-025**: The migrated system MUST preserve the primitive JAX-RS echo endpoints under `/jaxrs/sync`, including text, JSON, and XML echo responses.
- **REQ-026**: The migrated system MUST preserve the primitive launcher page and every directly linked primitive surface under it, including static HTML, servlet, JSP, JSF, session, JDBC, CDI, Bean Validation, async, upgrade, WebSocket, JSON/JSON-P, managed-thread/executor, and EJB/JMS demonstrations.
- **REQ-027**: The migrated system MUST preserve all currently reachable static documentation and informational pages linked from the shell, configuration, or FAQ surfaces.
- **REQ-028**: The migrated system MUST preserve the alternate JSP image-based pages and any directly reachable JSF/XHTML trading pages as supported parity surfaces unless a later approved artifact explicitly deprecates them.
- **REQ-029**: The migrated system MUST preserve current status-code semantics for the inventoried HTTP endpoints, including successful HTML/JSON/streaming responses and current server-error behavior for failed server-side actions.
- **REQ-030**: The migrated system MUST preserve session-dependent access control semantics for trading actions so that only login and registration remain available to unauthenticated users.

### Parity Surface Inventory

#### Canonical Trading Surface

- `/app` with no `action`: welcome/login surface.
- `/app?action=login`: sign-in submission.
- `/app?action=register`: registration submission.
- `/app?action=home`: authenticated home page.
- `/app?action=account`: account page.
- `/app?action=update_profile`: profile update submission.
- `/app?action=quotes&symbols=...`: quote lookup/trade page.
- `/app?action=buy&symbol=...&quantity=...`: buy submission.
- `/app?action=portfolio` and `/app?action=portfolioNoEdge`: portfolio page variants.
- `/app?action=sell&holdingID=...`: sell submission.
- `/app?action=mksummary`: market summary page.
- `/app?action=logout`: logout flow.

#### Admin and Benchmark Utility Surface

- `/config` with no `action`: current runtime configuration page.
- `/config?action=updateConfig`: runtime setting update.
- `/config?action=resetTrade`: reset runtime and show run statistics.
- `/config?action=buildDBTables`: rebuild schema/tables and indexes.
- `/config?action=buildDB`: repopulate the sample database.
- `/scenario`: synthetic scenario driver.
- `/scenario?action=<step>` where current steps include hello/no-op, login, logout, register, home, account, account update, quote, portfolio, buy, and sell.
- `configure.html`: operator utility launcher page.
- `runStats.jsp`: statistics result surface used after reset.

#### REST, SSE, and WebSocket Surface

- `/rest/quotes/{symbols}`: JSON quote lookup by GET.
- `/rest/quotes` with form field `symbols`: JSON quote lookup by POST.
- `/rest/broadcastevents`: server-sent events feed for recent quote changes.
- `/marketsummary`: WebSocket endpoint for market-summary updates.
- `/jaxrs/sync/echoText`, `/jaxrs/sync/echoJSON`, `/jaxrs/sync/echoXML`: primitive synchronous JAX-RS echo services.
- `/pingTextSync`, `/pingTextAsync`, `/pingBinary`, `/pingWebSocketJson`: primitive WebSocket endpoints.

#### Landing, Documentation, and Alternate Presentation Surface

- `index.html`: framed landing page.
- `header.html`, `footer.html`, `contentHome.html`, `leftMenu.html`: shell/navigation content.
- `welcome.jsp`, `register.jsp`, `tradehome.jsp`, `account.jsp`, `portfolio.jsp`, `quote.jsp`, `order.jsp`, `marketSummary.jsp`, `config.jsp`: canonical JSP pages.
- `welcomeImg.jsp`, `registerImg.jsp`, `tradehomeImg.jsp`, `accountImg.jsp`, `portfolioImg.jsp`, `quoteImg.jsp`, `orderImg.jsp`: image-based JSP variants.
- `index.xhtml`, `welcome.xhtml`, `register.xhtml`, `tradehome.xhtml`, `account.xhtml`, `portfolio.xhtml`, `quote.xhtml`, `order.xhtml`, `config.xhtml`, `configure.xhtml`, `web_prmtv.xhtml`, `docs/tradeFAQ.xhtml`: directly reachable JSF/XHTML variants.
- `docs/*`: FAQ, benchmarking, glossary, runtime characteristics, version, and documentation pages linked from the UI.

#### Primitive Launcher Inventory

- Static/Servlet/JSP/JSF primitives: `PingHtml.html`, `PingJsp.jsp`, `PingJspEL.jsp`, `PingJsf.faces`, `PingCDIJSF.faces`, `/servlet/PingServlet`, `/servlet/PingServletWriter`, `/servlet/PingServlet2Include`, `/servlet/PingServlet2Servlet`, `/servlet/PingServlet2Jsp`, `/servlet/PingServlet2PDF`, `/servlet/PingServlet2DB`, `/servlet/ExplicitGC`.
- Session/JDBC/runtime primitives: `/servlet/PingSession1`, `/servlet/PingSession2`, `/servlet/PingSession3`, `/servlet/PingJDBCRead`, `/servlet/PingJDBCRead2JSP`, `/servlet/PingJDBCWrite`, `/servlet/PingServlet2JNDI`, `/servlet/PingUpgradeServlet`, `/servlet/PingManagedThread`, `/servlet/PingManagedExecutor`.
- CDI/Bean Validation/JSON primitives: `/servlet/PingServletCDI`, `/servlet/PingServletCDIBeanManagerViaJNDI`, `/servlet/PingServletCDIBeanManagerViaCDICurrent`, `/servlet/PingServletCDIEvent`, `/servlet/PingServletCDIEventAsync`, `/servlet/PingServletBeanValSimple1`, `/servlet/PingServletBeanValSimple2`, `/servlet/PingServletBeanValCDI`, `/servlet/PingJSONPObject`, `/servlet/PingJSONPObjectFactory`, `/servlet/PingJSONPStreaming`.
- Async/HTTP2 and miscellaneous primitives: `/servlet/PingServlet30Async`, `/servlet/PingServlet31Async`, `/servlet/PingServlet31AsyncRead`, `/servlet/PingServletLargeContentLength`, `/servlet/PingServletSetContentLength`, `/PingServletHttpSimple`, `/PingServletPush`, `/drive/PingServlet`.
- EJB/JMS primitives: `/ejb3/PingServlet2Session`, `/ejb3/PingServlet2Entity`, `/ejb3/PingServlet2Session2Entity`, `/ejb3/PingServlet2Session2Entity2JSP`, `/ejb3/PingServlet2Session2EntityCollection`, `/ejb3/PingServlet2Session2CMROne2One`, `/ejb3/PingServlet2Session2CMROne2Many`, `/ejb3/PingServlet2MDBQueue`, `/ejb3/PingServlet2MDBTopic`, `/ejb3/PingServlet2TwoPhase`.

### Key Entities

- **Trader Account**: The signed-in user’s account identity, balances, login/logout counts, creation dates, and open balance.
- **Trader Profile**: Editable user-facing profile data including user ID, full name, password, address, email, and credit-card display value.
- **Quote**: Market data for a stock symbol including company name, volume, open price, current price, high/low range, and change.
- **Holding**: A trader’s owned position in a quote, including purchase date, quantity, purchase price, and derived market value/gain.
- **Order**: A submitted buy or sell transaction with order ID, status, timestamps, fees, symbol, quantity, and price.
- **Market Summary**: Aggregate market indicators including stock index, trading volume, top gainers, top losers, and recent price changes.
- **Runtime Configuration**: Operator-managed settings that change behavior such as order mode, page mode, scale values, update intervals, and feature toggles.
- **Scenario Step**: A synthetic benchmark action executed by the scenario driver to emulate user traffic.

## Success Criteria

### Measurable Outcomes

- **SC-001**: 100% of inventoried primary trading journeys can be executed end-to-end after migration using the same visible URLs, forms, and navigation paths.
- **SC-002**: 100% of inventoried admin, benchmark, REST, SSE, and WebSocket entry points remain reachable and return the same class of observable output as the current application.
- **SC-003**: 100% of visible validation and fallback behaviors documented in this artifact produce equivalent user-facing messages or states after migration.
- **SC-004**: Existing traders can complete sign-in, quote lookup, buy, portfolio review, sell, account update, and logout without losing current task capabilities or sequence ordering.
- **SC-005**: Operators can reset, configure, rebuild, and repopulate the environment without losing any currently exposed utility action.
- **SC-006**: Primitive and scenario surfaces required for benchmark or runtime verification remain available with no missing linked destination from the primitive launcher.
- **SC-007**: No inventoried parity surface is left unaccounted for in downstream design and planning artifacts.

## Requirements Quality Checklist

## Requirement ID Coverage
- [x] All requirements use REQ-XXX format
- [x] IDs are unique and sequential

## Testability
- [x] Every requirement is independently testable
- [x] Acceptance criteria are concrete (Given-When-Then)

## Completeness
- [x] Scope Baseline section complete
- [x] User Scenarios prioritized (P1, P2)
- [x] Functional requirements cover all in-scope items
- [x] Success criteria are measurable

## Constitution Alignment
- [x] All constitution principles referenced
- [x] Migration mode constraints respected