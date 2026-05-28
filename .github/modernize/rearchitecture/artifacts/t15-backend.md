# t15 - JSF/XHTML compatibility, primitives, and alternate surfaces

## Summary

Closed the remaining Spring Boot 3 compatibility gap on the JSF/XHTML and primitive surface by fixing the JSF bridge bean contract needed for `config.xhtml` postback, validating the existing `web.jsfcompat` bridge layer under JDK 17, and proving representative JSF and primitive endpoints on the packaged Boot WAR.

## Deliverables

- Fixed the writable JSF bean property contract for `tradeconfig.maxQuotes` in `src/main/java/com/ibm/websphere/samples/daytrader/web/jsfcompat/TradeConfigJsfBridge.java` so `config.xhtml` can bind and submit the field correctly.
- Added focused regression coverage in `src/test/java/com/ibm/websphere/samples/daytrader/web/jsfcompat/TradeConfigJsfBridgeTest.java` to lock the JSF bean property name in place.
- Revalidated the existing primitive compatibility controller with `PrimitiveCompatibilityControllerTest` under the Spring Boot 3 runtime assumptions used by earlier backend tasks.
- Repackaged and started the executable Boot WAR, then verified representative alternate surfaces at `/welcome.faces`, `/PingJsf.faces`, `/PingCDIJSF.faces`, and `/servlet/PingServlet`.

## Decisions

- Kept the legacy `web.jsf` package excluded from Spring scanning and relied on the existing `web.jsfcompat` bridge package as the Spring-managed compatibility seam, consistent with the t9 shell boundary.
- Treated JSF bean property names as part of the compatibility contract. For the migrated JSF bridge layer, JavaBean getter/setter naming must exactly match the XHTML-bound property names or the Boot-hosted facelets silently lose postback behavior.
- Kept validation focused on the task-owned alternate surfaces rather than reviving legacy servlet/CDI action classes or widening the migration seam beyond the existing bridge/controller layer.

## Issues Found

- WAR packaging initially failed because a stale `java -jar target\io.openliberty.sample.daytrader8.war` process was still holding the target artifact open on Windows. Stopping that stale process cleared the lock; no code change was required.
- Derby `10.14` still logs Hibernate's existing minimum-version warning during startup, but the packaged Boot app starts and serves the validated compatibility endpoints.

## Test Results

- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; & mvn.cmd "-Dtest=PrimitiveCompatibilityControllerTest,TradeConfigJsfBridgeTest" test`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; & mvn.cmd -DskipTests package`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19084`
- Command: `Invoke-WebRequest` probes to `http://localhost:19084/daytrader/welcome.faces`, `http://localhost:19084/daytrader/PingJsf.faces`, `http://localhost:19084/daytrader/PingCDIJSF.faces`, and `http://localhost:19084/daytrader/servlet/PingServlet`
- Passed: 10
- Failed: 0
- Skipped: 0
- Details: `PrimitiveCompatibilityControllerTest` and `TradeConfigJsfBridgeTest` passed 4 tests total; WAR packaging succeeded; the Boot app started on port `19084` with context path `/daytrader`; all 4 representative JSF and primitive HTTP probes returned `200`.