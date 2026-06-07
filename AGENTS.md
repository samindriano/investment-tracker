# AGENTS.md

This file is the repo-local guide for future Codex or GPT-assisted work on SahamLog. Read this file before making architecture or code changes.

## Project Intent

SahamLog is a personal finance web app for Indonesian equities. The primary goal is a usable personal product first, with room to grow into analytics and AI-oriented features later.

Core product areas:

- portfolio tracking
- dividend tracking
- watchlist valuation
- investment thesis journal

## Architecture Decision

Use a Spring Boot modular monolith, not microservices.

Reasons:

- one developer project
- tightly related domains with shared data
- lower operational and debugging overhead
- easier AI-assisted iteration
- still possible to split into services later if boundaries stay clean

Do not introduce microservices, message brokers, API gateways, or multiple repos unless there is a concrete scaling or team boundary reason.

## Current Tech Baseline

- Java 21
- Spring Boot 3.5
- Gradle Wrapper
- Spring Web
- Spring Data JPA
- Spring Security
- Flyway
- PostgreSQL

## Domain Modules

Keep package structure domain-first. Current and planned module boundaries:

- `config`
- `system`
- `auth`
- `portfolio`
- `dividend`
- `watchlist`
- `journal`
- `shared`

Inside each domain, prefer this structure when needed:

- `api`
- `service`
- `domain`
- `repository`
- `dto`

Do not organize the whole codebase primarily by technical layer across all domains.

## Data and Business Rules

- `transaction_entry` is the source of truth for buy and sell activity.
- Holdings should be derived from transactions, not stored as a permanent table in the early versions.
- Flyway migrations are the source of truth for schema evolution.
- Do not rely on Hibernate auto-DDL in non-test environments.
- Prefer explicit and reproducible financial calculations over clever abstractions.

## Coding Rules

- Keep the app API-first even if frontend decisions are postponed.
- Add features incrementally by domain.
- Write tests for business calculations and endpoint basics.
- Avoid introducing unnecessary framework complexity.
- Prefer clear, boring code over highly generic code.
- Keep security simple at first, but do not leave accidental open write endpoints.

## Implemented Milestones

Implemented:

1. local JWT auth
2. stock master data CRUD
3. transaction create/list/update/delete flow
4. holdings and average price calculation
5. unrealized gain/loss dashboard summary
6. dividend tracking
7. watchlist valuation
8. investment thesis journal
9. CSV exports and report endpoints
10. frontend scaffold under `frontend/`

Recommended next build order from here:

1. tighten pagination and sorting defaults across all list endpoints
2. improve snapshot scheduling/report retention
3. wire the frontend scaffold to authenticated mutations
4. add richer charts and portfolio review UX
5. add import flows and more advanced analytics

## Environment Notes

- use `.\gradlew.bat` on Windows
- local DB config comes from `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`
- health endpoints:
  - `GET /actuator/health`
  - `GET /api/v1/system/ping`

## About Repo Memory

This file is a local project guide, not a guaranteed global memory system. Future agents should still read:

1. `AGENTS.md`
2. `README.md`
3. relevant code and migrations

When project decisions change, update this file so future sessions inherit the latest local context.
