# t2 - Current Architecture and Migration Risk Analysis

## Summary

DayTrader8 is a single-module Java EE 8 WAR built with Maven and deployed to Open Liberty. The
application surface is broader than a basic MVC webapp: it combines servlet and JSP flows, JSF 2.3
views with CDI-backed session beans, JAX-RS JSON endpoints, WebSocket endpoints, JPA entities,
EJB session beans, JMS/MDB asynchronous processing, Liberty-managed executors, and Liberty server
configuration for datasource and messaging resources.

The most important architectural finding is that the codebase already exposes a stable business
service seam through `TradeServices`. Both the servlet stack and the JSF/JAX-RS/WebSocket entry
points resolve a `TradeServices` implementation based on `TradeConfig` runtime mode. That seam is
the best anchor for the Spring Boot 3 rewrite: preserve controller contracts and UI behavior above
it while replacing the Java EE runtime facilities behind it.

## Smoke Baseline

- Build command: `mvn -B -DskipTests clean package`
- Build result: SUCCESS
- Build time: ~31s
- Startup command: `mvn -B liberty:run -DskipTests`
- Startup result: inconclusive within this task window because first-run Liberty feature
  provisioning was still active and `http://localhost:9080/daytrader` was not yet accepting
  connections during the probe window.

## Deliverables

- [t2-architect-project-structure.md](./t2-architect-project-structure.md) - functional domains,
  layers, and migration surface size
- [t2-architect-tech-stack.md](./t2-architect-tech-stack.md) - current runtime and framework
  inventory
- [t2-architect-data-model.md](./t2-architect-data-model.md) - persistent entities and schema
  coupling
- [t2-architect-architecture-summary.md](./t2-architect-architecture-summary.md) - control flow,
  module boundaries, and architectural seams
- [t2-architect-migration-risks.md](./t2-architect-migration-risks.md) - severity-ranked migration
  risks with mitigation
- [t2-architect-infrastructure.md](./t2-architect-infrastructure.md) - Liberty resources,
  messaging, and environment dependencies
- [t2-architect-test-coverage.md](./t2-architect-test-coverage.md) - automated validation posture
  and evidence gaps
- [t2-architect-deployment.md](./t2-architect-deployment.md) - current packaging and deployment
  model

## Readiness for T4

T4 should treat `TradeServices` as the canonical application-service contract and design the Spring
Boot target as adapter layers around that contract: preserved web/API/WebSocket behavior above it,
Spring-managed persistence, scheduling, messaging, and concurrency below it.