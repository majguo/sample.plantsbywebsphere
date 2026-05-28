## [t16] Boot-context tester harness for MVC and streaming surfaces
- Built tester-owned Boot integration suites for MVC/session/admin/REST journeys and live-port SSE/WebSocket checks.
- Needed explicit `DayTraderApplication` binding because the tests live outside the application package tree.
- Needed scheduled publisher/worker beans mocked at context creation; `@BeforeEach` stubbing was too late because scheduled methods can fire during startup.
- The streaming suite exposed a backend bug in `StreamingHub.onQuotePriceChange`: after an SSE client disconnects, `completeWithError(...)` can raise `IllegalStateException` on a dead async context.
- Learnings consumed: [teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, backend/mvc-app-session-compatibility, backend/boot-streaming-and-order-work, backend/boot-mvc-operator-surfaces]

## [t16] Browser harness closure and readiness evidence
- Added the missing dedicated Playwright workspace and kept it anchored to the preserved `/daytrader` context path via config plus relative route navigation.
- First browser run failed because leading-slash routes escaped the base path; switching the spec to `welcome.jsp`, `config`, and `servlet/PingServlet` fixed the harness without changing application code.
- Generated a tester-owned readiness summary from surefire plus Playwright outputs in `target/readiness-evidence.md`.
- Docker remained unavailable, so the harness stays on the documented local-Derby fallback until downstream runtime validation.
- Learnings consumed: [tester/boot-harness-mocks-scheduled-publishers, teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, backend/mvc-app-session-compatibility, backend/boot-streaming-and-order-work, backend/boot-mvc-operator-surfaces]

## [t20] Mixed-surface runtime validation on packaged Boot WAR
- Re-ran the focused JDK 17 Boot journey and streaming suites and expanded the Playwright parity lane to cover registration, buy/sell, account validation, and config update/restore on the preserved `/daytrader` routes.
- First browser rerun failed because `DAYTRADER_BASE_URL` without a trailing slash caused relative Playwright navigation to escape the app context; normalizing the configured base URL fixed the harness.
- Registration parity initially failed because the generated email mirrored a `uid:*` identifier and violated the migrated bean-validation email constraint; sanitizing the generated email restored the browser journey without touching production code.
- A follow-up JVM rerun failed once because the packaged WAR still held the embedded Derby lock; killing the runtime and rerunning the suites produced the final green evidence.
- Learnings consumed: [tester/boot-harness-mocks-scheduled-publishers, tester/playwright-baseurl-and-preserved-routes]
