# Infrastructure

## Liberty Resource Model

`src/main/liberty/config/server.xml` currently defines the application runtime contract:

- HTTP endpoint on configurable ports, defaulting to 9080/9443
- WAR deployment at context root `/daytrader`
- Derby-backed datasource `jdbc/TradeDataSource`
- JMS queue/topic factories and destinations
- Activation specs for broker and streamer MDBs
- Liberty keystore and SSL feature enablement

## Environment Variants

- Derby is the default embedded/local data path
- DB2 alternative config exists in `server.xml_db2` and supporting files/scripts
- Shell scripts switch between server flavors such as Payara and WildFly, indicating portability
  experiments but also nontrivial runtime variance

## Container and Resource Packaging

- Build copies Derby driver and prebuilt data into Liberty shared resource paths
- Dockerfiles assume Liberty image layout and WAR deployment into `/config/apps/`
- Prepackaged data under `resources/data/tradedb` is part of the runnable local environment

## Migration Implications

- Spring Boot target must define one authoritative configuration model for datasource, messaging,
  ports, SSL, and environment-specific overrides.
- Liberty activation specs and messaging-engine assumptions cannot be carried forward directly.
- Derby local bootstrap and DB2 support need an explicit target-environment story before execution
  planning.