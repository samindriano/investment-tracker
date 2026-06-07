# SahamLog

SahamLog is a personal finance tracker for Indonesian equities. The backend is a Spring Boot modular monolith with JWT auth, portfolio tracking, dividend tracking, watchlist valuation, thesis journaling, exports, and reporting endpoints. A lightweight Next.js frontend scaffold lives in `frontend/`.

## Why Modular Monolith First

- One developer can move faster than with multiple services and repos.
- Portfolio, dividend, watchlist, and thesis data are tightly coupled and do not need network boundaries yet.
- If the app grows, these package boundaries can later be extracted into services with less rework.

## Domain Modules

- `auth`
- `portfolio`
- `dividend`
- `watchlist`
- `journal`
- `reporting`
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

## Main API Areas

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `GET|POST|PUT|DELETE /api/v1/stocks`
- `GET|POST|PUT|DELETE /api/v1/transactions`
- `GET|PUT /api/v1/prices`
- `GET /api/v1/dashboard/summary`
- `GET|POST|PUT|DELETE /api/v1/dividends`
- `GET|POST|PUT|DELETE /api/v1/watchlist`
- `GET|POST|PUT|DELETE /api/v1/theses`
- `GET /api/v1/reports/*`
- `GET /api-docs`
- `GET /swagger-ui.html`

## Frontend Scaffold

The repo includes a separate frontend scaffold under `frontend/`.

Run it separately:

```powershell
cd frontend
npm install
npm run dev
```

Set `NEXT_PUBLIC_API_BASE_URL` based on the backend URL.

## Ops

- Docker local stack: `docker compose up --build`
- Swagger/OpenAPI enabled in non-production profiles
- GitHub Actions CI runs tests on push and PR
