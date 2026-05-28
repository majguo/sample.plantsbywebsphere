# Primary Validation Stack Must Be Executed Or Explicitly Blocked

Final conformance for DayTrader cannot pass on fallback evidence when the approved primary test lane was available but never attempted.

## What Happened
In `sample.daytrader8/t22`, the runtime-validation package proved mixed-surface parity on the embedded Derby path, but `t6` had already made a container-backed infra lane part of the primary stack when Docker is available. `t20` said Docker was available and still did not publish a failed container-lane command or exact blocker output.

## Takeaway
For teamlead completeness gates, a planned primary lane is binding. If a fallback lane is used instead, the artifact must capture the exact attempted command, the exact error output, and why the blocker cannot be resolved in-session. Otherwise the verdict is FAIL even when the fallback tests are green.

## History
- 2026-05-28 (sample.daytrader8/t22): initial