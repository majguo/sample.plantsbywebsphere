# Completeness Checkpoints Must Have Aggregate Validation

Implementation checkpoints are not authoritative for final sign-off unless they cover the full task set and end with an aggregate validation block.

## What Happened
In `sample.daytrader8/t22`, `artifacts/checkpoints/tasks-to-impl.yaml` existed but contained only three task entries and no aggregate validation footer. The conformance review had to reconstruct requirement traceability manually before a verdict could be issued.

## Takeaway
When a worker produces a checkpoint that another gate depends on, the checkpoint must be complete enough to stand on its own: all expected task entries, an explicit validation summary, and no silent truncation. If that footer is missing, treat the checkpoint as broken rather than assuming green status from its filename alone.

For final conformance in this repo, the regenerated aggregate checkpoint and the requirement traceability input also need to stay synchronized with PM/runtime evidence status. Restoring checkpoint structure is necessary but not sufficient; the next gate must see the same open blockers that PM sign-off and testing-strategy review still report.

If a remediation task changes the underlying evidence bundle, the dependent sign-off artifact must be rerun as its own deliverable. Do not infer a fresh PM approval from tester/runtime evidence alone; a missing rerun artifact is still a FAIL at conformance time.

If a newer tester artifact closes a subset of previously open requirements, conformance may update the aggregate traceability package to reflect that narrower evidence delta. That does not relax the upstream dependency rule: the named PM rerun artifact is still required before the gate can flip to PASS.

## History
- 2026-05-28 (sample.daytrader8/t22): initial
- 2026-05-28 (sample.daytrader8/t22.1): extended to require synchronized PM/runtime blocker state when reissuing final conformance inputs
- 2026-05-28 (sample.daytrader8/t22.2): extended to require fresh upstream rerun artifacts after blocker-remediation evidence changes
- 2026-05-28 (sample.daytrader8/t22.3): clarified that newer tester evidence can narrow requirement status while the missing upstream PM closure artifact still keeps the conformance gate red