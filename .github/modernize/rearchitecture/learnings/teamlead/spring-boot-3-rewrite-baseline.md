# Spring Boot 3 Rewrite Baseline

Use rewrite governance for DayTrader8 because the migration replaces the runtime model, not just dependencies.

## What Happened
For t1 on sample.daytrader8, the repo profile and build docs showed a Java EE 8 WAR on Open
Liberty with mixed JSP, JSF, CDI, JAX-RS, and Liberty configuration. Combined with the clarified
requirement to preserve all flows and contracts on Spring Boot 3, that made an in-place upgrade
governance model too ambiguous.

## Takeaway
Treat this program as a Spring Boot 3 rewrite with strict parity constraints. Lock in three
baseline decisions early: Spring Boot 3.x plus Jakarta-only forward development, JDK 17 minimum,
and same-schema/auth preservation unless a later approved artifact changes them.

## History
- 2026-05-27 (sample.daytrader8/t1): initial