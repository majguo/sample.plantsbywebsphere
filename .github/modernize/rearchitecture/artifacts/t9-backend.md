# t9 - Spring Boot WAR shell, platform shell, and web asset scaffold

## Summary

Scaffolded the DayTrader8 Spring Boot 3 WAR runtime on the servlet stack while preserving `src/main/webapp` as the authoritative web root. The shell now packages as an executable WAR, starts on `/daytrader`, serves the existing welcome page assets, and keeps JSP plus JSF bootstrap wiring in place without prematurely activating the legacy JSF CDI bean layer.

## Deliverables

- Replaced the Liberty-rooted Maven build with a Spring Boot 3 WAR shell in `pom.xml`.
- Added the Boot entrypoint and WAR initializer in `src/main/java/com/ibm/websphere/samples/daytrader/boot/`.
- Added MVC and JSF compatibility bootstrap classes in `src/main/java/com/ibm/websphere/samples/daytrader/web/config/` and `src/main/java/com/ibm/websphere/samples/daytrader/web/jsfcompat/`.
- Added Boot runtime defaults in `src/main/resources/application.yml`.
- Migrated `src/main/webapp/WEB-INF/web.xml` to Jakarta EE 10 servlet and faces namespaces.

## Decisions

- Kept a temporary `javax:javaee-api` provided-scope compile bridge so the existing Java EE source still packages while later tasks port behavior slices to Jakarta/Spring-native code.
- Excluded `com.ibm.websphere.samples.daytrader.web.jsf.*` from Spring component scanning so the shell boots cleanly without pulling the unmigrated legacy JSF/CDI bean layer into the Boot application context. JSF/XHTML runtime reattachment remains owned by `t15`.
- Used plain Mojarra (`org.glassfish:jakarta.faces`) for scaffold-level Faces runtime support instead of a broader JoinFaces integration, because the broader starter eagerly activated legacy CDI wiring before the business seam exists.

## Test Results

- Command: `$env:JAVA_HOME="$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path="$env:JAVA_HOME\bin;" + $env:Path; mvn -DskipTests package`
- Command: `$env:JAVA_HOME="$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path="$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19080`
- Passed: 2
- Failed: 0
- Skipped: 0

## Notes For Downstream Tasks

- The workstation default Maven runtime is Java 11; validation passed only after switching `JAVA_HOME` to the installed Microsoft JDK 17. Downstream Boot 3 tasks should either set the workspace runtime to JDK 17+ or use the same per-command override.
- The legacy `web.jsf` package is intentionally not Spring-scanned in this scaffold. Reintroduce those beans only when their `javax.enterprise`/`javax.inject` usage is migrated or bridged deliberately in the dedicated JSF compatibility task.