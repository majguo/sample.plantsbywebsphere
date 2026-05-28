# t10 - Canonical TradeServices facade, business seam, and same-schema persistence

## Summary

Implemented the first Spring Boot 3-owned business seam under the existing `TradeServices` contract. The application now has a canonical `TradeServicesFacade`, Spring-managed auth/order services, same-schema JPA repositories, Flyway baseline scripts for Derby and DB2, runtime settings wiring, and JDBC-backed key allocation that preserves the existing `KEYGENEJB` integer schema.

## Deliverables

- Added the canonical Boot-owned service seam in `src/main/java/com/ibm/websphere/samples/daytrader/application/TradeServicesFacade.java`.
- Added Spring-managed auth and order services in `src/main/java/com/ibm/websphere/samples/daytrader/application/auth/AuthenticationApplicationService.java` and `src/main/java/com/ibm/websphere/samples/daytrader/application/orders/TradeOrderApplicationService.java`.
- Added same-schema repository and key allocation infrastructure in `src/main/java/com/ibm/websphere/samples/daytrader/persistence/jpa/` and `src/main/java/com/ibm/websphere/samples/daytrader/persistence/jdbc/KeySequenceJdbcRepository.java`.
- Ported the core persistence entities to `jakarta.persistence` while keeping table and column names stable.
- Added Boot-owned runtime settings in `src/main/java/com/ibm/websphere/samples/daytrader/config/RuntimeSettingsService.java`.
- Added Flyway baselines in `src/main/resources/db/migration/common/V1__baseline.sql` and `src/main/resources/db/migration/db2/V1__baseline.sql`.
- Enabled repo-wide entity and repository scanning for the Boot runtime and configured Derby-backed JPA/Flyway startup in `pom.xml`, `src/main/resources/application.yml`, and `src/main/java/com/ibm/websphere/samples/daytrader/boot/DayTraderApplication.java`.

## Decisions

- Replaced Hibernate table generators with the JDBC `KEYGENEJB` allocator instead of changing the schema type from `INTEGER` to `BIGINT`. This preserves the shipped schema contract and matches the t5 guidance that key allocation may need infrastructure-level JDBC.
- Kept async order modes out of the canonical seam for this task. `queueOrder` and `completeOrderAsync` now fail fast rather than flattening async and 2-phase semantics into synchronous behavior. That work remains owned by `t14`.
- Kept runtime mutability behind `RuntimeSettingsService`, but synchronized its values back into legacy `TradeConfig` statics so unmigrated code paths still observe consistent settings during the transition.

## Downstream Notes

- `t11` and `t12` can bind web adapters directly to `TradeServicesFacade` without touching repositories.
- `t14` must replace the current fail-fast async placeholders with the approved durable order-work implementation.
- Derby starts successfully with the shipped local database, but Hibernate warns that Derby `10.14` is below its preferred minimum `10.15.2`. This is a runtime-environment risk, not a blocker for the seam itself.

## Test Results

- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; mvn -DskipTests compile`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; mvn -DskipTests package`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19080`
- Passed: 3
- Failed: 0
- Skipped: 0