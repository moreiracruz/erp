---
name: clean-architecture-design
description: Use when designing or reviewing modules, layers, ports, adapters, dependency direction, domain boundaries, or architecture decisions for this ERP.
---

# Clean Architecture Design

Follow `AGENTS.md`, `.github/skills/clean-architecture-design.yaml`, and `.github/policies/architecture-policy.yaml`.

Architecture rules:

1. Domain contains entities, value objects, domain services, and domain events.
2. Application contains use cases, ports, commands, and queries.
3. Infrastructure contains persistence, messaging, security, and external clients.
4. Web/interface adapters contain controllers and request/response mapping.
5. Dependencies point inward; do not couple domain to Spring, JPA, HTTP, or database APIs.
6. Add ADRs in `.github/docs/adr/` for meaningful architectural decisions.
