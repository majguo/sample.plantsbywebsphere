# Flyway 10 DB2 and Derby plugins

Spring Boot 3 DB2 support in DayTrader8 needs Flyway 10.x with explicit Derby and DB2 plugin modules, not just the JDBC driver.

## What Happened
In sample.daytrader8 task t20.2, the packaged Boot WAR could not start against DB2 because it only
carried Derby plus Flyway 9.22.3. Adding `com.ibm.db2:jcc` fixed the missing-driver failure, but
the repo still needed a Flyway line that exposes `flyway-database-db2`. The stable backend choice
was to pin Flyway to `10.22.0`, add both `flyway-database-derby` and `flyway-database-db2`, and
remove the hard-coded Derby driver so Boot can auto-detect the vendor from the datasource URL.

In sample.daytrader8 task t20.3.1, DB2 startup still failed after the plugin upgrade because the
Boot Flyway config loaded `db/migration/common` and `db/migration/db2` together, and both folders
contained `V1__baseline.sql`. The stable fix was to treat baseline migrations as vendor-specific
and move post-baseline shared DDL into a separate `db/migration/shared` location that both Derby
and DB2 can load.

## Takeaway
For this migration, treat DB2 support as a three-part runtime contract: `jcc` on the packaged WAR,
Flyway 10.x plus both Derby/DB2 database plugins, and vendor-aware migration-location wiring that
adds `classpath:db/migration/db2` only when the datasource points at DB2. When baseline DDL differs
by vendor, never keep the same Flyway version in both the default and vendor-specific locations;
put later shared migrations in a separate neutral folder instead.

## History
- 2026-05-28 (sample.daytrader8/t20.2): initial
- 2026-05-28 (sample.daytrader8/t20.3.1): split shared follow-on migrations from vendor-specific baselines to avoid duplicate `V1` discovery on DB2