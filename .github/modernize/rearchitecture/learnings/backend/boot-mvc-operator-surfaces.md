# Boot MVC operator surfaces

Spring Boot 3 should own the `/config` and `/scenario` compatibility surfaces through MVC controllers while leaving the existing JSP pages and scenario-forward contract in place.

## What Happened
In sample.daytrader8 task t13, the operator/configuration slice still lived in legacy `javax.servlet` classes and a CDI/JNDI-managed DB utility even though Boot had already introduced the canonical application seam. The stable migration path was to keep `config.jsp` and `runStats.jsp` as the rendered contract, move mutable runtime settings into `RuntimeSettingsService`, convert `TradeDirectDBUtils` into a Spring-managed service, and expose `/config` and `/scenario` from Boot MVC.

## Takeaway
For servlet-era DayTrader admin surfaces, migrate the entrypoint and state authority before touching the page markup. Use Boot MVC controllers at the preserved URLs, keep `TradeConfig` as a compatibility mirror for legacy JSP reads, and route scenario traffic through the same `/app?action=*` surface rather than inventing a separate execution path.

When preserving `/config`, keep the legacy operator post-action flow intact as well: `buildDB` and `buildDBTables` should hand control back to the config surface with status messaging after the DB utility output rather than terminating the request at the raw utility stream.

## History
- 2026-05-28 (sample.daytrader8/t13): initial
- 2026-05-28 (sample.daytrader8/t13): recorded that Boot `/config` must re-render after build actions, not stop at the utility output
