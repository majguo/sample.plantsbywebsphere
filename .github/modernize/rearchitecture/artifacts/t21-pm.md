# t21 - Feature Parity Sign-off

## Verdict

FAIL - PM sign-off is not approved.

The Spring Boot 3 rewrite has credible implementation progress and representative runtime proof, but the available evidence does not account for every inventoried parity requirement from `t3`. Under the constitution's evidence-before-acceptance rule and the PM charter's no-partial-parity bar, this task cannot sign off the migration as feature-complete.

## Inputs Reviewed

- `t3-pm.md`
- `t6-teamlead.md`
- `t17.2-architect.md`
- `t19.1-architect.md`
- `t20-tester.md`
- `src/test/java/com/ibm/websphere/samples/daytrader/integration/journeys/DayTraderJourneyIntegrationTest.java`
- `src/test/java/com/ibm/websphere/samples/daytrader/integration/streaming/DayTraderStreamingIntegrationTest.java`
- `src/test/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeConfigControllerTest.java`
- `src/test/java/com/ibm/websphere/samples/daytrader/web/mvc/TradeScenarioControllerTest.java`

## Sign-off Findings

### CRITICAL - Full parity proof is missing for the directly reachable alternate/documentation/primitive surface

`t3` makes all directly reachable surfaces part of parity scope, including `web_prmtv.html`, linked primitive destinations, docs pages, alternate JSP image pages, and directly reachable JSF/XHTML pages. The executed validation only proves representative reachability for `/welcome.faces`, `/PingJsf.faces`, `/PingCDIJSF.faces`, and `/servlet/PingServlet`, plus one generic primitive probe in Playwright. No runtime proof was found for the primitive launcher page itself, the JAX-RS echo endpoints, the linked documentation pages, the image-based JSP variants, or the broader linked primitive inventory.

Impacted requirements: `REQ-001`, `REQ-025`, `REQ-026`, `REQ-027`, `REQ-028`, `REQ-029`.

### CRITICAL - Admin and scenario parity remains only partially proven

The runtime evidence covers `/config?action=updateConfig`, `/config?action=buildDB`, and `/scenario?action=n`. No proof was found for `/config?action=resetTrade`, `/config?action=buildDBTables`, or the scenario-driver actions for login, logout, register, home, account, profile update, quote, portfolio, buy, and sell. The PM inventory requires those operator-visible behaviors to remain available, and the current evidence set does not account for them.

Impacted requirements: `REQ-019`, `REQ-020`, `REQ-021`, `REQ-029`.

### CRITICAL - The planned primary infrastructure lane is still unverified

`t6` defined Docker-backed real infrastructure as the primary validation lane when Docker is available. `t20` explicitly reports that validation remained on the embedded Derby developer path and that the container-backed lane was not proven. Because acceptance depends on evidence rather than implementation intent, the migration cannot receive product sign-off while the authoritative runtime lane remains unverified.

Impacted requirements: acceptance blocker across `REQ-001` through `REQ-030`.

### HIGH - Several user-facing requirements are only partially evidenced rather than fully closed

The current suites prove representative browser and integration behavior for login/logout, registration mismatch and success, buy, sell, account password mismatch, config update, REST quotes, SSE welcome, and WebSocket updates. They do not yet fully prove all inventoried observable details, including invalid-login messaging, quote deep-link behavior, empty portfolio handling, show-all-orders expansion, profile-update success path, market-summary page rendering, completed-order alerts across trading pages, SSE ongoing update behavior, WebSocket browser page behavior, and the strict rule that only login and registration remain available to anonymous users.

Impacted requirements: `REQ-004`, `REQ-008`, `REQ-010`, `REQ-011`, `REQ-012`, `REQ-013`, `REQ-014`, `REQ-016`, `REQ-017`, `REQ-023`, `REQ-024`, `REQ-030`.

## Requirement Checklist

| Requirement | Status | Evidence basis | Notes |
|---|---|---|---|
| REQ-001 | Partial | `t9`, `t15`, `t19.1`, `t20` | Boot shell and representative surfaces run, but the framed shell/navigation and linked surface set are not fully proven. |
| REQ-002 | Evidenced | `t20`, Playwright login spec | Welcome/login page is reachable on the preserved JSP surface. |
| REQ-003 | Evidenced | `t20`, journey integration, Playwright login spec | Existing-trader login and session establishment are proven. |
| REQ-004 | Partial | `t20` summary only | No focused proof found for invalid-login rejection and visible error message. |
| REQ-005 | Partial | `t20`, Playwright registration spec | Registration mismatch and success are proven, but the full field/contract surface is not exhaustively closed in runtime evidence. |
| REQ-006 | Partial | `t20`, Playwright login spec | Authenticated landing is proven, but the full home-page content contract is not explicitly checked. |
| REQ-007 | Partial | journey integration, `t20` | Quote flow is exercised, but the visible quote-table content contract is not fully proven. |
| REQ-008 | Partial | implementation mapping only | No focused proof found for quote symbol deep-link behavior. |
| REQ-009 | Evidenced | journey integration, Playwright buy spec | Buy submission remains functional. |
| REQ-010 | Partial | `t20`, Playwright buy/sell spec | Order page reachability is proven, but the full order-detail field set is not explicitly checked. |
| REQ-011 | Partial | `t20`, Playwright portfolio spec | Portfolio and sell reachability are proven, but the full holdings metric contract is not explicitly checked. |
| REQ-012 | Partial | implementation mapping only | No focused proof found for explicit empty-portfolio rendering. |
| REQ-013 | Partial | `t20`, Playwright account spec | Account page renders, but show-all-orders expansion is not proven. |
| REQ-014 | Partial | `t20`, Playwright account spec | Validation failure is proven; successful profile update is not. |
| REQ-015 | Evidenced | journey integration, Playwright logout spec | Logout invalidation and return to welcome are proven. |
| REQ-016 | Partial | streaming integration, `t20` | Summary payload updates are proven, but the market-summary page rendering contract is not explicitly closed. |
| REQ-017 | Partial | implementation mapping only | No focused proof found for completed-order alerts across trading pages. |
| REQ-018 | Evidenced | `TradeConfigControllerTest`, journey integration, Playwright config spec | Config page render and mutable runtime parameters are covered. |
| REQ-019 | Partial | `TradeConfigControllerTest`, journey integration | `updateConfig` and `buildDB` are proven; `resetTrade` and `buildDBTables` are not. |
| REQ-020 | Partial | `TradeConfigControllerTest`, journey integration | Some success messaging is proven; full success/failure coverage across admin actions is not. |
| REQ-021 | Partial | `TradeScenarioControllerTest`, journey integration | `/scenario?action=n` is proven; the supported synthetic trade steps are not. |
| REQ-022 | Evidenced | journey integration, `t20` | REST quote GET and POST contracts are proven. |
| REQ-023 | Partial | streaming integration, `t20` | SSE welcome behavior is proven; ongoing update-stream proof is incomplete. |
| REQ-024 | Partial | streaming integration, `t20` | WebSocket payloads are proven, but the associated browser page behavior is not. |
| REQ-025 | Partial | implementation mapping only | No runtime proof found for the primitive JAX-RS echo endpoints. |
| REQ-026 | Partial | `t15`, `t20` | Representative primitive reachability is proven; the launcher page and every linked primitive surface are not. |
| REQ-027 | Partial | implementation mapping only | No runtime proof found for the reachable docs/informational page set. |
| REQ-028 | Partial | `t15` | Representative JSF reachability is proven; image-based JSP variants and the broader alternate-surface inventory are not. |
| REQ-029 | Partial | `t17.2`, `t19.1`, `t20` | Some success and auth-boundary status codes are proven, but full endpoint status/error semantics are not. |
| REQ-030 | Partial | `t17.2`, config access tests, `t20` | Some access-control boundaries are proven, but the full anonymous-access contract is not closed. |

## What Must Be True Before PM Sign-off Can Pass

1. Add explicit runtime evidence for the missing admin and scenario actions, not just controller wiring.
2. Prove the primitive launcher, JAX-RS echo endpoints, docs pages, image JSP variants, and broader alternate-surface inventory are still directly reachable.
3. Close the partial trading-surface gaps with focused tests for the remaining observable behaviors.
4. Execute or formally block the planned Docker-backed infrastructure lane with the exact attempted command and exact failure output.

## Final Product Verdict

The migration is not yet at feature-parity sign-off. The implementation looks viable, but the evidence bundle still supports only a representative-parity conclusion, not the full inventory-preserving conclusion required by the PM charter and constitution.