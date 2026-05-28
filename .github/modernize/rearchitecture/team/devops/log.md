# DevOps Log

## [t2.1.1] Resolved deterministic clean-build smoke lock
- The active runtime lock came from the Liberty process launched out of `target\liberty\wlp`, not from Maven itself.
- `server.xml` points embedded Derby at `${shared.resource.dir}/data/tradedb`, so the live runtime mutates files inside `target`.
- `mvn -B liberty:stop` is sufficient to release the lock and make `mvn -B -DskipTests clean package` deterministic again.
- Reusable pattern: codify smoke evidence as stop-before-clean when mutable runtime state lives under the build directory.
- Learnings consumed: [(none)]

## [t20.3] Added repo-local DB2 primary infra lane for Boot runtime validation
- Replaced the legacy Liberty-only `Dockerfile-db2` path with a pinned compose setup around `icr.io/db2_community/db2:11.5.9.0` plus PowerShell helpers that generate matching DB2 and Boot env files under `target/`.
- The compose lane reached Docker health `healthy`, and the packaged WAR reached real DB2 connectivity using the generated datasource variables.
- The remaining startup blocker is backend-owned Flyway configuration: DB2 runtime currently loads both `common/V1__baseline.sql` and `db2/V1__baseline.sql`, which Flyway rejects as duplicate version `1` migrations.
- Keeping the generated password in `target/daytrader-db2.env` makes repeated local starts deterministic without checking secrets into git.
- Learnings consumed: [tester/primary-infra-lane-needs-boot-db2-wiring, backend/flyway-10-db2-and-derby-plugins]
