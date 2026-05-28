## [t3] Inventory complete parity surface for DayTrader8
- The authoritative product surface is broader than the core trading flow: admin/configuration, scenario driver, docs-linked utilities, REST/SSE/WebSocket feeds, and primitive launcher destinations are all directly addressable and must be inventoried.
- `TradeAppServlet`, `TradeConfigServlet`, `TradeScenarioServlet`, `TradeConfig.getPage(...)`, and `web_prmtv.html` were the highest-value anchors for enumerating the observable surface without drifting into implementation design.
- The repo has no pre-existing PM log file; created one for this role.
- Learnings consumed: [(none)]

## [t21] Feature parity sign-off review for Spring Boot 3 rewrite
- Representative parity evidence is not enough for PM sign-off when the approved inventory explicitly includes every directly reachable primitive, docs page, and alternate surface.
- The strongest local discriminator was comparing t3's broad surface inventory against the concrete runtime proofs in t20 plus the underlying integration/browser tests; that exposed missing proof for admin actions, scenario steps, docs, image JSPs, primitive echo endpoints, and broader alternate surfaces.
- t20's `overall: NEEDS_SIGNOFF` status was correct and aligned with the constitution's evidence-before-acceptance rule because the Docker-backed infrastructure lane from t6 remained unverified.
- Learnings consumed: [pm/addressable-surfaces-count-for-parity]

## [t21.1] Re-ran PM sign-off after DB2 evidence closure
- `t20.5` materially closes the real-infrastructure blocker from `t21`, but it does not change the PM requirement for per-surface proof across the approved parity inventory.
- The cheapest discriminating check was comparing the rerun artifact's exact DB2-backed evidence to the actual browser/integration coverage: the new proof closes the DB2 lane, while the remaining gaps stay open for primitive/docs/alternate surfaces, operator reset and rebuild actions, and the broader scenario action set.
- Learnings consumed: [pm/addressable-surfaces-count-for-parity, pm/signoff-needs-per-surface-proof]

## [t21.3] Re-ran PM sign-off after final evidence bundle completion
- `t21.2.2` closes the seeded DB2 regressions that previously blocked PM sign-off for operator reset/rebuild, primitive JAX-RS echo endpoints, and the named scenario-driver action set.
- The controlling PM check remained a direct comparison of `t3`'s full parity inventory against the final evidence bundle: the repaired slices are now green, but the docs-linked pages, broader alternate surfaces, broader primitive inventory, and several detailed user-visible behaviors still lack explicit proof.
- Learnings consumed: [pm/addressable-surfaces-count-for-parity, pm/signoff-needs-per-surface-proof]

## [t21.5] Re-ran PM sign-off after final surface-proof closure
- The controlling check stayed local to the prior PM blocker list: compare `t21.3`'s remaining open surface families against the exact closure counts in `t21.4.2` rather than re-inventorying the whole app.
- `t20.5`, `t21.2.2`, and `t21.4.2` now form a complete evidence chain for the approved PM scope: real-infra trading/auth baseline, seeded operator/scenario/JAX-RS repairs, and inventory-complete docs/primitive/image/alternate surface closure.
- No new PM-side parity blockers remained once `t21.4.2` reported `74` passed and `0` failed for the previously open surface families.
- Learnings consumed: [pm/addressable-surfaces-count-for-parity, pm/signoff-needs-per-surface-proof]
