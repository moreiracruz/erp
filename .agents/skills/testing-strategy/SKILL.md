---
name: testing-strategy
description: Use when designing, adding, or reviewing test coverage for domain rules, persistence, APIs, regressions, RBAC/JWT security, property-based invariants, or CI quality gates.
---

# Testing Strategy

Follow `AGENTS.md`, `.github/skills/testing-strategy.yaml`, and `.github/policies/testing-policy.yaml`.

Choose tests by risk:

1. Domain invariants: unit tests with JUnit 5, AssertJ, and property-based tests when useful.
2. Persistence and migrations: integration tests with Spring Boot Test and Testcontainers.
3. Critical API/security flows: MockMvc/facade tests.
4. Bug fixes: add regression tests that fail before the fix when practical.

Do not add low-value getter/setter-only tests.
