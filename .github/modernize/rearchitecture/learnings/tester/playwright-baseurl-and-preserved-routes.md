# Playwright base URL and preserved routes

For DayTrader browser validation, anchor Playwright to the preserved `/daytrader` context path and use relative route navigation so tests do not escape to host-root URLs.

## What Happened
In sample.daytrader8 task t16, the missing browser lane was added as a dedicated `e2e/` workspace. The first Playwright run failed with `HTTP Status 404 - Not Found` even though the Boot app was up, because `page.goto('/')` and `page.goto('/config')` ignored the `/daytrader` path segment in the configured base URL and resolved against the host root. Switching the suite to a `/daytrader/` base URL plus relative routes (`welcome.jsp`, `config`, `servlet/PingServlet`) made the browser smoke deterministic without changing application routing.

In sample.daytrader8 task t20, the same path class reappeared when the runtime environment variable omitted the trailing slash (`http://127.0.0.1:19089/daytrader`). Relative navigation such as `page.goto('welcome.jsp')` then resolved to the host root instead of `/daytrader/welcome.jsp`. Normalizing the configured base URL inside `playwright.config.ts` fixed the issue for both the default path and environment overrides.

## Takeaway
For DayTrader browser suites, keep `DAYTRADER_BASE_URL` pointed at the preserved context path, normalize it to end with `/`, and avoid leading-slash navigation in tests. Relative routes preserve the migrated app's compatibility surface and prevent false 404 failures caused by test harness pathing.

## History
- 2026-05-28 (sample.daytrader8/t16): initial
- 2026-05-28 (sample.daytrader8/t20): added trailing-slash normalization for environment overrides
