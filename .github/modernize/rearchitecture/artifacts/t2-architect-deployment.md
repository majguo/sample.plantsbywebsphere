# Deployment

## Current Delivery Model

- Maven packages a WAR
- Liberty plugin supports local run via `mvn clean package liberty:run`
- Dockerfiles deploy the WAR into an Open Liberty base image
- Runtime assumes container/server-managed application lifecycle rather than embedded application
  startup

## Deployment Coupling

- Current images are Liberty-specific and expect Liberty config directories
- Messaging and datasource definitions are externalized to Liberty descriptors rather than the WAR
- Resource copying during package phase is part of the deployment contract

## Migration Considerations

- Spring Boot 3 target delivery will likely move from WAR-on-Liberty to executable JAR or layered
  container image, but that design choice belongs to t4.
- Regardless of packaging choice, deployment design must preserve context-root behavior,
  environment-specific datasource settings, and any required local data bootstrap path.
- Existing Dockerfiles are not directly reusable because they assume Liberty image conventions.