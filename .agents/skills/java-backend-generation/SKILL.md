---
name: java-backend-generation
description: Use when implementing or planning Java 21 Spring Boot 3 backend code for this ERP, especially domain models, use cases, ports, adapters, REST controllers, Flyway migrations, JWT/security changes, or backend tests.
---

# Java Backend Generation

Follow the project rules in `AGENTS.md` and the source descriptor at `.github/skills/java-backend-generation.yaml`.

When implementing backend work:

1. Read the relevant existing module before designing new code.
2. Keep domain code free of Spring/JPA/HTTP dependencies.
3. Put orchestration in explicit use cases and technical concerns in adapters.
4. Use records for immutable commands/responses when consistent with local code.
5. Use BigDecimal for money, UUID for public IDs, and transactions around critical use cases.
6. Add focused tests with the change; use Testcontainers for persistence/integration behavior when needed.

Reference policies when relevant:

- `.github/policies/architecture-policy.yaml`
- `.github/policies/security-policy.yaml`
- `.github/policies/testing-policy.yaml`
