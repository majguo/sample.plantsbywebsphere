# Tech Stack

## Core Runtime

- Java source/target: 1.8
- Build tool: Maven
- Packaging: WAR
- Current server runtime: Open Liberty via `liberty-maven-plugin`

## Java EE / Jakarta-Predecessor APIs in Active Use

- Servlet 4.0
- JSP and JSTL/taglibs
- JSF 2.3
- CDI 2.0
- JAX-RS 2.1
- WebSocket 1.1
- JPA 2.2 style usage with persistence descriptor version 2.1
- EJB 3.2
- JMS / MDB
- Bean Validation 2.0
- JSON-P / JSON-B related APIs
- Concurrency utilities via `ManagedExecutorService`
- `UserTransaction` in direct JDBC mode

## Dependency Highlights

- `javax:javaee-api:8.0` with provided scope
- `taglibs:standard:1.1.1`
- `javax.xml.bind:jaxb-api:2.3.0`
- Derby driver for test/resource packaging

## Current Runtime-Managed Resources

- JTA datasource `jdbc/TradeDataSource`
- JMS queue and topic factories
- JMS destinations for order broker queue and quote streamer topic
- Managed executor service usage in CDI/EJB/direct JDBC flows
- Liberty keystore and HTTP endpoint configuration

## Migration Significance

- The codebase is fully `javax.*` based. Spring Boot 3 requires Jakarta namespaces and Spring 6
  runtime semantics, so the migration is a runtime and API model replacement, not a library bump.
- Liberty-specific configuration, plugin workflow, and embedded messaging need explicit Spring Boot
  replacements or containment decisions.