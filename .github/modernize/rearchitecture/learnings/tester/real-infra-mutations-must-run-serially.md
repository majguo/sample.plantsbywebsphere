# Real-infra mutations must run serially

When DayTrader parity probes mutate live DB2 runtime state, run those checks serially so one probe does not invalidate another and create false regressions.

## What Happened
In sample.daytrader8 task t21.2.2, the first seeded DB2 rerun mixed `resetTrade` with scenario-driver checks in parallel. That briefly made the scenario results look regressed even though the repaired runtime was green once the scenario suite was rerun serially on the seeded lane.

## Takeaway
On the primary DB2 lane, keep mutating probes such as `buildDB`, `resetTrade`, and scenario-driver validation in a deliberate order. Use parallelism only for read-only checks; otherwise a valid repair can appear broken because another probe changed runtime state mid-run.

## History
- 2026-05-28 (sample.daytrader8/t21.2.2): initial