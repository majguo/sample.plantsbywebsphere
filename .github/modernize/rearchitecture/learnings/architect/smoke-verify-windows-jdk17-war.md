# Smoke Verify Windows Jdk17 War

For this repo on Windows, deterministic smoke verification is `cmd /d /c` for the Maven build plus `java -jar target\io.openliberty.sample.daytrader8.war --server.port=<alt>` under JDK 17.

## What Happened
During sample.daytrader8 t19, a PowerShell-launched `mvn.cmd -DskipTests clean package` attempt fell into `Terminate batch job (Y/N)?`, which made the smoke result noisy even though the project itself was buildable. Re-running the build through `cmd /d /c` with `JAVA_HOME` set to the Microsoft JDK 17 installation produced a clean `BUILD SUCCESS`. The packaged WAR then started successfully on an alternate port and was verified with both a TCP socket probe and an HTTP `200` on `/daytrader/`.

## Takeaway
On this repo, use JDK 17 explicitly for all Spring Boot 3 smoke checks. For Windows build evidence, prefer `cmd /d /c` over direct `mvn.cmd` from PowerShell so the result is not polluted by the batch termination prompt. If a rerun still enters the test phase under `-DskipTests`, use `-Dmaven.test.skip=true` for the architect-owned package smoke path. For startup evidence, verify both the Tomcat bind and an HTTP response on the `/daytrader` context path.

## History
- 2026-05-28 (sample.daytrader8/t19): initial
- 2026-05-28 (sample.daytrader8/t19.1): recorded that `-Dmaven.test.skip=true` is the reliable fallback when a Windows rerun with `-DskipTests` does not stay on the skip-tests path