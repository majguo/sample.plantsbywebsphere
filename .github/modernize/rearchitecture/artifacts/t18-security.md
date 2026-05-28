# t18 - Security audit: authentication, validation, secrets, and dependencies

## Verdict

FAIL - The Spring Boot 3 rewrite is not security-ready. The audit found 2 CRITICAL and 1 HIGH issue in the migrated authentication boundary and secret-handling path.

## Scope

- Reviewed the Boot-owned auth/session boundary, operator/admin MVC surface, REST/SSE/WebSocket surfaces, secret handling, and resolved Maven dependency set.
- Checked the implementation against the explicit auth/session constraints recorded in the migration artifacts.

## Findings

### CRITICAL - `/config` allows unauthenticated runtime mutation and database operations

- Severity: CRITICAL
- Owner: backend
- Evidence:
  - [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeConfigController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeConfigController.java#L32) exposes `/config` directly.
  - [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeConfigController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeConfigController.java#L44) through [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeConfigController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeConfigController.java#L52) execute `updateConfig`, `resetTrade`, `buildDB`, and `buildDBTables` with no auth guard.
  - [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeAppCompatibilityController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeAppCompatibilityController.java#L55) and [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeAppCompatibilityController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeAppCompatibilityController.java#L57) show the intended session gate exists for `/app`, which `/config` does not reuse.
  - Runtime probe: anonymous `GET http://127.0.0.1:19086/daytrader/config` returned `200`.
- Impact: Any anonymous caller can change runtime settings, reset trader data, or rebuild tables. That is a full integrity compromise of the migrated runtime.
- Required fix: Put `/config` behind the same authenticated session boundary as protected trading flows, then add a role or operator-only authorization check before mutation actions run.

### HIGH - REST, SSE, and WebSocket endpoints remain outside the authenticated boundary

- Severity: HIGH
- Owner: backend
- Architect sign-off: required
- Evidence:
  - [pom.xml](c:\Users\jiangma\Workspace\repos\sample.daytrader8\pom.xml#L25), [pom.xml](c:\Users\jiangma\Workspace\repos\sample.daytrader8\pom.xml#L29), [pom.xml](c:\Users\jiangma\Workspace\repos\sample.daytrader8\pom.xml#L37), and [pom.xml](c:\Users\jiangma\Workspace\repos\sample.daytrader8\pom.xml#L50) show Boot web, JPA, websocket, and validation starters, but no Spring Security dependency is present.
  - [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/QuoteRestController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\QuoteRestController.java#L18), [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/QuoteRestController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\QuoteRestController.java#L27), and [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/QuoteRestController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\QuoteRestController.java#L32) expose `/rest/quotes` with no session or principal check.
  - [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/BroadcastEventsController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\BroadcastEventsController.java#L12), [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/BroadcastEventsController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\BroadcastEventsController.java#L21), and [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/BroadcastEventsController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\BroadcastEventsController.java#L23) expose anonymous SSE registration.
  - [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/MarketSummaryWebSocketConfig.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\MarketSummaryWebSocketConfig.java#L20) allows any origin, and [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/MarketSummaryWebSocketHandler.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\MarketSummaryWebSocketHandler.java#L21) through [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/MarketSummaryWebSocketHandler.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\MarketSummaryWebSocketHandler.java#L22) register sessions immediately.
  - [src/main/java/com/ibm/websphere/samples/daytrader/streaming/StreamingHub.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\streaming\StreamingHub.java#L43) and [src/main/java/com/ibm/websphere/samples/daytrader/streaming/StreamingHub.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\streaming\StreamingHub.java#L65) persist anonymous SSE and WebSocket clients.
  - Runtime probe: anonymous `GET http://127.0.0.1:19086/daytrader/rest/quotes/s:0` returned `200`.
- Impact: Unauthenticated clients can read quote and streaming data, and the WebSocket endpoint is cross-origin reachable. This violates the explicit migration constraint that one auth boundary must own servlet, REST, SSE, and WebSocket traffic.
- Required fix: Add one authoritative Boot security/session boundary for REST and streaming surfaces, preserving `uidBean` session semantics while enforcing authenticated access and a restricted origin policy.

### CRITICAL - Passwords and credit-card data remain plaintext and can leak through logs and object rendering

- Severity: CRITICAL
- Owner: backend
- Evidence:
  - [src/main/java/com/ibm/websphere/samples/daytrader/application/auth/AuthenticationApplicationService.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\application\auth\AuthenticationApplicationService.java#L48) writes raw password updates back to the profile entity, and [src/main/java/com/ibm/websphere/samples/daytrader/application/auth/AuthenticationApplicationService.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\application\auth\AuthenticationApplicationService.java#L82) creates new profiles from raw password and credit-card values.
  - [src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountProfileDataBean.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\entities\AccountProfileDataBean.java#L48) and [src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountProfileDataBean.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\entities\AccountProfileDataBean.java#L64) store plaintext password and credit-card fields.
  - [src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountProfileDataBean.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\entities\AccountProfileDataBean.java#L93) and [src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountProfileDataBean.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\entities\AccountProfileDataBean.java#L97) serialize those secrets in `toString()` and HTML rendering.
  - [src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountDataBean.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\entities\AccountDataBean.java#L247) compares raw passwords directly, and [src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountDataBean.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\entities\AccountDataBean.java#L249) includes the stored password in the failure message.
  - [src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/TradeDirect.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\impl\direct\TradeDirect.java#L1416) logs login attempts with the raw password, and [src/main/java/com/ibm/websphere/samples/daytrader/impl/direct/TradeDirect.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\impl\direct\TradeDirect.java#L1431) builds an error string containing the attempted password.
  - [src/main/java/com/ibm/websphere/samples/daytrader/util/Log.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\util\Log.java#L51) and [src/main/java/com/ibm/websphere/samples/daytrader/util/Log.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\util\Log.java#L64) print stack traces to stdout, increasing exposure of those error strings.
- Impact: The migrated app still stores credentials and payment-like data in plaintext and can emit them to logs or exception streams. That turns ordinary auth failures into credential and PII disclosure paths.
- Required fix: Replace plaintext password handling with one-way hashing and comparison, remove secret-bearing fields from rendering and error messages, and scrub password/credit-card values from all logs and throwable text.

### MEDIUM - Boot-side request validation remains ad hoc instead of framework-enforced

- Severity: MEDIUM
- Owner: backend
- Evidence:
  - [src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountProfileDataBean.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\entities\AccountProfileDataBean.java#L26) through [src/main/java/com/ibm/websphere/samples/daytrader/entities/AccountProfileDataBean.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\entities\AccountProfileDataBean.java#L28) still import `javax.validation` annotations even though the Spring Boot 3 runtime resolves Jakarta/Hibernate Validator 8 in [target/dependency-tree.txt](c:\Users\jiangma\Workspace\repos\sample.daytrader8\target\dependency-tree.txt#L67).
  - [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeAppCompatibilityController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeAppCompatibilityController.java#L112), [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeAppCompatibilityController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeAppCompatibilityController.java#L224), [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeAppCompatibilityController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeAppCompatibilityController.java#L229), and [src/main/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeAppCompatibilityController.java](c:\Users\jiangma\Workspace\repos\sample.daytrader8\src\main\java\com\ibm\websphere\samples\daytrader\web\mvc\TradeAppCompatibilityController.java#L242) parse raw numeric request values directly without framework-level validation or positive/range checks.
- Impact: Invalid or hostile inputs fall through to legacy parsing behavior and downstream exceptions instead of being rejected consistently at the controller boundary.
- Required fix: Move Boot MVC request payloads to validated DTOs using Jakarta validation annotations, then keep only compatibility-specific message shaping in the controller.

## Dependency Posture

- Resolved dependency graph captured in [target/dependency-tree.txt](c:\Users\jiangma\Workspace\repos\sample.daytrader8\target\dependency-tree.txt#L1).
- Notable version risk: [target/dependency-tree.txt](c:\Users\jiangma\Workspace\repos\sample.daytrader8\target\dependency-tree.txt#L81) still resolves Apache Derby `10.14.2.0`, which is below the Hibernate-supported minimum already flagged in runtime startup warnings.
- OWASP Dependency-Check was executed, but the NVD API returned HTTP `429`, so the audit cannot claim a clean CVE verdict from database-backed scanning in this task window.

## Validation Evidence

- Resolved dependency tree command: `cmd /d /c "set JAVA_HOME=%USERPROFILE%\scoop\apps\microsoft17-jdk\current&& set PATH=%JAVA_HOME%\bin;%PATH%&& mvn -q dependency:tree -DoutputFile=target\dependency-tree.txt -DappendOutput=false"`
- Runtime startup command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19086`
- Anonymous probe: `Invoke-WebRequest http://127.0.0.1:19086/daytrader/config` -> `200`
- Anonymous probe: `Invoke-WebRequest http://127.0.0.1:19086/daytrader/rest/quotes/s:0` -> `200`
- CVE scan attempt: `mvn org.owasp:dependency-check-maven:check ...` -> failed because NVD API returned `429`

## Downstream Action

- `t20` should not sign off on auth/session parity until `/config`, REST, SSE, and WebSocket surfaces are brought behind one authenticated boundary.
- Backend remediation should be split into two slices: access-control boundary first, secret-handling cleanup second. Both need revalidation with anonymous probes after the fix.