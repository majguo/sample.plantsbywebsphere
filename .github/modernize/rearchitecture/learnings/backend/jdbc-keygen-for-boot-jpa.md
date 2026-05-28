# JDBC key generation for Boot JPA

Spring Boot 3 on DayTrader8 should allocate `KEYGENEJB` identifiers through JDBC rather than Hibernate table generators when preserving the shipped Derby/DB2 schema.

## What Happened
In sample.daytrader8 task t10, the first Boot JPA startup passed compile and packaging but failed schema validation because Hibernate 6 expected `KEYGENEJB.KEYVAL` to behave like a `BIGINT` for `@TableGenerator`, while the shipped schema keeps it as `INTEGER`. Changing the schema would have violated the same-schema requirement, so the fix was to remove table generators from `AccountDataBean`, `HoldingDataBean`, and `OrderDataBean` and allocate IDs through a small JDBC helper backed by the existing `KEYGENEJB` table.

## Takeaway
For this migration, keep entity IDs explicit and let `KeySequenceJdbcRepository` own `KEYGENEJB` updates. Do not reintroduce Hibernate table generators unless the schema contract changes and the DBA signs off on the type drift.

## History
- 2026-05-28 (sample.daytrader8/t10): initial