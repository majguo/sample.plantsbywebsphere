## [t9] Scaffolded the Spring Boot 3 WAR shell and web bootstrap baseline
- Replaced the Liberty-centric build shell with a Boot 3 WAR while preserving `src/main/webapp` as the authoritative asset root.
- The workspace default Maven runtime was Java 11; packaging and startup validation required switching to the installed Microsoft JDK 17.
- JoinFaces-based starters activated unmigrated legacy JSF/CDI beans too early; plain Mojarra plus descriptor/bootstrap scaffolding was the stable shell boundary for this phase.
- Spring component scanning had to exclude `com.ibm.websphere.samples.daytrader.web.jsf.*` so the shell could boot without crossing into the later JSF compatibility task.
- Learnings consumed: [(none)]