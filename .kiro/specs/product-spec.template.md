# Product Specification

## 1. Contexto

Descreva o problema, público-alvo e objetivo do sistema.

## 2. Objetivos

- Objetivo 1
- Objetivo 2
- Objetivo 3

## 3. Fora de Escopo

- Item não incluído
- Item não incluído

## 4. Atores

| Ator | Descrição |
|---|---|
| Admin | Gerencia o sistema |
| Usuário | Usa funcionalidades principais |

## 5. Casos de Uso

### UC-001 — Nome do Caso de Uso

**Como** ator  
**Quero** ação  
**Para** benefício

#### Fluxo Principal

1. Passo 1
2. Passo 2
3. Passo 3

#### Fluxos Alternativos

- A1:
- A2:

#### Regras de Negócio

- BR-001:
- BR-002:

#### Critérios de Aceite

```gherkin
Scenario: Executar caso com sucesso
  Given uma condição inicial
  When uma ação acontece
  Then o resultado esperado deve ocorrer
```

## 6. Requisitos Não Funcionais

| ID | Requisito | Critério |
|---|---|---|
| NFR-001 | Performance | p95 < 300ms |
| NFR-002 | Segurança | RBAC obrigatório |
| NFR-003 | Observabilidade | Logs estruturados |

## 7. Restrições Técnicas

- Java 21
- Spring Boot 3
- Angular
- PostgreSQL
- Docker
- Maven
