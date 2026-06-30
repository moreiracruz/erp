---
name: database-design
description: Use when designing or changing relational schema, Flyway migrations, JPA persistence mappings, constraints, indexes, transactional consistency, or database tests.
---

# Database Design

Follow `AGENTS.md` and `.github/skills/database-design.yaml`.

When changing persistence:

1. Use Flyway migrations for schema changes.
2. Give every table, constraint, and index a clear purpose.
3. Prefer database constraints for integrity that must always hold.
4. Tie indexes to known query patterns.
5. Consider concurrency on stock, sales, payment, coupon, and financial flows.
6. Add integration tests with Testcontainers for behavior that depends on the database.
