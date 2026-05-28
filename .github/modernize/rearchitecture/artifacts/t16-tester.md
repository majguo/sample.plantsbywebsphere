# t16 - Deterministic test harness, validation infrastructure, and journey suites

## Summary

Built the tester-owned deterministic validation harness required by the DayTrader Spring Boot 3 rewrite and closed the missing browser lane bootstrap. The task now proves MVC/session/admin/REST flows in-process, validates live-port SSE and raw WebSocket contracts under JDK 17, adds a dedicated Playwright workspace for preserved `/daytrader` browser routes, and publishes a staged readiness summary from surefire plus Playwright outputs.

## Deliverables

- Added Boot-context journey coverage in `src/test/java/com/ibm/websphere/samples/daytrader/integration/journeys/DayTraderJourneyIntegrationTest.java`.
- Added live-port streaming coverage in `src/test/java/com/ibm/websphere/samples/daytrader/integration/streaming/DayTraderStreamingIntegrationTest.java`.
- Added the dedicated browser-parity workspace in `e2e/package.json`, `e2e/playwright.config.ts`, and `e2e/tests/daytrader-smoke.spec.ts`.
- Added `scripts/ci/publish-readiness-evidence.ps1` to summarize surefire and Playwright outputs into one readiness artifact.
- Established deterministic tester harness rules across the suites:
  - explicit `DayTraderApplication` bootstrap binding
  - JDK 17 Maven execution
  - scheduled publisher/worker beans mocked so tests control runtime timing
  - runtime settings reset before each MVC journey test
  - browser tests pinned to the preserved `/daytrader` base path with relative route navigation
  - real local Derby startup under the repo's existing configuration fallback path when Docker is unavailable

## Coverage Reached

- MVC/session journey: login -> home -> logout with compatibility session markers
- Registration validation failure surface
- Operator config update path with `RuntimeSettingsService` and `TradeConfig` mirror assertions
- Quote lookup + buy flow via `/app`
- REST quote contract via `GET /rest/quotes/{symbols}` and `POST /rest/quotes`
- SSE handshake reachability via `/rest/broadcastevents`
- Raw WebSocket reachability and request/response contract via `/marketsummary`
- Browser smoke coverage for preserved JSP login/home/logout, config, and primitive servlet reachability
- Readiness evidence publication in `target/readiness-evidence.md`

## Findings

- WARNING: Docker is unavailable on this workstation (`docker info` failed), so the primary infra-tier lane remains downgraded to the repo's local Derby fallback for now.
- WARNING: The live SSE disconnect path now stays non-fatal for validation, but Tomcat still logs client-abort `IOException` noise when the test subscriber closes the socket intentionally. The streaming suite passes and the disconnect no longer breaks later publication.
- WARNING: Browser coverage in this task is representative smoke coverage, not the full parity run reserved for `t20`.

integration: PASS — Boot-context MVC/session/admin/REST harness and live-port streaming suite pass under JDK 17 with explicit Docker fallback disclosure
e2e: PARTIAL — dedicated Playwright workspace is bootstrapped and representative browser smoke passes against the preserved `/daytrader` routes; full parity execution remains for `t20`
overall: PASS — deterministic validation infrastructure and journey harnesses are in place with explicit infra-tier fallback and staged-readiness evidence

## Test Results

- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; & mvn.cmd "-Dtest=DayTraderJourneyIntegrationTest,DayTraderStreamingIntegrationTest" test`
- Command: `Set-Location e2e; npm install`
- Command: `Set-Location e2e; npx playwright install chromium`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; & mvn.cmd -DskipTests package`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19085`
- Command: `$env:DAYTRADER_BASE_URL = "http://127.0.0.1:19085/daytrader/"; npm --prefix e2e test`
- Command: `& .\scripts\ci\publish-readiness-evidence.ps1 -OutputPath target\readiness-evidence.md`
- Passed: 10
- Failed: 0
- Skipped: 0
- Errors: 0
- Details:
  - `DayTraderJourneyIntegrationTest`: passed 5 tests
  - `DayTraderStreamingIntegrationTest`: passed 3 tests
  - `daytrader-smoke.spec.ts`: passed 2 Playwright tests
  - `target/readiness-evidence.md` generated successfully and reports surefire plus Playwright outputs
- Environment evidence:
  - Docker: unavailable (`daemon not responding`)
  - Node.js: available (`v22.17.1`)
  - Maven: available (`3.9.11`)

## Readiness State

- build-ready: PASS — JDK 17 package build succeeded
- startup-ready: PASS — packaged Boot WAR started on port `19085` with context path `/daytrader`
- surface-ready: PASS — MVC, REST, SSE, WebSocket, config, JSF/primitive reachability, and representative browser routes are all exercised by executable suites
- journey-ready: PARTIAL — representative browser journeys are in place and pass, but the full parity run remains downstream work for `t20`