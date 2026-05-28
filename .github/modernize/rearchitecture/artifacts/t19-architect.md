# t19 - Smoke test: build, startup, and port verification

## Summary

Independent architect smoke verification passed for the Spring Boot 3 rewrite under JDK 17. The full root Maven package build completed successfully, the repackaged WAR started on an alternate port, Tomcat bound to the expected HTTP socket, and the `/daytrader/` surface returned `200`.

## Verdict

- Build: PASS
- Startup: PASS
- Port verification: PASS
- HTTP verification: PASS
- Overall: PASS with one non-blocking environment compatibility warning

## Evidence

### Full Build

- Command: `cmd /d /c "set JAVA_HOME=%USERPROFILE%\scoop\apps\microsoft17-jdk\current&& set PATH=%JAVA_HOME%\bin;%PATH%&& mvn -DskipTests clean package"`
- Exit code: `0`
- Plugin failures: none
- Result: `BUILD SUCCESS`
- Duration: `34.809 s`
- Output notes:
  - Packaging type is `war` under Spring Boot `3.2.6`.
  - The Spring Boot Maven plugin repackaged `target\io.openliberty.sample.daytrader8.war` successfully.
  - The build still emits legacy-source warnings for deprecated boxed primitive constructors and value-based synchronization, but they do not fail the smoke build.

### Startup

- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19086`
- Process result: application reached ready state and remained running until intentionally terminated after verification
- Bound port: `19086`
- Context path: `/daytrader`
- Startup time: `18.868 s` (`20.358 s` process runtime at ready log line)
- Runtime notes:
  - Flyway validated 3 migrations and reported schema version `2` up to date.
  - Tomcat reported `Tomcat started on port 19086 (http) with context path '/daytrader'`.

### Port and HTTP Probe

- Command: `$tcp = Test-NetConnection -ComputerName 127.0.0.1 -Port 19086 -WarningAction SilentlyContinue; $response = Invoke-WebRequest -Uri 'http://127.0.0.1:19086/daytrader/' -UseBasicParsing; ...`
- TCP result: `TcpTestSucceeded=True`
- HTTP status: `200`

## Issues Found

- WARNING: Startup logs `HHH000511` because the repo is still running Derby `10.14`, while Hibernate `6.4.8.Final` warns that its minimum supported Derby version is `10.15.2`. Smoke behavior is green, but version compatibility risk remains for later conformance/release decisions.
- WARNING: On Windows PowerShell, direct `mvn.cmd` smoke invocation can fall into `Terminate batch job (Y/N)?` during interrupted runs. The deterministic smoke path for this repo is `cmd /d /c` with JDK 17 exported first.

## Test Results

- Command: `cmd /d /c "set JAVA_HOME=%USERPROFILE%\scoop\apps\microsoft17-jdk\current&& set PATH=%JAVA_HOME%\bin;%PATH%&& mvn -DskipTests clean package"`
- Command: `$env:JAVA_HOME = "$env:USERPROFILE\scoop\apps\microsoft17-jdk\current"; $env:Path = "$env:JAVA_HOME\bin;" + $env:Path; java -jar target\io.openliberty.sample.daytrader8.war --server.port=19086`
- Command: `Test-NetConnection 127.0.0.1:19086` plus `Invoke-WebRequest http://127.0.0.1:19086/daytrader/`
- Passed: 3
- Failed: 0
- Skipped: 0
