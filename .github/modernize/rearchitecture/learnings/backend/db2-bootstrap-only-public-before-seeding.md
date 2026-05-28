# DB2 bootstrap only public before seeding

For DayTrader Boot `/config`, preserve the legacy anonymous `buildDB` utility only until the canonical seed data exists; keep all other config/admin traffic operator-gated.

## What Happened
In sample.daytrader8 task t20.4.1, the shared `/config` interceptor introduced after the auth review blocked the preserved DB2 bootstrap flow on a fresh lane. That created a circular dependency: the app required operator login to run `buildDB`, but the canonical operator account `uid:0` and seeded `s:*` quotes are created by `buildDB` itself.

## Takeaway
Treat `config?action=buildDB` as a one-time bootstrap exception, not as a general relaxation of the `/config` boundary. Use a narrow interceptor bypass keyed to the `buildDB` action and the absence of canonical seed markers such as `uid:0` and `s:1`; once the seed set exists, anonymous access should return to `401`.

## History
- 2026-05-28 (sample.daytrader8/t20.4.1): initial