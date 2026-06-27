# ADR-0001 — Escolha do Estilo Arquitetural

## Status

Aceito

## Contexto

O sistema precisa ser modular, testável, evolutivo e simples de operar.

## Decisão

Usaremos Monólito Modular com Arquitetura Hexagonal por módulo.

## Justificativa

- Menor complexidade operacional.
- Boa separação de domínio.
- Deploy simples.
- Evolução futura para microsserviços se houver necessidade real.

## Consequências Positivas

- Menos infraestrutura.
- Testes mais simples.
- Menor custo inicial.
- Melhor rastreabilidade.

## Consequências Negativas

- Deploy único.
- Exige disciplina modular.
- Limites precisam ser protegidos por arquitetura e testes.
