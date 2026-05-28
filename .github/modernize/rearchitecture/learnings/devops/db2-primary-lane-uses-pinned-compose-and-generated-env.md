# DB2 primary lane uses pinned compose and generated env

For DayTrader8 Boot validation, the repo-local DB2 path should use a pinned Db2 Community compose file plus generated runtime env files instead of checked-in secrets or host-specific setup.

## What Happened
In sample.daytrader8 task t20.3, the old DB2 path was still a Liberty-era Dockerfile that depended on missing local `db2jars/` assets. The stable devops replacement was to pin `icr.io/db2_community/db2:11.5.9.0` in `compose.db2.yml`, generate the DB2 password into `target/daytrader-db2.env` on first start, and emit a matching `target/daytrader-db2-app-env.ps1` so the packaged WAR can be pointed at the same container without manual copy/paste.

## Takeaway
For this repo, treat the DB2 primary infra lane as two reproducible artifacts: a checked-in compose definition with a health check, and generated local env files under `target/` that bridge the container credentials into the Boot runtime. Keep the image pinned and keep runtime secrets out of tracked files.

## History
- 2026-05-28 (sample.daytrader8/t20.3): initial