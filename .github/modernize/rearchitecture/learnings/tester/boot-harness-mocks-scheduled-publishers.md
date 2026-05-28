# Boot harness mocks scheduled publishers

When Boot integration tests target live MVC/SSE/WebSocket surfaces in DayTrader, mock the scheduled publisher beans at context creation so validation controls timing deterministically.

## What Happened
For t16 in sample.daytrader8, `@BeforeEach` stubbing was not enough because scheduled publisher methods could fire during application startup and hit unstubbed mocks. Replacing `MarketSummaryPublisher` and `OrderWorkProcessor` with test-time mocks made the Boot harness deterministic while keeping the runtime surface under test real.

## Takeaway
Tester-owned Boot harnesses for DayTrader should isolate scheduler timing by mocking scheduled driver beans, then exercise the externally visible MVC/SSE/WebSocket contracts through MockMvc or live-port clients.

## History
- 2026-05-28 (sample.daytrader8/t16): initial