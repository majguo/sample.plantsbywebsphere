# Liberty Stop Before Clean Smoke

When Liberty runs from `target`, stop it before `mvn clean` so Derby does not lock the build tree.

## What Happened
In `sample.daytrader8` task `t2.1.1`, `mvn -B -DskipTests clean package` failed because the active
Liberty runtime launched from `target\liberty\wlp` kept Derby file
`target\liberty\wlp\usr\shared\resources\data\tradedb\seg0\cd1.dat` open. `mvn -B liberty:stop`
released the lock immediately, and the same clean package command then succeeded.

## Takeaway
If a baseline Liberty smoke flow uses mutable runtime data under `target`, make stop-before-clean
the default deterministic build procedure. Defer relocating runtime data out of `target` until a
later design task actually needs concurrent runtime and build activity.

## History
- 2026-05-27 (sample.daytrader8/t2.1.1): initial