# Signoff Needs Per Surface Proof

PM sign-off for parity work requires evidence for each directly reachable surface family, not just representative probes.

## What Happened
In sample.daytrader8 task t21, the migration had strong representative proof for core flows and selected alternate surfaces, but the parity inventory from t3 treated every directly reachable primitive, docs page, and alternate page as in scope. That left sign-off blocked even though the implementation looked broadly healthy.

## Takeaway
For PM parity sign-off, treat representative reachability as insufficient whenever the approved inventory names a full directly reachable surface set. Either produce runtime evidence for each required surface family or document an approved de-scope before sign-off.

## History
- 2026-05-28 (sample.daytrader8/t21): initial