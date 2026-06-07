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

## Near-Term Build Order

Build features in this order unless there is a strong reason to change it:

1. stock master data
2. transaction entry create/list flow
3. holdings and average price calculation
4. unrealized gain/loss dashboard summary
5. dividend tracking
6. watchlist valuation
7. investment thesis journal

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
