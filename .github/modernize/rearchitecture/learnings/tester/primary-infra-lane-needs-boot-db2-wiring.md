# Primary infra lane needs Boot DB2 wiring

For DayTrader's Spring Boot 3 validation, Docker availability alone is not enough to satisfy the primary infrastructure lane if the packaged Boot app cannot boot against the canonical DB target.

## What Happened
In sample.daytrader8 task t20.1, Docker was available but the container-backed lane still could not be executed. The packaged Boot WAR failed immediately when pointed at DB2 settings with `Cannot load driver class: com.ibm.db2.jcc.DB2Driver`, and the workspace also lacked the `db2jars/` directory expected by the legacy `Dockerfile-db2` path.

## Takeaway
Before declaring the primary infra lane runnable, verify three things together: Docker daemon availability, Boot runtime support for the canonical DB driver on the classpath, and a repo-local reproducible path for the DB/container assets. If any of the three is missing, publish the exact blocker and treat real-infra evidence as FAIL rather than silently reusing embedded-DB coverage.

## History
- 2026-05-28 (sample.daytrader8/t20.1): initial