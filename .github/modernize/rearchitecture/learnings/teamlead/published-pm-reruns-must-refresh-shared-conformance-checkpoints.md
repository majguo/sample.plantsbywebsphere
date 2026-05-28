# Published PM Reruns Must Refresh Shared Conformance Checkpoints

If a delayed PM parity artifact unblocks conformance, refresh the shared summary and checkpoint package together instead of treating the rerun as a report-only change.

## What Happened
In `sample.daytrader8/t22.4`, conformance stayed red only because `t21.5-pm.md` had not been published yet. In `t22.5`, once that PM artifact existed and approved the final evidence bundle, the gate could flip to PASS without any new runtime evidence, but only after `migration-summary.md`, `checkpoints/tasks-to-impl.yaml`, and `checkpoints/traceability-matrix.yaml` were all refreshed to the same state.

## Takeaway
For DayTrader conformance reruns triggered by artifact-publication races, do not update only the top-level teamlead report. Refresh the shared summary and both aggregate checkpoints in the same task so every downstream consumer reads the same verdict, blocker list, and requirement totals.

## History
- 2026-05-28 (sample.daytrader8/t22.5): initial