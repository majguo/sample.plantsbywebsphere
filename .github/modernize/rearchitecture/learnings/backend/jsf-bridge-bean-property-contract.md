# JSF bridge bean property contract

Spring-managed JSF bridge beans must keep exact JavaBean getter/setter naming for every XHTML-bound property or Faces postback silently drops the submitted value.

## What Happened
In sample.daytrader8 task t15, the Boot-hosted JSF bridge layer was already the correct compatibility seam, but `TradeConfigJsfBridge` exposed `getMaxQuotes()` with a misspelled `setmaxQuotes(...)`. `config.xhtml` binds `#{tradeconfig.maxQuotes}`, so the page could render the current value but could not write updates back through the bridge.

## Takeaway
Treat bean property names in `web.jsfcompat` as part of the preserved surface contract. When migrating or reviewing JSF bridges, verify XHTML-bound fields with bean introspection or equivalent focused tests so property writer drift is caught before runtime.

## History
- 2026-05-28 (sample.daytrader8/t15): initial