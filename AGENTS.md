# AGENTS.md

## Repository Expectations

- This project is a modular monolith ERP for clothing retail using Java 21, Spring Boot 3, Maven, Angular, PostgreSQL, Docker, Flyway, JWT security, and automated tests.
- Prefer Clean Architecture / Hexagonal Architecture per module: domain code must not depend on Spring, JPA, HTTP, or database details.
- Keep business rules in domain/application use cases. Controllers validate/request-map and delegate; adapters handle technical details.
- Preserve module boundaries and existing patterns before adding new abstractions or dependencies.
- Treat `.github/` as the versioned project knowledge base for GitHub and Codex reference material:
  - specs and templates: `.github/specs/`
  - policies: `.github/policies/`
  - architecture docs and workflow docs: `.github/docs/`
  - examples: `.github/examples/`
  - original agent/tool descriptors: `.github/agents/` and `.github/tools/`
- Keep `.github/workflows/`, `.github/ISSUE_TEMPLATE/`, and `.github/PULL_REQUEST_TEMPLATE.md` GitHub-native. Do not move them into Codex configuration.

## Development Rules

- Use Java 21, Spring Boot 3.x, Maven multi-module conventions, and explicit use cases/ports/adapters.
- Use records for immutable commands/responses when appropriate; use expressive domain classes for invariants.
- Use BigDecimal for money and UUIDs for public identifiers unless existing local code says otherwise.
- Use Flyway for schema changes and constraints/indexes for database integrity.
- Frontend work should use TypeScript strict, Angular reactive forms, guards, interceptors, and HTTP services/adapters.
- Do not put critical business, authorization, or validation rules only in the frontend.

## Security

- Never trust client input. Authorization must be enforced in the backend.
- Protect administrative endpoints with appropriate roles.
- Keep secrets out of code; use environment variables or secret managers.
- Do not log credentials, tokens, or sensitive customer data.
- Keep CORS restricted and evaluate CSRF/session implications when changing security behavior.
- When touching auth/JWT/RBAC, add or update regression tests.

## Testing And Verification

- Add tests with code changes. Domain rules need unit tests; persistence and critical API behavior need integration/facade tests where practical.
- Bugs fixed should become regression tests.
- Strong invariants should use property-based tests when useful.
- Useful commands:
  - Backend/module tests: `./mvnw test`
  - Integration slices when Docker/Testcontainers are needed: `./mvnw -pl infrastructure,bootstrap -Dtest=<TestName> -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false clean test`
  - Frontend tests: run the package scripts from `frontend/package.json`.
- If a verification command cannot run because Docker, network, or permissions are unavailable, say so explicitly and report the partial checks that did run.

## Review And Change Safety

- Do not remove failing tests just to make a build pass.
- Do not introduce production dependencies without a clear reason.
- Do not alter public API contracts without calling out the breaking change and updating docs/specs.
- Ignore unrelated dirty worktree changes unless they block the requested task.
