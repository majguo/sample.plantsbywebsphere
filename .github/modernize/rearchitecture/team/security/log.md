## [t18] Security audit of auth boundary, secret handling, and dependency posture
- The Boot migration preserved page behavior but never established one authoritative auth boundary for MVC admin, REST, SSE, and WebSocket traffic.
- `/config` is the highest-risk surface because it exposes runtime mutation plus reset/build actions without reusing the session gate present on `/app`.
- Secret handling is still legacy-grade: passwords and credit-card values remain plaintext in the entity model and leak into exception or trace paths.
- OWASP Dependency-Check was worth attempting, but the NVD API rate-limited the run with HTTP 429; the audit can cite the attempt but not a clean CVE result.
- Learnings consumed: [(none)]

## [t18.1] Re-run security audit after remediation
- The backend remediation closed the original auth-boundary and plaintext-secret findings with explicit MVC interceptors, WebSocket handshake checks, BCrypt hashing, and credit-card masking.
- Focused regression evidence is sufficient to clear the previous CRITICAL/HIGH findings, but dependency assurance still needs separate handling from behavioral security fixes.
- The remaining risk is dependency posture: Derby still resolves at 10.14.2.0, below Hibernate's supported minimum, and the OWASP rerun did not leave a usable report artifact.
- Treat a missing or empty dependency-check output as inconclusive, not as a clean scan.
- Learnings consumed: [security/auth-boundary-and-secret-exposure]