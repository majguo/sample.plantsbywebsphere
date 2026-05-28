# Generated registration data must pass bean validation

When browser tests create DayTrader users dynamically, generated field values still have to satisfy the migrated JPA bean-validation rules.

## What Happened
In sample.daytrader8 task t20, a Playwright registration journey generated a unique user ID like `uid:pw-...` and reused it directly in the email local-part. The migrated Spring Boot path validates `AccountProfileDataBean.email` with `@Email`, so the colon-containing address failed at transaction commit and the browser landed on the error surface instead of the home page.

## Takeaway
Keep the generated user ID free to preserve legacy compatibility, but sanitize any derived email or other validated fields separately before submitting them through browser parity tests.

## History
- 2026-05-28 (sample.daytrader8/t20): initial