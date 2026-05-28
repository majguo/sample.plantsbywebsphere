# Playwright base URL and preserved routes

For DayTrader browser validation, anchor Playwright to the preserved `/daytrader` context path and use relative route navigation so tests do not escape to host-root URLs.

## What Happened
In sample.daytrader8 task t16, the missing browser lane was added as a dedicated `e2e/` workspace. The first Playwright run failed with `HTTP Status 404 - Not Found` even though the Boot app was up, because `page.goto('/')` and `page.goto('/config')` ignored the `/daytrader` path segment in the configured base URL and resolved against the host root. Switching the suite to a `/daytrader/` base URL plus relative routes (`welcome.jsp`, `config`, `servlet/PingServlet`) made the browser smoke deterministic without changing application routing.

## Takeaway
For DayTrader browser suites, keep `DAYTRADER_BASE_URL` pointed at the preserved context path and avoid leading-slash navigation in tests. Relative routes preserve the migrated app's compatibility surface and prevent false 404 failures caused by test harness pathing.

## History
- 2026-05-28 (sample.daytrader8/t16): initial