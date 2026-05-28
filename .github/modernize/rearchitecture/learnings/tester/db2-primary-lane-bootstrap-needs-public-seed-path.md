# DB2 primary lane bootstrap needs public seed path

On DayTrader's DB2 primary lane, a fresh Boot startup can be schema-complete but still unusable for parity if the benchmark seed data cannot be created through the preserved bootstrap surface.

## What Happened
In sample.daytrader8 task t20.4, the repo-local DB2 container lane and packaged Spring Boot WAR both started successfully, but the fresh DB2 database contained neither the canonical `uid:0` operator account nor the seeded `s:*` quotes required by the preserved browser journeys. The public utility page `configure.html` still rendered and linked to `config?action=buildDB`, but that linked action returned `401 Unauthorized` on the unseeded runtime. That left the system in a circular bootstrap state: the operator account needed for config/admin flows did not exist, and the public utility that should create it was no longer executable anonymously.

## Takeaway
For real-infra DayTrader validation, do not stop at `Started DayTraderApplication`. Also verify that a fresh lane can expose or create the canonical seed dataset through the preserved bootstrap path. If `uid:0 / xxx` fails and `/rest/quotes/s:1` resolves to `[null]`, treat the lane as unseeded even though Flyway and startup succeeded.

## History
- 2026-05-28 (sample.daytrader8/t20.4): initial