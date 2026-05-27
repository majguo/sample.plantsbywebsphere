# Same Schema Flyway Baseline

Spring Boot 3 persistence work should baseline the existing DayTrader schema with Flyway and keep `KEYGENEJB` plus current table names unchanged for parity.

## What Happened
In sample.daytrader8 task t5, the persistence design review found that DayTrader already uses both
JPA entities and direct JDBC against the same six logical tables. The safe Spring Boot 3 path is
therefore not a schema rewrite. The design keeps the existing tables, columns, indexes, and
`KEYGENEJB` identifier contract, disables ORM-driven DDL changes, and uses Flyway as the single
schema orchestration layer for Derby and DB2.

## Takeaway
Treat the existing schema as a baseline, not something Hibernate should recreate. Use one
transactional Spring write model for core trading logic, and keep JDBC only for infrastructure
details such as vendor DDL execution, explicit locking, or key allocation where required.

## History
- 2026-05-27 (sample.daytrader8/t5): initial