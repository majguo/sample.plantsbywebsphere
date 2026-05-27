# DevOps Log

## [t2.1.1] Resolved deterministic clean-build smoke lock
- The active runtime lock came from the Liberty process launched out of `target\liberty\wlp`, not from Maven itself.
- `server.xml` points embedded Derby at `${shared.resource.dir}/data/tradedb`, so the live runtime mutates files inside `target`.
- `mvn -B liberty:stop` is sufficient to release the lock and make `mvn -B -DskipTests clean package` deterministic again.
- Reusable pattern: codify smoke evidence as stop-before-clean when mutable runtime state lives under the build directory.
- Learnings consumed: [(none)]