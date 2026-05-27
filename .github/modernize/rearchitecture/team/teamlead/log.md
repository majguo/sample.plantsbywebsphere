## [t1] Established Spring Boot 3 rewrite constitution
- The project must be treated as a rewrite, not an in-place upgrade, because Liberty and `javax`
  runtime dependencies cut across the entire WAR.
- A conservative platform floor of JDK 17 was chosen because it is the minimum Spring Boot 3
  baseline and avoids forcing a higher-LTS jump before architecture review.
- The constitution locks in full parity, same-schema migration, and auth preservation so later plan
  and gate work can reject scope drift deterministically.
- Learnings consumed: [(none)]

## [t6] Defined migration test strategy and evidence expectations
- DayTrader8 requires a mixed-surface validation stack: MockMvc-backed integration tests, streaming
  contract tests, Playwright browser coverage, and Docker-backed infrastructure for the primary
  execution lane.
- The repo currently has no automated parity suite to migrate; the existing JMeter assets are only
  supplemental performance smoke and cannot stand in for functional evidence.
- Browser E2E remains part of the primary stack because Node.js is present, but implementation must
  add a dedicated Playwright workspace before the tester phase can execute it.
- Async order-mode coverage must be explicit for `Sync`, `Async`, and `Async_2-Phase`; it cannot
  collapse into generic CRUD verification.
- Learnings consumed: [teamlead/spring-boot-3-rewrite-baseline]

## [t4.1] Resolved readiness-evidence policy for downstream planning and validation
- The missing policy gap between target design and test strategy was not about tool choice; it was
  about what downstream tasks are allowed to claim from build, startup, reachability, and journey
  evidence.
- Readiness needs staged states so plan and tester outputs do not over-claim parity based on clean
  build or startup-only proof.
- Surface reachability evidence must be direct for each contract class; an entry-page probe cannot
  stand in for REST, streaming, or alternate-surface parity.
- Learnings consumed: [teamlead/mixed-surface-validation-stack, teamlead/spring-boot-3-rewrite-baseline]

## [t7] Created the implementation plan and task breakdown
- The missing `context.md` file did not block planning because the required execution inputs were already present in the constitution, clarification artifact, `t3`, `t4`, `t5`, `t6`, and `t4.1`.
- No project topology artifact was present, so the plan used capability workstreams rather than G-group labels while still keeping each task independently executable.
- The decomposition rule that mattered most was to keep foundational platform and seam work separate from parity slices, then split the rewrite by user-visible capability rather than entity/service/controller layers.
- Readiness evidence stayed isolated to explicit validation tasks so implementation slices do not over-claim journey readiness from startup-only proof.
- Learnings consumed: [teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, teamlead/spring-boot-3-rewrite-baseline, architect/explicit-runtime-contracts-for-boot-target, architect/spring-boot-war-and-jsf-compat-layer]

## [t8] Ran the implementation-plan quality gate
- Spec-to-plan coverage passed at 30 of 30 requirements; the gate failure came from task-breakdown quality, not missing REQ coverage.
- Constitution Delivery Workflow requirements must be enforced at the inline-task level: phase-level REQ references are not enough when tasks need to be independently executable and auditable.
- Validation-harness work in a brownfield rewrite still needs source anchors so tester-facing tasks stay tied to the existing parity surfaces and approved strategy.
- Gate summaries must be cross-checked against the detailed plan because a stale headline count is easy to miss and weakens deterministic review.
- Learnings consumed: [teamlead/capability-workstreams-for-daytrader-plan, teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, teamlead/spring-boot-3-rewrite-baseline, architect/explicit-runtime-contracts-for-boot-target, architect/spring-boot-war-and-jsf-compat-layer]

## [t8.1] Repaired implementation-plan traceability metadata and source anchors
- The controlling defect was local to the `t7` package: inline tasks lacked constitution-mandated REQ, evidence, and fallback metadata even though plan coverage itself was complete.
- Validation-harness work also needs rewrite-mode anchors to both the legacy parity surfaces and the approved testing-strategy artifacts; otherwise tester-facing execution stays under-specified.
- Summary and checkpoint metadata must be reissued alongside plan repairs so downstream re-gates do not consume stale counts or stale failure state.
- Learnings consumed: [teamlead/capability-workstreams-for-daytrader-plan, teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, teamlead/spring-boot-3-rewrite-baseline, teamlead/task-level-traceability-for-plan-gates, architect/explicit-runtime-contracts-for-boot-target, architect/spring-boot-war-and-jsf-compat-layer]

## [t8] Re-ran the implementation-plan quality gate after the t8.1 repair
- The repaired `t7-teamlead-plan.md` package now satisfies the blocking plan-to-task rules: every plan item is covered, every inline task has `Plan/REQ/Evidence/Fallback`, and the validation-harness tasks now carry rewrite-mode source anchors.
- The remaining defect is narrower than the original gate failure: `t7-teamlead.md` still has one stale summary sentence claiming 14 plan items even though the coverage section, detailed plan, and checkpoint all report 16.
- That package-summary drift is worth recording as a MEDIUM warning because it weakens quick auditability, but it does not break traceability or change the blocking PASS/FAIL decision.
- Learnings consumed: [teamlead/capability-workstreams-for-daytrader-plan, teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, teamlead/spring-boot-3-rewrite-baseline, teamlead/task-level-traceability-for-plan-gates, architect/explicit-runtime-contracts-for-boot-target, architect/spring-boot-war-and-jsf-compat-layer]