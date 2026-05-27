# Spring Boot War And Jsf Compat Layer

For DayTrader8 parity, Spring Boot 3 must use WAR packaging and an explicit JSF compatibility layer instead of executable-JAR packaging or implicit JSF deprecation.

## What Happened
During `sample.daytrader8/t4`, the target design had to preserve directly reachable JSP and XHTML surfaces while migrating off Liberty. Spring Boot's servlet guidance makes WAR packaging the safe path for `src/main/webapp` and embedded-container JSP support, while a Boot-compatible JSF integration is required if XHTML parity remains in scope.

## Takeaway
Future implementation and planning tasks should assume: executable WAR packaging, preserved `src/main/webapp`, and a dedicated JSF compatibility slice aligned to the chosen Boot minor. Do not plan an executable JAR or silently drop XHTML pages unless a later approved artifact changes scope.

## History
- 2026-05-27 (sample.daytrader8/t4): initial