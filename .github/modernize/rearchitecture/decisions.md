## [TEAMLEAD] [t1] - 2026-05-27

**Decision**: Adopt Constitution v1.0.0 for the DayTrader8 migration.
**Rationale**: The project will be treated as a Spring Boot 3 rewrite with strict feature/API parity, Jakarta-only forward development, same-schema data migration, preserved authentication behavior, and evidence-backed acceptance.

## [ARCHITECT] [t2] - 2026-05-27

**Decision**: Use `TradeServices` as the canonical migration seam for DayTrader8.
**Rationale**: Contracts above the seam must be preserved, while Liberty, EJB, JMS/MDB, CDI eventing, scheduling, and resource wiring beneath it can be redesigned for Spring Boot 3.

## [ARCHITECT] [t2.1] - 2026-05-27

**Decision**: Fix the Spring Boot 3 target around explicit runtime contracts rather than container equivalence.
**Rationale**: The rewrite will preserve application-service, session-auth, order semantics, event fan-out, transaction boundaries, and authoritative configuration behavior, while allowing implementation changes beneath those contracts.

## [ARCHITECT] [t2.1] - 2026-05-27

**Decision**: Preserve the DayTrader runtime contracts explicitly in downstream design.
**Rationale**: Architecture, persistence, and testing must center on the `TradeServices` seam, auth/session behavior, order-mode semantics, event fan-out, transaction model, and one Boot-owned configuration surface.

## [TEAMLEAD] [t4.1] - 2026-05-27

**Decision**: Govern readiness evidence as staged binary states.
**Rationale**: Downstream work must distinguish build, startup, surface, journey, and release readiness; missing evidence is a FAIL and startup proof cannot substitute for parity proof.