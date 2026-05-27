# Test Coverage

## Current Automated Coverage Posture

- No Java test sources were found under `src/test`
- Maven build can compile and package successfully
- Surefire runs with no test classes and skipped test execution in the smoke build
- JMeter scripts exist for benchmark and load scenarios, including WebSocket flows

## What Exists

- Load and benchmark assets under `jmeter_files/`
- Load-test setup guidance in `README_LOAD_TEST.md`
- Primitive pages and endpoints that can act as manual/runtime probes for container features

## What Is Missing

- Unit tests for business services
- Integration tests for servlet, JSF, REST, and WebSocket contracts
- Contract tests for order state transitions and messaging behavior
- Automated smoke tests for startup and key parity paths

## Migration Impact

The existing validation posture is not sufficient for a Spring Boot 3 rewrite with strict parity.
Performance assets are useful later, but they do not replace characterization tests. Downstream
planning should treat missing behavioral automation as a first-class migration risk, especially for
authentication/session behavior, order-processing modes, REST payloads, and WebSocket updates.