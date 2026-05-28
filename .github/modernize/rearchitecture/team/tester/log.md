## [t16] Boot-context tester harness for MVC and streaming surfaces
- Built tester-owned Boot integration suites for MVC/session/admin/REST journeys and live-port SSE/WebSocket checks.
- Needed explicit `DayTraderApplication` binding because the tests live outside the application package tree.
- Needed scheduled publisher/worker beans mocked at context creation; `@BeforeEach` stubbing was too late because scheduled methods can fire during startup.
- The streaming suite exposed a backend bug in `StreamingHub.onQuotePriceChange`: after an SSE client disconnects, `completeWithError(...)` can raise `IllegalStateException` on a dead async context.
- Learnings consumed: [teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, backend/mvc-app-session-compatibility, backend/boot-streaming-and-order-work, backend/boot-mvc-operator-surfaces]

## [t16] Browser harness closure and readiness evidence
- Added the missing dedicated Playwright workspace and kept it anchored to the preserved `/daytrader` context path via config plus relative route navigation.
- First browser run failed because leading-slash routes escaped the base path; switching the spec to `welcome.jsp`, `config`, and `servlet/PingServlet` fixed the harness without changing application code.
- Generated a tester-owned readiness summary from surefire plus Playwright outputs in `target/readiness-evidence.md`.
- Docker remained unavailable, so the harness stays on the documented local-Derby fallback until downstream runtime validation.
- Learnings consumed: [tester/boot-harness-mocks-scheduled-publishers, teamlead/mixed-surface-validation-stack, teamlead/readiness-evidence-policy, backend/mvc-app-session-compatibility, backend/boot-streaming-and-order-work, backend/boot-mvc-operator-surfaces]

## [t20] Mixed-surface runtime validation on packaged Boot WAR
- Re-ran the focused JDK 17 Boot journey and streaming suites and expanded the Playwright parity lane to cover registration, buy/sell, account validation, and config update/restore on the preserved `/daytrader` routes.
- First browser rerun failed because `DAYTRADER_BASE_URL` without a trailing slash caused relative Playwright navigation to escape the app context; normalizing the configured base URL fixed the harness.
- Registration parity initially failed because the generated email mirrored a `uid:*` identifier and violated the migrated bean-validation email constraint; sanitizing the generated email restored the browser journey without touching production code.
- A follow-up JVM rerun failed once because the packaged WAR still held the embedded Derby lock; killing the runtime and rerunning the suites produced the final green evidence.
- Learnings consumed: [tester/boot-harness-mocks-scheduled-publishers, tester/playwright-baseurl-and-preserved-routes]

## [t20.1] Container-backed primary infra lane blocker verification
- Confirmed Docker daemon availability, then checked the nearest in-repo DB2/container path and the migrated Boot runtime instead of assuming the missing lane was only an execution oversight.
- The repo does not contain the `db2jars/` assets expected by `Dockerfile-db2`, and the packaged Spring Boot WAR fails immediately under DB2 datasource settings with `Cannot load driver class: com.ibm.db2.jcc.DB2Driver`.
- Existing Boot integration suites remain unsuitable as real-infra substitutes because the shared tester support class mocks the trade facade, DB utility, scheduled worker, and streaming publisher.
- Learnings consumed: [tester/boot-harness-mocks-scheduled-publishers, tester/generated-registration-data-must-pass-bean-validation, tester/playwright-baseurl-and-preserved-routes]

## [t20.4] DB2 primary lane rerun isolates bootstrap-data parity blocker
- Confirmed the repo-supported DB2 lane now starts reproducibly and the packaged Boot WAR reaches `Started DayTraderApplication` on the DB2 datasource under JDK 17.
- The remaining blocker is not container startup: fresh DB2 startup is schema-only, `uid:0 / xxx` login fails, `GET /rest/quotes/s:1` returns `[null]`, and the public `configure.html` utility links to `config?action=buildDB` even though that action returns `401 Unauthorized` when invoked unauthenticated.
- Registration still lands on the welcome surface, so the failure is specifically the missing seeded benchmark dataset plus a circular bootstrap auth boundary on the real-infra lane.
- Learnings consumed: [tester/generated-registration-data-must-pass-bean-validation, tester/playwright-baseurl-and-preserved-routes, tester/primary-infra-lane-needs-boot-db2-wiring]

## [t20.5] DB2 primary lane rerun closes the real-infra gap
- Rebuilt the packaged WAR under JDK 17, reset the repo-local DB2 container lane, and reran the authoritative Boot plus Playwright validation stack on `http://127.0.0.1:19095/daytrader`.
- Canonical `uid:0 / xxx` login and `GET /rest/quotes/s:1` are restored on fresh DB2 infra, and the full Playwright browser suite now passes on the primary lane.
- Anonymous `configure.html` remains reachable while anonymous `/config` and `/config?action=buildDB` return `401` again after seeding, which matches the intended narrow bootstrap exception.
- Learnings consumed: [tester/db2-primary-lane-bootstrap-needs-public-seed-path, tester/playwright-baseurl-and-preserved-routes, tester/primary-infra-lane-needs-boot-db2-wiring, teamlead/mixed-surface-validation-stack, teamlead/primary-validation-stack-must-be-executed-or-explicitly-blocked, teamlead/readiness-evidence-policy, backend/db2-bootstrap-only-public-before-seeding]

## [t21.2] Targeted runtime evidence turns PM gaps into concrete failures
- A fresh DB2 reset still needs the preserved bootstrap seed path before operator-driven checks are meaningful; once the lane was seeded, canonical `uid:0 / xxx` login succeeded again on the same `19097` runtime.
- Direct primitive/docs/alternate reachability is mostly intact on the live Boot WAR, but the primitive JAX-RS echo endpoints (`/jaxrs/sync/echoText`, `/echoJSON`, `/echoXML`) all return `404` on the seeded runtime.
- The scenario driver remains broken even on the seeded lane: login, register, home, account, update, quote, portfolio, buy, and sell all resolve back to the login surface instead of the expected trading pages.
- Operator utilities are inconsistent: `buildDB` streams the expected bootstrap output, `resetTrade` reports success on the wrong surface, and `buildDBTables` returns `401` after canonical operator login.
- Learnings consumed: [tester/db2-primary-lane-bootstrap-needs-public-seed-path, tester/playwright-baseurl-and-preserved-routes, teamlead/mixed-surface-validation-stack, teamlead/primary-validation-stack-must-be-executed-or-explicitly-blocked, teamlead/readiness-evidence-policy]

## [t21.2.2] Seeded DB2 parity rerun after backend repairs
- Rebuilt the WAR under JDK 17, reset the repo-local DB2 lane, launched the packaged runtime on `19099`, and reran the exact primitive JAX-RS, operator, and scenario probes that were failing in `t21.2`.
- The first scenario rerun was a false lead because `resetTrade` was being exercised in parallel; rerunning the scenario suite serially on the seeded lane showed the expected preserved titles for login, home, account, update, quote, portfolio, buy, sell, logout, and register.
- The repaired seeded-runtime slice is now green: canonical login and `rest/quotes/s:1` still work, all three `/jaxrs/sync/*` echo endpoints return `200`, `resetTrade` renders run-stats with the success banner, and `buildDBTables` accepts the operator session with `200`.
- Learnings consumed: [tester/db2-primary-lane-bootstrap-needs-public-seed-path, tester/playwright-baseurl-and-preserved-routes, tester/primary-infra-lane-needs-boot-db2-wiring]

## [t21.4] Inventory-complete docs, alternate, primitive, and image proof on seeded DB2 lane
- Rebuilt the packaged WAR under JDK 17, reset the repo-local DB2 lane, launched the live runtime on `19101`, and executed the remaining surface inventory with the checkpoint script in `.github/modernize/rearchitecture/artifacts/checkpoints/t21.4-runtime-proof.ps1`.
- The direct JSF/XHTML alternate inventory is intact on the seeded lane, and image mode mostly works after `WebInterface=1`, but the remaining proof slice exposed three real runtime gaps: `docs/tradeTech.pdf` and `docs/tradeUML.pdf` both return `404`, thirty primitive launcher destinations still return `404`, and image-mode `app?action=home` fails with `500` through nested JSP include/resource handling in `tradehomeImg.jsp`.
- The seeded lane now treats anonymous `config?action=buildDB` as a post-seed `401` again after reset helper startup, so that probe is useful only as an informational seeded-lane check, not as a parity failure by itself.
- Learnings consumed: [tester/db2-primary-lane-bootstrap-needs-public-seed-path, tester/playwright-baseurl-and-preserved-routes, tester/primary-infra-lane-needs-boot-db2-wiring, tester/real-infra-mutations-must-run-serially]

## [t21.4.2] Re-run docs, primitive, and image proof after backend repairs
- Rebuilt the packaged WAR under JDK 17, reset the repo-local DB2 lane, relaunched the live runtime on `19101`, and reran the exact proof script from `t21.4` without modifying the harness.
- The repaired surface slice is fully green on real DB2 infra: docs now return `200` including both PDF assets, all 43 primitive launcher routes return `200`, and image-mode actions including `app?action=home` render successfully after the config switch.
- No new runtime gaps surfaced in this scope; the checkpoint script returned `74` passed, `0` failed, and an empty failure list.
- Learnings consumed: [tester/db2-primary-lane-bootstrap-needs-public-seed-path, tester/playwright-baseurl-and-preserved-routes, tester/primary-infra-lane-needs-boot-db2-wiring, tester/real-infra-mutations-must-run-serially]

