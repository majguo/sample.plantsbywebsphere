<!--
Sync Impact Report
- Version change: none -> 1.0.0
- Modified principles: none (initial adoption)
- Added sections: Migration Mode; Core Principles; Target Technology Stack; Additional Constraints; Delivery Workflow; Governance
- Removed sections: none
- Templates requiring updates: ✅ reviewed c:\Users\jiangma\.copilot\skills\creating-implementation-plan\templates\plan-template.md (aligned); ✅ reviewed c:\Users\jiangma\.copilot\skills\feature-inventory\templates\spec-template.md (aligned); ⚠ pending c:\Users\jiangma\.copilot\skills\breaking-down-tasks\templates\tasks-template.md (template not present in available skills path)
- Follow-up TODOs: none
-->

# DayTrader8 Migration Constitution

## Migration Mode

**Mode**: REWRITE

**Justification**: The current application is a Java EE 8 WAR built for Open Liberty and relies on
`javax` APIs, container-managed behavior, Liberty runtime configuration, JSP, JSF, CDI, JAX-RS,
and WebSocket endpoints. Spring Boot 3 requires Jakarta namespaces and a different runtime model,
so the migration must re-implement the application on a new Spring Boot 3 foundation while
preserving observable business behavior.

## Core Principles

### I. Functional Parity Is Mandatory
All existing user flows, HTTP endpoints, response status codes, and externally visible behaviors
MUST remain equivalent after migration unless a later approved artifact explicitly authorizes a
change. Performance and load behavior MUST meet the current production baseline with no regression.
Rationale: the clarified success condition is full parity on Spring Boot 3, not feature redesign.

### II. Rewrite the Runtime, Preserve the Business Semantics
The team MUST replace Liberty- and Java EE-specific runtime concerns with Spring Boot 3-native
implementations instead of attempting piecemeal compatibility shims across the whole codebase.
Business rules, data semantics, and workflow ordering MUST be preserved through extraction,
characterization, and re-implementation. Rationale: the project profile classifies this work as a
rewrite because Jakarta, Spring Boot, and embedded runtime concerns cut across the entire stack.

### III. Spring Boot 3 and Jakarta Are the Only Forward Path
New application code MUST target Spring Boot 3.x, Spring Framework 6.x, and Jakarta EE 10
compatible APIs. No new `javax.*`, Liberty-specific descriptors, container-managed EJB patterns,
or JSF/CDI runtime dependencies may be introduced into the target solution. Any temporary bridge
needed during execution MUST be isolated, explicitly documented, and removed before completion.
Rationale: allowing mixed runtime models would make parity verification and final cutover ambiguous.

### IV. Same Schema, Same Data, Same Authentication Contract
The migration MUST retain the existing database identity, preserve the schema in place, and limit
database changes to compatible alterations required by Spring Boot 3 operation. Existing
authentication behavior MUST be detected and preserved unless a later approved decision supersedes
it. Rationale: the clarification brief explicitly requires in-place data migration and auth
preservation.

### V. Evidence Before Acceptance
Every downstream plan, task, and validation artifact MUST identify the user journeys, API
contracts, and persistence behaviors it protects. No migration slice is complete without runnable
evidence that demonstrates parity for the affected surface, and any constitution violation is
CRITICAL. Rationale: brownfield rewrites fail when equivalence is asserted instead of proven.

## Target Technology Stack

| Component | Target Version | Notes |
|-----------|---------------|-------|
| JDK | 17 LTS minimum | Spring Boot 3 baseline; later artifacts may raise this to a newer LTS but not below 17 |
| Framework | Spring Boot 3.x | Includes Spring Framework 6.x and Jakarta EE 10 compatible APIs |
| Build Tool | Maven 3.9+ | Retain Maven as the authoritative build and dependency management tool |
| Database | Existing DayTrader schema on Derby and DB2 | Preserve current schema and data semantics across supported environments |

## Additional Constraints

The migration scope covers the full application surface: server-rendered pages, REST-style
endpoints, WebSocket behavior, database initialization/configuration flows, and operational scripts
required to run or validate DayTrader8. Work MUST proceed as vertical parity slices that keep
business capability intact across UI, service, and persistence boundaries. Runtime configuration
MUST have one authoritative source per environment; duplicate backend address or environment files
must be flagged and reconciled during planning.

## Delivery Workflow

Architecture, feature inventory, and test strategy artifacts MUST be completed before implementation
planning begins. Task breakdown MUST split work by independently executable business slice rather
than by technical layer alone. Each task MUST state the requirements it traces to, the evidence it
must produce, and any fallback validation path if the preferred environment is unavailable. Quality
gates MUST return binary PASS or FAIL verdicts only.

## Governance

This constitution governs all migration artifacts under `.github/modernize/rearchitecture/` and
supersedes conflicting local planning habits. Amendments MUST record the reason for change, update
the version according to semantic versioning, and note any downstream artifact adjustments required
for compliance. MAJOR versions represent incompatible governance changes, MINOR versions add or
materially expand principles, and PATCH versions clarify existing guidance without changing intent.
Every review of a plan, task set, or validation result MUST include an explicit constitution
compliance check. Missing evidence, missing dependency artifacts, or any deviation from these
principles is a FAIL condition for the affected downstream gate.

**Version**: 1.0.0 | **Ratified**: 2026-05-27 | **Last Amended**: 2026-05-27