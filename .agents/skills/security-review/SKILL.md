---
name: security-review
description: Use when reviewing or changing authentication, authorization, JWT filters, RBAC, endpoint exposure, CORS/CSRF, secrets, logging, input validation, dependency vulnerabilities, or OWASP risks.
---

# Security Review

Follow `AGENTS.md`, `.github/skills/security-review.yaml`, and `.github/policies/security-policy.yaml`.

Security review checklist:

1. Confirm authentication and authorization are enforced in the backend.
2. Check protected endpoints, administrative roles, and method-level security.
3. Validate JWT/token expiry, tampering behavior, and error responses.
4. Ensure logs and responses do not expose secrets, tokens, stack traces, or sensitive data.
5. Review CORS, CSRF/session assumptions, input validation, dependency risk, and residual risks.
6. Add or update security regression tests for fixed issues.
