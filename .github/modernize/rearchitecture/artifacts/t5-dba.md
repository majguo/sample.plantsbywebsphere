# t5 - Persistence Migration and Schema Compatibility Strategy

## Summary

DayTrader8 can migrate to Spring Boot 3 without a parity-breaking relational redesign. The current
application already runs two persistence styles over the same six logical tables: JPA/EJB entities
and direct JDBC. The Spring Boot 3 target should preserve that schema contract, stop runtime-time
DDL generation, and standardize core trading writes behind one Spring-managed transactional model.

This strategy therefore fixes the target around:

- same logical schema and table names for parity (`ACCOUNTPROFILEEJB`, `ACCOUNTEJB`,
  `HOLDINGEJB`, `ORDEREJB`, `QUOTEEJB`, `KEYGENEJB`)
- one canonical write path for core trade mutations under the `TradeServices` seam
- vendor-owned bootstrap DDL for Derby and DB2, but Boot-owned migration orchestration
- zero required destructive schema changes in the parity phase

## Source-Anchored Baseline

### Existing logical schema

The current Derby and DB2 DDL define the same application-level schema surface:

- `ACCOUNTPROFILEEJB`: trader identity and profile fields
- `ACCOUNTEJB`: account balance, counts, timestamps, profile reference
- `HOLDINGEJB`: current holdings by account and quote
- `ORDEREJB`: order lifecycle rows, account/quote/holding references, open/completion timestamps
- `QUOTEEJB`: market data rows keyed by symbol
- `KEYGENEJB`: row-based key allocator for `account`, `holding`, and `order`

### Compatibility-critical observations

- Primary keys and supporting indexes exist, but the shipped DDL does not add foreign-key
  constraints. Current code therefore tolerates application-managed referential integrity.
- JPA entities map directly to the same table and column names used by direct JDBC.
- ID generation is schema-backed, not database-native: JPA uses `@TableGenerator` and direct JDBC
  uses `KEYGENEJB` row locking and block allocation.
- DB2 DDL includes physical storage and partitioning clauses that are environment-specific; Derby
  DDL is simpler but defines the same logical tables and indexes.
- Config/admin flows select a DDL script by detected database product and then recreate or populate
  the database through application code.

## Canonical Target Persistence Model

### Decision

Use Spring-managed transactions plus Jakarta Persistence as the authoritative write model for core
trading aggregates. Allow JDBC only as an infrastructure detail where parity depends on explicit SQL
 locking or vendor DDL execution.

### What is canonical

- `AccountProfile`, `Account`, `Order`, `Holding`, and `Quote` remain mapped as JPA entities to the
  existing table names and column names.
- Business writes for register, buy, sell, complete-order, closed-order acknowledgement, and quote
  updates execute inside one Spring transaction boundary per business step.
- Locking-sensitive infrastructure operations may use `JdbcTemplate` or native queries behind the
  same service boundary:
  - `KEYGENEJB` allocation if the Spring/JPA generator path cannot preserve current allocation and
    lock behavior under load
  - quote row locking equivalent to the current `SELECT ... FOR UPDATE`
  - bootstrap DDL execution and bulk sample-data population

### What is not allowed

- No parallel “direct JDBC trade service” and “JPA trade service” runtime modes in Boot.
- No Hibernate-managed schema creation or drift (`ddl-auto=create`, `update`, or `create-drop`) in
  parity environments.
- No silent remapping of table names, column names, or identifier strategy away from `KEYGENEJB`
  during the parity phase.

## Transaction and Commit Strategy

### T1. Registration

- One transaction creates `ACCOUNTPROFILEEJB` and `ACCOUNTEJB` together.
- `PROFILE_USERID` remains the join column from account to profile.
- No schema change required.

### T2. Buy order submission

Current observable behavior must remain intact:

- create `ORDEREJB` row in `open` state
- debit account balance before completion becomes visible
- in sync mode, create holding, close order, and update quote price/volume before response returns
- in async modes, persist the open order and accepted balance mutation before durable handoff

Target transaction split:

- Sync mode: one transaction for order create, balance debit, holding create, order close, quote
  update, completion timestamp
- Async mode: transaction A persists order create + balance debit + durable handoff record/message;
  transaction B completes holding creation + order close + quote update

### T3. Sell order submission

Current observable behavior must remain intact:

- create `ORDEREJB` row in `open` state
- mark holding as in-flight by setting `PURCHASEDATE = 0` equivalent sentinel behavior
- credit account balance before completion becomes visible
- on completion, remove holding or cancel if already absent

Target transaction split:

- Sync mode: one transaction for order create, holding in-flight marker, balance credit, holding
  removal, order close/cancel, quote update
- Async mode: transaction A persists order create + holding in-flight marker + balance credit +
  durable handoff; transaction B removes holding or cancels order, then updates quote/order status

### T4. Complete-order idempotency

The completion path must reject or no-op rows already in terminal states:

- `completed`
- `alertcompleted`
- `cancelled`

Boot services must preserve the current duplicate-completion protection before mutating holdings or
quotes.

### T5. Closed-order acknowledgement

`getClosedOrders` currently performs a state transition from `closed` to `completed`, or deletes the
rows in long-run mode. The target must keep that cleanup behavior explicit and transactional rather
than treating it as incidental read-side logic.

## Schema Compatibility Strategy

### Phase 1 - Parity baseline

Adopt the existing logical schema as the Boot 3 baseline with no required relational changes.

Rules:

- preserve existing table names and uppercase column names
- preserve nullable relationships exactly where the current DDL allows them
- preserve `KEYGENEJB` and its seeded rows (`account`, `holding`, `order`)
- preserve shipped secondary indexes at minimum:
  - account by `PROFILE_USERID`
  - holding by `ACCOUNT_ACCOUNTID`
  - order by `ACCOUNT_ACCOUNTID`
  - order by `HOLDING_HOLDINGID`
  - closed orders by `(ACCOUNT_ACCOUNTID, ORDERSTATUS)`
- keep DB2 vendor partitioning/tablespace concerns as deployment-specific bootstrap DDL, not as
  JPA mapping concerns

### Phase 2 - Boot migration ownership

Use Flyway as the single migration orchestrator for Spring Boot 3.

Rules:

- baseline the existing Derby and DB2 schemas instead of regenerating them
- store vendor-specific bootstrap scripts separately from incremental migration scripts
- configure Boot to validate mappings against the existing schema on startup
- make every forward migration idempotent or explicitly version-gated

Recommended startup posture:

- local Derby and DB2 parity environments: Flyway `baseline` + `migrate`
- ORM schema setting: `validate`
- bootstrap DDL scripts only for empty environments or explicit admin rebuild flows

### Phase 3 - Deferred hardening after parity sign-off

The following are valuable but are out of the parity-critical path and must not be introduced until
data profiling proves they are safe:

- foreign-key constraints between account/profile, holding/account, holding/quote, order/account,
  order/holding, and order/quote
- `NOT NULL` tightening on relationship columns that are nullable today
- check constraints for `ORDERSTATUS` and `ORDERTYPE`
- password storage redesign
- replacement of `KEYGENEJB` with database-native sequences/identity columns

## Compatibility Assumptions and Data Risks

### Assumptions being preserved

- existing rows in all six tables can be read without transform
- existing `KEYGENEJB` values are authoritative and continue monotonically
- some historical rows may rely on absent foreign keys and nullable relationships
- the `PURCHASEDATE = epoch/sentinel` sell-in-flight convention remains part of runtime behavior

### Risks to call out explicitly

- Adding foreign keys in the parity phase could fail on pre-existing orphaned rows.
- Replacing `KEYGENEJB` without data migration would break both JPA and direct-ID expectations.
- Allowing Hibernate to update the schema could silently add constraints or rename indexes in ways
  the current DDL and admin flows do not expect.
- DB2 physical DDL is not portable; it must remain environment-scoped and not leak into the Derby
  development default.

### Irreversible changes

None are required for the parity phase.

If future work introduces foreign keys, `NOT NULL` constraints, type narrowing, password hashing
replacement, or native sequence migration, those changes are irreversible enough to require a
separate data-audit artifact and rollback plan.

## Admin and Bootstrap Strategy

### Rebuild tables

- Keep explicit operator-triggered rebuild behavior separate from application startup.
- Replace servlet/JSF-owned DDL execution with a Boot admin service that selects the vendor script
  intentionally.
- Preserve current failure behavior when database product detection or DDL resolution fails.

### Populate sample data

- Preserve sample quote/user generation semantics for benchmark parity.
- Execute populate flows through the same canonical trade services where parity matters, so seeded
  holdings and orders exercise the same transactional behavior as normal requests.

### Local Derby operational constraint

- The current local Derby files live in a runtime-controlled directory that has already caused
  clean-build lock contention.
- Boot migration design should move local mutable Derby data out of build output ownership or make
  stop-before-clean an explicit dev/test lifecycle rule.

## Downstream Requirements

### For backend implementation

- Port entities to `jakarta.persistence` without renaming tables or columns.
- Implement one Spring `@Transactional` trade service layer for core writes.
- Keep any JDBC helpers behind that layer and document them as infrastructure, not alternate
  business modes.
- Disable ORM-driven DDL generation in parity environments.

### For architecture and devops

- Persist messaging handoff for async order completion in the same transaction that accepts the
  order.
- Treat Derby local data location as an environment decision, not a byproduct of the packaging
  layout.
- Carry both Derby and DB2 bootstrap scripts forward as supported environment variants.

## Recommended Implementation Sequence

1. Freeze the current schema as the migration baseline and add Boot startup validation against it.
2. Port entities and repository mappings to Jakarta APIs with no logical schema changes.
3. Implement the Spring transactional trade service and move all core writes behind it.
4. Isolate JDBC-only infrastructure helpers for key allocation, lock-sensitive SQL, and bootstrap.
5. Replace servlet/JSF admin DDL execution with Boot-owned admin services using vendor-aware
   scripts.
6. Profile live data before proposing any hardening constraints.

## Test Results

- Command: `git diff --check -- .github/modernize/rearchitecture/artifacts/t5-dba.md`
- Passed: 1
- Failed: 0
- Skipped: 0