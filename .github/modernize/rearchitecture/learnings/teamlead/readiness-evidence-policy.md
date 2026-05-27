# Readiness Evidence Policy

Treat startup and readiness proof as staged evidence, not as shorthand for parity completion.

## What Happened
For sample.daytrader8 task t4.1, the target architecture and testing strategy already fixed the
runtime contracts and validation stack, but they did not yet define what downstream tasks were
allowed to claim from build, startup, reachability, and functional evidence. A separate policy was
needed so planning and validation could reject over-claims deterministically.

## Takeaway
Use explicit readiness states such as build-ready, startup-ready, surface-ready, journey-ready, and
release-ready. Require exact evidence for each state, forbid one surface from standing in for
another, and never let startup proof count as feature parity proof.

## History
- 2026-05-27 (sample.daytrader8/t4.1): initial