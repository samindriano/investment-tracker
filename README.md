# SahamLog

SahamLog is a personal finance tracker for Indonesian equities. This base project starts as a Spring Boot modular monolith so the business logic stays in one codebase while each domain keeps a clean boundary.

## Why Modular Monolith First

- One developer can move faster than with multiple services and repos.
- Portfolio, dividend, watchlist, and thesis data are tightly coupled and do not need network boundaries yet.
- If the app grows, these package boundaries can later be extracted into services with less rework.

## Initial Domain Modules

- `auth`
- `portfolio`
- `dividend`
- `watchlist`
- `journal`
- `system`
- `config`

## Tech Baseline

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security
- Flyway
- PostgreSQL
- Gradle Wrapper

## Run Locally

Set these environment variables if you do not use the defaults:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Run:

```powershell
.\gradlew.bat bootRun
```

Health checks:

- `GET /actuator/health`
- `GET /api/v1/system/ping`

## Suggested Next Steps

1. Create the `stock` master-data module.
2. Add `transaction_entry` entity, repository, service, and API.
3. Implement holdings and average-price calculation from transactions.
4. Add basic auth user flow instead of relying on default Spring Security credentials.
