# Task-Level Traceability For Plan Gates

For DayTrader8 planning gates, phase-level references are not enough when the constitution requires each task to be independently executable and auditable.

## What Happened
During `sample.daytrader8/t8`, the plan covered all 30 requirements and every plan item had a task, but the quality gate still failed because the inline tasks did not carry their own requirement traceability, evidence expectations, or fallback validation path. The validation-harness tasks also lacked rewrite-mode source anchors, which made the tester-facing work less deterministic than the rest of the plan.

During `sample.daytrader8/t8.1`, reissuing the package showed that the repair has to update the adjacent summary and checkpoint metadata in the same pass. Otherwise the detailed plan is fixed but the package still presents stale counts or stale failure state to downstream reviewers.

## Takeaway
For future implementation-plan artifacts in this repo, every inline task should explicitly name its REQ coverage, required evidence, fallback path when the preferred validation lane is unavailable, and any rewrite-mode source anchors needed for source-anchored migration work. Do not rely on phase summaries to satisfy task-level audit requirements.

When reissuing a repaired plan package, update the summary artifact and traceability checkpoint at the same time so every file in the package reflects the same plan-item counts and task-annotation state.

## History
- 2026-05-27 (sample.daytrader8/t8): initial
- 2026-05-27 (sample.daytrader8/t8.1): extended to require summary and checkpoint synchronization when reissuing repaired plan packages