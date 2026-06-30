# Architecture Overview

Este repositório define um framework multiagente orientado por especificação.

## Estilo sugerido

- Monólito Modular como padrão inicial.
- Arquitetura Hexagonal por módulo.
- Domínio isolado de frameworks.
- Adapters para HTTP, banco, mensageria e integrações.

## Evolução

A evolução para microsserviços deve ocorrer apenas quando houver necessidade real de deploy, escala ou ownership independente.
