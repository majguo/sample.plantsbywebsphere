# t20 - Runtime validation: integration, streaming, and browser parity testing

## Summary

Executed the tester-owned runtime validation for the Spring Boot 3 DayTrader rewrite across the focused Boot integration lane, live-port SSE/WebSocket lane, packaged-WAR startup lane, and Playwright browser lane. The current state proves mixed-surface parity on the preserved `/daytrader` runtime using JDK 17, with explicit evidence for login/logout, registration validation and success, buy/sell flows, account validation, operator config updates, alternate surface reachability, REST quote access, SSE reachability, and WebSocket market-summary updates.

## Environment and Execution Notes

- JDK: Microsoft OpenJDK 17 (`%USERPROFILE%\scoop\apps\microsoft17-jdk\current`)
- Node.js: `v22.17.1`
- Maven: `3.9.11`
- Docker: available in this session, but the validated application path still used the repo's embedded Derby runtime and did not expose a separate container-backed database harness.
- Startup evidence used the packaged Boot WAR on port `19089` with context path `/daytrader`.
- During validation, one intermediate JVM rerun failed only because the packaged WAR still held the embedded Derby lock; stopping the runtime and rerunning the JVM suites cleared the issue.

## Coverage Reached

- J1 session entry and protection: browser login/home/logout flow passed on the preserved JSP surface.
- J2 registration: browser validation covered mismatch handling plus successful registration with authenticated landing.
- J3 quote lookup and buy lifecycle: browser validation covered Quotes/Trade and order submission; Boot integration validation still covers all order modes `0/1/2`.
- J4 portfolio, sell, and account maintenance: browser validation covered portfolio sell plus account-profile password mismatch handling.
- J5 operator configuration: browser validation covered mutable config update and restore on `/config`.
- J6 streaming and API contracts: Boot integration validation covered REST quote JSON, SSE welcome frame, WebSocket request/response payloads, and market-summary push publication.
- J7 alternate surfaces: Boot integration plus browser validation covered primitive servlet reachability and representative alternate surfaces.

## Findings

- WARNING: The runtime validation is still anchored to the embedded Derby developer path. Even with Docker available in this session, there is no tester-owned container-backed database lane in the repo to prove the higher-fidelity infrastructure path from `t6`.
- WARNING: Hibernate continues to emit `HHH000511` because the active runtime still uses Derby `10.14.2.0`, below Hibernate's supported minimum `10.15.2`.
- INFO: The SSE/WebSocket validation still produces expected client-abort `IOException` noise when the test subscriber closes the socket intentionally, but the streaming suite passes and later publication is unaffected.

integration: PASS — Boot integration journeys and live-port streaming suites passed under JDK 17; MVC, REST, SSE, WebSocket, order-mode coverage, and alternate surfaces are exercised with explicit evidence.
e2e: PASS — Playwright now proves preserved browser parity for login/logout, registration, buy, portfolio sell, account-profile validation, config update/restore, and primitive reachability on the packaged Boot WAR.
overall: NEEDS_SIGNOFF — mixed-surface runtime parity is proven on the embedded-Derby path, but the container-backed infra lane remains unverified and the Derby support warning remains open.

## Test Results

- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; & mvn.cmd "-Dtest=DayTraderJourneyIntegrationTest,DayTraderStreamingIntegrationTest" test`
- Command: `cmd /d /c "set JAVA_HOME=%USERPROFILE%\scoop\apps\microsoft17-jdk\current&& set PATH=%JAVA_HOME%\bin;%PATH%&& mvn -Dmaven.test.skip=true package"`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19089`
- Command: `$env:DAYTRADER_BASE_URL = "http://127.0.0.1:19089/daytrader"; npm --prefix e2e test -- --workers=1`
- Passed: 11
- Failed: 0
- Skipped: 0
- Details:
  - `DayTraderJourneyIntegrationTest`: 5 passed, 0 failed, 0 skipped
  - `DayTraderStreamingIntegrationTest`: 3 passed, 0 failed, 0 skipped
  - `daytrader-smoke.spec.ts`: 3 passed, 0 failed, 0 skipped
  - Packaged WAR startup: PASS on `19089` with `/daytrader` context path

## Evidence Sources

- Surefire XML:
  - `target/surefire-reports/TEST-com.ibm.websphere.samples.daytrader.integration.journeys.DayTraderJourneyIntegrationTest.xml`
  - `target/surefire-reports/TEST-com.ibm.websphere.samples.daytrader.integration.streaming.DayTraderStreamingIntegrationTest.xml`
- Playwright artifacts:
  - `e2e/playwright-report/index.html`
  - `e2e/test-results/.last-run.json`
