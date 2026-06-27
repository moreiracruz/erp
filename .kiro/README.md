# Enterprise Agent System

Framework base enterprise para agentes + orquestrador capazes de gerar um sistema do zero a partir de uma especificação.

## Conteúdo

- `agents/`: agentes especializados.
- `skills/`: skills reutilizáveis.
- `specs/`: templates de especificação, casos de uso, ADR, API e testes.
- `tools/`: políticas de uso de ferramentas.
- `policies/`: regras globais de arquitetura, segurança e testes.
- `docs/`: fluxo do orquestrador e documentação arquitetural.
- `examples/`: exemplos de uso.

## Uso sugerido

1. Comece por `skills/enterprise-spec-driven-system-builder.yaml`.
2. Use `agents/system-orchestrator.yaml` como coordenador.
3. Preencha `specs/product-spec.template.md` com a necessidade de negócio.
4. Execute o workflow em `docs/orchestrator-workflow.yaml`.
5. Valide entregáveis contra `policies/`.

## Princípios

- Spec-driven development.
- Clean Architecture / Hexagonal Architecture.
- SOLID pragmático.
- Testes automatizados.
- Segurança por padrão.
- Observabilidade desde o início.
- Sem overengineering.
