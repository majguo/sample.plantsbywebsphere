## [t3] Inventory complete parity surface for DayTrader8
- The authoritative product surface is broader than the core trading flow: admin/configuration, scenario driver, docs-linked utilities, REST/SSE/WebSocket feeds, and primitive launcher destinations are all directly addressable and must be inventoried.
- `TradeAppServlet`, `TradeConfigServlet`, `TradeScenarioServlet`, `TradeConfig.getPage(...)`, and `web_prmtv.html` were the highest-value anchors for enumerating the observable surface without drifting into implementation design.
- The repo has no pre-existing PM log file; created one for this role.
- Learnings consumed: [(none)]
