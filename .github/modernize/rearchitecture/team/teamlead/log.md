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