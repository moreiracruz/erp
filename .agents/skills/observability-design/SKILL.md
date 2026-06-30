---
name: observability-design
description: Use when adding or reviewing structured logging, metrics, tracing, health checks, dashboards, alerts, or operational visibility for this ERP.
---

# Observability Design

Follow `AGENTS.md` and `.github/skills/observability-design.yaml`.

When designing observability:

1. Prefer structured JSON logs with correlation or trace IDs where available.
2. Include technical and business metrics only when they support useful operations.
3. Add health checks for critical dependencies.
4. Do not log sensitive data.
5. Avoid noisy alerts; define actionable signals.
