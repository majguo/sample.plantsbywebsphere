# Active Boot Boundary Vs Dormant Legacy Source

For DayTrader8 architecture reviews, judge Jakarta conformance on the active Spring Boot compile path, not on excluded legacy source inventory.

## What Happened
During `sample.daytrader8/t17.2`, the repo still contained many Liberty-era `javax.*` classes under
legacy servlet, JSF, WebSocket, JAX-RS, MDB, and EJB paths. The remediation was still architecture-
conformant because `pom.xml` removed Java EE umbrella dependencies and the Maven compiler excludes
kept those packages off the active Boot build. The only remaining `javax` import on the active path
was `javax.sql.DataSource`, which is JDK-owned rather than Java EE compatibility drift.

## Takeaway
Future review tasks should distinguish dormant archived source from active build surface. For this
repo, treat non-JDK `javax.*` on the non-excluded Boot compile path as a review failure, but do not
reopen the finding solely because archived Liberty-era sources still exist behind the compiler
exclusion boundary.

## History
- 2026-05-28 (sample.daytrader8/t17.2): initial