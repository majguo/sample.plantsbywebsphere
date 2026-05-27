# t6 - Migration Test Strategy and Evidence Expectations

## Summary

DayTrader8 must be validated as a mixed-surface Spring Boot 3 rewrite: server-rendered HTML,
session-driven trading flows, REST, SSE, WebSocket, operator utilities, scenario endpoints, and
benchmark primitives all remain in scope. The strategy below fixes the validation stack,
environment prerequisites, fallback rules, and evidence requirements before implementation begins.

This strategy is source-anchored to `t2.1` runtime constraints and `t3` parity requirements.

## Verified Inputs

- Dependencies consumed: `t2.1-architect`, `t3-pm`, `clarification.md`
- Capability checks executed on 2026-05-27:
  - Docker: available
  - Node.js: available (`v22.17.1`)
  - Maven: available (`3.9.11`)
- Playwright probe: `npx --yes playwright install chromium` emitted a bootstrap warning because the
  repo does not yet contain Node-based test dependencies. Treat browser E2E as part of the primary
  target stack, but require implementation to add an explicit Playwright workspace before execution.
- Legacy automated parity tests discovered: none under `src/test` and no Selenium/Cypress/
  Playwright suites found in the repo.
- Supplemental non-parity assets discovered: `jmeter_files/*.jmx` and `README_LOAD_TEST.md`.

## Application Characterization

- `appType`: mixed server-rendered web app with streaming and operator utilities
- Runtime contracts that drive testing:
  - Session-auth boundary and protected-route fallback
  - Order modes `Sync`, `Async`, `Async_2-Phase`
  - Async order completion and rollback-sensitive processing
  - REST quote API plus SSE and WebSocket fan-out
  - Operator configuration toggles that change visible behavior
  - Same-schema persistence compatibility

## Legacy Test Assets

- `legacyTestAssets`: none discovered for automated parity coverage
- `source`: discovered
- `migrationDecision`: discard
- `migrationRationale`: there is no existing automated browser or integration suite to migrate;
  the shipped JMeter plans are useful only as supplemental performance smoke after functional
  parity is established
- `legacyTestMappingTable`: N/A

## Critical Journeys

1. `J1 - Session entry and protection`
   - Flow: welcome page -> valid login -> authenticated home -> protected request without session ->
     welcome/login fallback -> logout invalidates session
   - Coverage: `REQ-002`, `REQ-003`, `REQ-004`, `REQ-006`, `REQ-015`, `REQ-029`, `REQ-030`
2. `J2 - Registration`
   - Flow: registration page -> successful account creation -> immediate signed-in session; mismatch
     passwords remain on registration with visible error
   - Coverage: `REQ-005`, `REQ-029`, `REQ-030`
3. `J3 - Quote lookup and buy lifecycle`
   - Flow: quote lookup -> deep-link symbol view -> buy submission -> order confirmation ->
     completed-order alert visibility across mode variants
   - Coverage: `REQ-007`, `REQ-008`, `REQ-009`, `REQ-010`, `REQ-017`, `REQ-029`
   - Mode requirement: execute against `Sync`, `Async`, and `Async_2-Phase`
4. `J4 - Portfolio, sell, and account maintenance`
   - Flow: portfolio render (including empty state) -> sell -> account page -> show-all-orders ->
     profile update success and validation failures
   - Coverage: `REQ-011`, `REQ-012`, `REQ-013`, `REQ-014`, `REQ-029`
5. `J5 - Operator configuration and environment reset`
   - Flow: configuration page render -> update runtime config -> reset trade runtime -> rebuild
     schema/indexes -> repopulate DB -> explicit success/failure messaging
   - Coverage: `REQ-018`, `REQ-019`, `REQ-020`, `REQ-029`
6. `J6 - Streaming and API contracts`
   - Flow: REST quote GET/POST -> SSE initial payload and update stream -> market-summary WebSocket
     connect/update/disconnect behavior
   - Coverage: `REQ-022`, `REQ-023`, `REQ-024`, `REQ-029`
7. `J7 - Scenario, primitive, and alternate-surface reachability`
   - Flow: scenario driver representative actions -> primitive launcher -> representative servlet,
     async, JSON, WebSocket, and EJB/JMS replacement probes -> alternate JSP/JSF/XHTML and docs
     remain reachable
   - Coverage: `REQ-001`, `REQ-021`, `REQ-025`, `REQ-026`, `REQ-027`, `REQ-028`, `REQ-029`

## Primary Validation Stack

### Build and startup lane

- Clean-build reproducibility: `mvn -B -DskipTests clean package`
- Application startup: target Spring Boot app launched with the migration's authoritative `test`
  profile while preserving the external context path `/daytrader`
- Readiness evidence: HTTP probe to the preserved entry surface plus any health endpoint added by
  the migration

### Integration lane

- Primary tool: `@SpringBootTest` + `@AutoConfigureMockMvc`
- Scope:
  - HTML controller/view-model behavior
  - session lifecycle and protected-route enforcement
  - REST quote contract
  - config and scenario actions
  - error/result page semantics
- Rule: use MockMvc for HTML and mixed HTML/API flows; do not rely on `TestRestTemplate` for page
  flows because view resolution and model assertions are required for parity

### Streaming lane

- SSE: Java HTTP client or equivalent stream consumer under `@SpringBootTest(webEnvironment =
  RANDOM_PORT)`
- WebSocket: standard WebSocket client against the running app for market-summary updates
- Scope:
  - initial payload contract
  - ongoing update cadence
  - disconnect/reconnect behavior
  - operator-controlled toggle effects

### Browser lane

- Primary tool: Playwright running against a started Spring Boot instance
- Scope:
  - primary user flows `J1` through `J5`
  - alternate surfaces that must remain directly reachable
  - browser-visible rendering, forms, redirects, cookies, and live-update DOM changes
- Execution prerequisite:
  - add a Node-based E2E workspace with `@playwright/test`
  - install Chromium before validation begins

### Infrastructure lane

- Primary tool: Docker-based real infrastructure
- Minimum expectation:
  - relational database container aligned to the canonical persistence target defined in `t5`
  - any durable async transport used for order completion must run as real infrastructure in this
    lane, not a mock
- Local smoke allowance:
  - if the target keeps an embedded Derby-compatible developer profile, it may be used for fast
    smoke tests, but it does not replace the authoritative container-backed integration lane

### Supplemental non-functional lane

- Reuse the existing JMeter plans only after functional parity passes
- Purpose:
  - compare throughput and obvious regressions
  - exercise the same visible URLs under load
- Constraint: JMeter evidence is supplemental and cannot substitute for functional parity evidence

## Fallback Matrix

Fallbacks are per-capability. Losing one capability does not justify dropping another.

| Capability axis | Primary | Fallback | Known gap |
|---|---|---|---|
| `infra-tier` with Docker available | Testcontainers or equivalent real containers for DB and async infrastructure | Embedded or local-test substitutes only for the affected dependency | Lower infrastructure fidelity; restart, delivery, and SQL-compat behavior may diverge |
| `browser-tier` with Node.js and Playwright workspace available | Playwright browser E2E | MockMvc plus streaming/client integration tests | No real browser rendering, static asset, or DOM-event validation |
| Playwright install fails after workspace bootstrap | Playwright | MockMvc plus streaming/client integration tests | Same browser gap; record exact install error and escalate |
| Docker and Node.js both unavailable | Full primary stack | MockMvc plus embedded infrastructure only | Combined infrastructure and browser fidelity loss |

Rules:

- No Docker never justifies dropping Playwright if Node.js is available.
- No Node.js never justifies dropping Docker-backed integration tests.
- Any fallback use requires the exact command attempted, exact error output, and explicit
  `knownGaps` in the evidence bundle.

## Environment Requirements

- JDK 17 or later for the Spring Boot 3 target
- Maven on PATH
- Docker daemon running for the primary infrastructure lane
- Node.js on PATH for browser E2E
- Dedicated Playwright workspace and Chromium browser installation before tester execution
- Test profile that preserves `/daytrader` context path, deterministic seed data, and explicit
  configuration overrides for order modes and streaming toggles

## Test Data Strategy

- Seed a deterministic baseline dataset for traders, holdings, quotes, and operator config before
  each suite or suite group
- Use unique user IDs for registration and scenario-driven creations to prevent cross-test leakage
- Reset mutable runtime configuration to the canonical baseline before every suite that depends on
  order mode, market-summary interval, quote-publish toggle, long-run flag, or display-order-alerts
- Run order-flow assertions separately for `Sync`, `Async`, and `Async_2-Phase`
- For async completion, poll for observable completion conditions or streamed updates; do not use
  fixed sleeps as the primary oracle
- Admin flows that rebuild or repopulate the database must run in isolated suites so they do not
  invalidate other test data unexpectedly

## Acceptance Criteria By Journey

| Journey | PASS criteria |
|---|---|
| `J1` | Valid login reaches authenticated home, invalid login remains on welcome, protected requests without session fall back correctly, logout invalidates the session |
| `J2` | Successful registration creates a new user and lands in an authenticated state; mismatch/error cases preserve the failure surface and messaging |
| `J3` | Quote data renders for requested symbols, buy creates an order confirmation with expected fields, order-alert visibility matches toggle state, and all three order modes preserve their visible timing semantics |
| `J4` | Portfolio renders with and without holdings, sell produces the expected confirmation, account page shows order history behavior, and profile validation messages remain observable |
| `J5` | Config page exposes current runtime settings, update/reset/rebuild/repopulate actions return explicit success or failure results, and no action silently degrades |
| `J6` | REST quote contract returns expected JSON shapes, SSE emits initial plus follow-up data, WebSocket updates market summary without full reload, and disconnect behavior is observable |
| `J7` | Representative scenario and primitive endpoints remain reachable with the same class of observable output, and alternate/docs surfaces remain directly addressable |

## Required Evidence Bundle

Every implementation task and the final validation run must publish evidence in a deterministic,
reviewable format.

### Per-task evidence

- Exact test/build command executed
- Passed, failed, and skipped counts
- Test report artifacts (`surefire`, `failsafe`, Playwright HTML report, or equivalent)
- For any fallback used, the exact failed command and output that forced it

### Final validation evidence

- Clean-build log showing a successful package build from a clean workspace
- Startup log plus readiness probe output against the preserved `/daytrader` surface
- Integration test reports for HTML/API/admin/scenario coverage
- Streaming transcripts or assertions for SSE and WebSocket flows
- Playwright report for browser journeys, including screenshots on failure
- Seed/reset evidence for test data and configuration baselines
- Database-side assertions or fixture checks for order completion and persistence-sensitive flows
- Supplemental JMeter summary only if executed after functional parity passes

## Validation Review Expectations

The final reviewer must confirm all of the following:

- Every critical journey `J1` through `J7` has at least one executable test and captured evidence
- `REQ-022` through `REQ-024` are covered by dedicated contract tests, not only browser tests
- Order-mode coverage includes `Sync`, `Async`, and `Async_2-Phase`
- Any durable async replacement is exercised with real infrastructure in the primary lane
- Any fallback use is explicitly justified with exact command failure output and declared gaps
- Missing evidence is treated as FAIL, not as assumed coverage
- Verdicts are binary: PASS only when all required evidence exists and no HIGH/CRITICAL findings
  remain; otherwise FAIL

## Unit and Integration Test Infrastructure Expectations

Implementation work must create and maintain the following scaffold:

- Shared Spring Boot `test` profile with authoritative configuration defaults
- Base classes or fixtures for:
  - authenticated-session setup
  - seeded quote/account/holding data
  - config reset helpers
  - async completion polling helpers
  - SSE and WebSocket capture helpers
- Per-slice test skeletons at minimum for:
  - auth and welcome flow
  - trading flow
  - account and portfolio flow
  - admin/config flow
  - streaming contracts
  - scenario and primitive reachability
- External dependency strategy:
  - real containerized instances for the canonical relational database and any durable async
    transport in the primary lane
  - no manual environment setup required to run module-level tests
- Execution rule:
  - every implementation task must run the narrowest relevant module-level tests and report
    pass/fail/skip counts; a task with failing tests is not complete

## Findings

- CRITICAL blockers to strategy definition: none
- Risks to carry into execution:
  - the project currently has no automated parity suite, so all functional validation must be
    created during implementation
  - Playwright is not yet bootstrapped in the repo and must be added explicitly before browser-E2E
    execution
  - the authoritative containerized database/async matrix must align with `t5` when that artifact
    lands