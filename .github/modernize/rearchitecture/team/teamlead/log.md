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

## [t22] Conformance review found blocking evidence gaps
- Final completeness review is blocked by evidence integrity, not by a newly discovered functional regression: mixed-surface parity evidence is present, but the sign-off package still fails because required gate artifacts are incomplete.
- `tasks-to-impl.yaml` cannot be trusted as the completeness checkpoint when it omits most implementation tasks and lacks an aggregate validation footer; final review should rebuild traceability explicitly instead of assuming green status from the filename.
- A planned primary validation lane is binding at conformance time. If Docker is available, embedded-Derby evidence cannot substitute for the container-backed database lane without an attempted command and exact blocker output.
- Learnings consumed: [teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, teamlead/task-level-traceability-for-plan-gates, tester/playwright-baseurl-and-preserved-routes, tester/generated-registration-data-must-pass-bean-validation, tester/boot-harness-mocks-scheduled-publishers]

## [t22.1] Regenerated aggregate completeness checkpoints and synchronized conformance inputs
- The controlling defect was local to the artifact package: `tasks-to-impl.yaml` needed to become an authoritative aggregate checkpoint before any conformance rerun could be meaningful.
- The repaired checkpoint should track two truths at once: package integrity can pass while aggregate validation still fails because upstream parity and infra-tier blockers remain open.
- Final traceability for this repo cannot stop at implementation presence; once PM sign-off exists, the requirement matrix must mirror PM/runtime evidence state so the next conformance pass reads the same closure status the product gate sees.
- Learnings consumed: [teamlead/completeness-checkpoints-must-have-aggregate-validation, teamlead/mixed-surface-validation-stack, teamlead/primary-validation-stack-must-be-executed-or-explicitly-blocked, teamlead/task-level-traceability-for-plan-gates]

## [t22.2] Re-ran conformance review after DB2 remediation
- `t20.5` closes the DB2 primary infra-lane blocker from `t22`; the planned mixed-surface primary stack is now executed on real DB2 infrastructure.
- Missing `t21.1` is itself a gating defect because conformance cannot upgrade a stale PM FAIL artifact just because the underlying runtime evidence improved.
- The remaining blocking state is now parity-signoff driven: alternate/documentation/primitive and admin/scenario inventories still lack exhaustive PM-accepted evidence.
- Learnings consumed: [teamlead/completeness-checkpoints-must-have-aggregate-validation, teamlead/mixed-surface-validation-stack, teamlead/primary-validation-stack-must-be-executed-or-explicitly-blocked, teamlead/readiness-evidence-policy, teamlead/task-level-traceability-for-plan-gates]

## [t22.3] Re-ran conformance review after the seeded parity closure evidence
- The controlling blocker moved again: `t21.1` is no longer missing, but the dispatched dependency for this rerun is `t21.3`, and conformance still fails when that exact upstream PM closure artifact is absent.
- New tester evidence can legitimately tighten the traceability package even before PM reruns. `t21.2.2` was enough to promote `REQ-019`, `REQ-020`, `REQ-021`, and `REQ-025` to complete because those operator/scenario/JAX-RS contracts were directly re-proven on DB2.
- That evidence improvement still cannot substitute for PM parity closure on the remaining directly reachable documentation and alternate-surface inventory; teamlead conformance must not infer product sign-off from tester evidence alone.
- Learnings consumed: [teamlead/completeness-checkpoints-must-have-aggregate-validation, teamlead/mixed-surface-validation-stack, teamlead/primary-validation-stack-must-be-executed-or-explicitly-blocked, pm/signoff-needs-per-surface-proof]

## [t22.4] Re-ran conformance review after the final surface-proof rerun
- The controlling check was again the named dependency artifact itself: once the board dispatched `t21.5`, conformance had to key on that exact PM rerun artifact rather than treating `t21.3` as implicitly refreshed.
- New tester evidence can still tighten aggregate traceability before PM reruns. `t21.4.2` was enough to promote `REQ-001`, `REQ-026`, `REQ-027`, and `REQ-028` to complete because those docs, primitive-launcher, alternate-surface, and image-mode contracts were directly re-proven on the DB2 lane.
- That evidence improvement still cannot substitute for PM parity closure on the remaining user-visible behavior and anonymous-access gaps; without `t21.5-pm.md`, the latest PM verdict stays stale and the gate remains FAIL.
- Learnings consumed: [teamlead/completeness-checkpoints-must-have-aggregate-validation, teamlead/mixed-surface-validation-stack, teamlead/primary-validation-stack-must-be-executed-or-explicitly-blocked, pm/signoff-needs-per-surface-proof]

## [t22.5] Final conformance rerun after PM artifact publication race fix
- The controlling defect was exactly the artifact freshness issue carried by `t22.4`: once `t21.5-pm.md` existed, the conformance bundle could flip without any new product/runtime evidence.
- For this repo, the final rerun is not just a new top-level report. `migration-summary.md`, `checkpoints/tasks-to-impl.yaml`, and `checkpoints/traceability-matrix.yaml` all need to be refreshed together so downstream consumers do not see a PASS report paired with stale FAIL checkpoints.
- The final conformance package now closes at `30/30` requirements complete with only the previously accepted MEDIUM dependency-assurance note from `t18.1` remaining.
- Learnings consumed: [teamlead/completeness-checkpoints-must-have-aggregate-validation, teamlead/mixed-surface-validation-stack, teamlead/primary-validation-stack-must-be-executed-or-explicitly-blocked, teamlead/readiness-evidence-policy, pm/addressable-surfaces-count-for-parity, pm/signoff-needs-per-surface-proof]