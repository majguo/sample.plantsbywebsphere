## [t1] Established Spring Boot 3 rewrite constitution
- The project must be treated as a rewrite, not an in-place upgrade, because Liberty and `javax`
  runtime dependencies cut across the entire WAR.
- A conservative platform floor of JDK 17 was chosen because it is the minimum Spring Boot 3
  baseline and avoids forcing a higher-LTS jump before architecture review.
- The constitution locks in full parity, same-schema migration, and auth preservation so later plan
  and gate work can reject scope drift deterministically.
- Learnings consumed: [(none)]