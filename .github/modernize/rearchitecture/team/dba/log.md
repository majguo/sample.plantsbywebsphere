## [t5] Same-schema Spring Boot 3 persistence strategy
- Source confirmed six logical tables shared by both JPA and direct JDBC paths, so parity does not require a relational redesign.
- Core buy/sell semantics debit or credit balance before completion, then close/cancel orders and create/remove holdings in the completion path.
- `KEYGENEJB` is part of the compatibility surface, not just an implementation detail; both entity and direct paths rely on it.
- Current shipped DDL has PKs and indexes but no foreign keys, so hardening constraints must be deferred until data profiling proves safety.
- Derby local runtime data placement is coupled to build/runtime cleanup and should be treated as an environment concern in Boot.
- Learnings consumed: [architect/explicit-runtime-contracts-for-boot-target, architect/trade-services-migration-seam]