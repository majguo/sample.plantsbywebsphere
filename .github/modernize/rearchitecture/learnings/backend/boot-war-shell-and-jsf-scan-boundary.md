# Boot WAR shell and JSF scan boundary

Spring Boot 3 packaging on DayTrader8 works cleanly only if the platform shell keeps the legacy JSF bean package out of Spring scanning until the dedicated JSF migration slice is ready.

## What Happened
In sample.daytrader8 task t9, the first Boot WAR scaffold packaged successfully but startup failed because legacy `web.jsf` beans annotated with `@Named` were picked up by Spring component scanning and still depended on `javax.enterprise.inject.Instance<TradeServices>`. A broader JSF starter also pulled CDI/Weld too early. The stable shell used plain Mojarra, kept `web.xml` plus a small `ServletContextInitializer` for JSF bootstrap defaults, and excluded `com.ibm.websphere.samples.daytrader.web.jsf.*` from Spring scanning.

## Takeaway
For the scaffold phase, keep the Boot WAR focused on runtime packaging, servlet context defaults, and web-root preservation. Do not let Spring instantiate legacy JSF/CDI beans before the `TradeServices` seam and namespace migration are in place. Reattach legacy JSF packages only in the dedicated JSF compatibility task.

## History
- 2026-05-28 (sample.daytrader8/t9): initial