# ERP Loja de Roupas

Sistema ERP para loja de roupas, construído como um **modular monolith** em Java 21 com Spring Boot 3.4, seguindo arquitetura hexagonal (Ports & Adapters) e princípios de Domain-Driven Design.

## Visão Geral

O sistema gerencia o ciclo completo de uma loja de roupas:

- **Autenticação**: JWT com refresh token rotation, lockout após falhas, RBAC por role
- **Catálogo**: Produtos com variantes (tamanho, cor, SKU, código de barras)
- **Estoque**: Controle físico/reservado com invariantes formais de consistência
- **Vendas (PDV)**: Fluxo completo de venda com cálculo automático de total/troco
- **Precificação**: Campanhas de desconto e cupons com controle de concorrência
- **Clientes**: Cadastro com validação de CPF (LGPD-aware)
- **Financeiro**: Lançamentos automáticos via domain events (receitas/despesas)
- **Observabilidade**: Prometheus metrics, health checks, structured logging

---

## Arquitetura

```
┌─────────────────────────────────────────────────────┐
│                    Bootstrap                          │
│         (Spring Boot App + Flyway Migrations)        │
├─────────────────────────────────────────────────────┤
│                 Infrastructure                        │
│    (Security, JPA Adapters, Event Bus, Observability)│
├─────────┬─────────┬──────────┬──────────┬───────────┤
│  Auth   │ Product │Inventory │  Sales   │ Customer  │
│         │         │          │          │           │
│ Finance │ Pricing │          │          │           │
├─────────┴─────────┴──────────┴──────────┴───────────┤
│              Shared (Kernel, Events, Exceptions)     │
└─────────────────────────────────────────────────────┘
```

Cada módulo possui:
- `domain/model` — Aggregates e value objects
- `domain/port/in` — Use cases (inbound)
- `domain/port/out` — Repositories e ports externos (outbound)
- `application/usecase` — Implementação dos use cases
- `adapter/in/web` — REST controllers
- `adapter/out/persistence` — JPA entities e adapters

Módulos comunicam-se apenas via **shared-kernel ports** (interfaces), com adapters na camada de infraestrutura. Isso é validado por testes ArchUnit.

---

## Pré-requisitos

| Ferramenta | Versão Mínima |
|-----------|---------------|
| Java | 21 |
| Docker | 20+ |
| Docker Compose | 2.0+ |
| Maven | 3.9+ (ou use o wrapper `./mvnw`) |

---

## Configuração

### 1. Clonar o repositório

```bash
git clone https://github.com/moreiracruz/erp.git
cd erp
```

### 2. Criar o arquivo `.env`

```bash
cp .env.example .env
```

Edite o `.env` com valores seguros:

```dotenv
DB_PASSWORD=sua_senha_segura_aqui
JWT_SECRET=chave-com-pelo-menos-32-caracteres-para-hmac-sha256
GRAFANA_PASSWORD=admin123
INVENTORY_EXPIRY_TTL_MINUTES=30
```

> ⚠️ **Nunca commite o `.env` real.** O `.gitignore` já o bloqueia.

### 3. Variáveis de Ambiente

| Variável | Descrição | Default |
|----------|-----------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://localhost:5432/erp` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `erp_user` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | — (obrigatório) |
| `JWT_SECRET` | Chave HMAC-SHA256 (mín. 32 chars) | — (obrigatório) |
| `INVENTORY_EXPIRY_TTL_MINUTES` | TTL de reservas de estoque | `30` |

---

## Execução

### Com Docker Compose (recomendado)

```bash
# Sobe tudo: PostgreSQL + App + Prometheus + Grafana
docker compose up -d

# Verificar saúde da aplicação
curl http://localhost:8080/actuator/health
```

Serviços disponíveis:
- **API**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin / valor do `GRAFANA_PASSWORD`)

### Desenvolvimento Local

```bash
# 1. Subir apenas o PostgreSQL
docker compose up -d postgres

# 2. Rodar a aplicação via Maven
./mvnw spring-boot:run -pl bootstrap
```

A aplicação executa as migrações Flyway automaticamente na inicialização.

---

## Testes

O projeto segue uma estratégia de testes em camadas:

### Camadas de Teste

| Camada | Padrão de Nome | Docker | Spring Context | Tempo Alvo |
|--------|---------------|--------|----------------|------------|
| Unit + Property | `*Test.java` | Não | Não | < 30s |
| Integration | `*IT.java` | Sim | Parcial | < 2min |
| E2E | `*E2ETest.java` | Sim | Completo | < 3min |

### Comandos

```bash
# Testes unitários + property (rápido, sem Docker)
./mvnw test

# Profile fast-tests (idêntico, explícito)
./mvnw test -Pfast-tests

# Testes de integração + E2E (requer Docker)
./mvnw verify -Pintegration

# Build completo com todos os testes
./mvnw clean verify -Pintegration

# Rodar testes de um módulo específico
./mvnw test -pl modules/inventory

# Rodar um teste específico
./mvnw test -pl modules/inventory -Dtest=Property6_StockCounterInvariantTest
```

### Property-Based Testing (jqwik)

O projeto usa **jqwik** para testar invariantes do domínio com inputs aleatórios (1000 tentativas por propriedade). Propriedades verificadas incluem:

| # | Propriedade | Módulo |
|---|-------------|--------|
| P6 | Estoque nunca fica negativo após qualquer operação | Inventory |
| P7 | Reserve → Release é idempotente (round-trip) | Inventory |
| P8 | Withdrawal negativo é sempre rejeitado | Inventory |
| P9 | Total da venda = soma dos itens | Sales |
| P10 | Troco exato para pagamento em dinheiro | Sales |
| P11 | Desativação de produto cascateia para variantes | Product |
| P13 | Desconto combinado nunca excede total da venda | Pricing |
| P14 | Uso de cupom nunca excede max_usages | Pricing |
| P15 | Envelope de domain event é sempre estruturalmente válido | Infrastructure |
| P16 | Balanço financeiro = receitas - despesas | Finance |
| P17 | Validação algorítmica de CPF | Customer |

### Integration Tests

Os testes de integração usam **Testcontainers** (PostgreSQL 16) com container singleton compartilhado entre todos os testes para performance:

- **Concorrência**: Testes de stock reservation e coupon usage com 10-20 threads simultâneas
- **Idempotência**: Processamento de domain events duplicados
- **Segurança**: Lockout, refresh token rotation, RBAC matrix
- **Privacidade**: Nenhum dado pessoal em respostas de erro

---

## API — Autenticação

Todos os endpoints (exceto `/api/v1/auth/login`) requerem JWT:

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin@loja.com", "password": "senha123"}'

# Resposta: { "accessToken": "eyJ...", "refreshToken": "abc..." }

# Usar o token em chamadas subsequentes
curl http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer eyJ..."
```

### Roles e Permissões

| Role | Acesso |
|------|--------|
| `ROLE_MANAGER` | Todos os endpoints |
| `ROLE_CASHIER` | Vendas, produtos (leitura), clientes |
| `ROLE_STOCK` | Estoque (entrada, saída, consulta) |
| `ROLE_FINANCE` | Lançamentos financeiros, relatórios |

---

## API — Endpoints Principais

### Produtos
```
POST   /api/v1/products              — Cadastrar produto
GET    /api/v1/products/{uuid}       — Buscar produto
PUT    /api/v1/products/{uuid}       — Atualizar produto
DELETE /api/v1/products/{uuid}       — Desativar produto (cascade variantes)
```

### Estoque
```
POST   /api/v1/inventory/entry       — Entrada de estoque
POST   /api/v1/inventory/withdrawal  — Saída de estoque
GET    /api/v1/inventory/{varianteUuid} — Consultar saldo
```

### Vendas (PDV)
```
POST   /api/v1/sales                 — Abrir venda
POST   /api/v1/sales/{uuid}/items    — Adicionar item (por barcode)
POST   /api/v1/sales/{uuid}/finalize — Finalizar venda
POST   /api/v1/sales/{uuid}/cancel   — Cancelar venda
GET    /api/v1/sales/{uuid}          — Consultar venda
```

### Clientes
```
POST   /api/v1/customers             — Cadastrar cliente
GET    /api/v1/customers?search=...  — Buscar cliente
```

### Financeiro
```
GET    /api/v1/finance/cash-flow?start=2025-01-01&end=2025-01-31
POST   /api/v1/finance/expenses      — Registrar despesa manual
```

### Precificação
```
POST   /api/v1/pricing/campaigns     — Criar campanha de desconto
POST   /api/v1/pricing/coupons       — Criar cupom
```

---

## Banco de Dados

O schema é gerenciado por **Flyway** (migrações versionadas em `bootstrap/src/main/resources/db/migration/`):

| Migração | Tabelas |
|----------|---------|
| V1 | `usuarios`, `refresh_tokens` |
| V2 | `produtos`, `variantes` |
| V3 | `estoque_items`, `movimentos_estoque`, `reservas_estoque` |
| V4 | `vendas`, `itens_venda` |
| V5 | `campanhas`, `cupons` |
| V6 | `clientes` |
| V7 | `lancamentos_financeiros` |
| V8 | `domain_events` (outbox) |

As migrações são executadas automaticamente ao iniciar a aplicação.

---

## Observabilidade

- **Health Check**: `GET /actuator/health` (readiness: DB + diskSpace)
- **Métricas**: `GET /actuator/prometheus` (histogramas HTTP, contadores customizados)
- **Grafana**: Dashboards em http://localhost:3000
- **Logs**: JSON estruturado (logback-spring.xml)

---

## Estrutura do Projeto

```
erp/
├── bootstrap/          → Spring Boot app, Flyway migrations
├── infrastructure/     → Security, JPA, Event Bus, Adapters
├── modules/
│   ├── auth/           → Autenticação e autorização
│   ├── product/        → Catálogo de produtos
│   ├── inventory/      → Gestão de estoque
│   ├── sales/          → PDV (vendas)
│   ├── customer/       → Clientes
│   ├── finance/        → Lançamentos financeiros
│   └── pricing/        → Campanhas e cupons
├── shared/
│   ├── shared-kernel/  → AggregateRoot, DomainEvent, Ports
│   ├── shared-events/  → Event payloads
│   ├── shared-exceptions/ → Exceções de domínio
│   └── test-support/   → Utilitários de teste (builders, arbitraries)
├── docker-compose.yml
├── Dockerfile
└── pom.xml             → Parent POM
```

---

## Decisões Técnicas

- **Modular Monolith**: Módulos isolados com comunicação via ports — preparado para eventual extração em microserviços
- **Hexagonal Architecture**: Domínio não depende de frameworks; inversão de dependência via interfaces
- **Property-Based Testing**: Invariantes verificadas com inputs aleatórios (jqwik) — mais confiável que testes com exemplos fixos
- **Outbox Pattern**: Domain events persistidos em tabela `domain_events` antes de processamento — garante at-least-once delivery
- **Optimistic Locking**: Cupons e estoque usam versionamento para controle de concorrência
- **Module Boundary Enforcement**: ArchUnit valida em build que nenhum módulo depende lateralmente de outro

---

## CI/CD Pipeline

### GitHub Actions Workflows

| Workflow | Trigger | Descrição |
|----------|---------|-----------|
| `ci.yml` | Push/PR em `main`, `develop` | Compile → Unit tests → Integration tests → ArchUnit → Docker build |
| `pr-checks.yml` | Pull Requests | Lint de commits, size check, validação de migrations |
| `security.yml` | Toda segunda + push em `pom.xml` | OWASP dependency check, secret scanning |
| `release.yml` | Push de tag `v*.*.*` | Tests → Docker build+push → GitHub Release |

### Criando uma Release

```bash
# Tag + push dispara o workflow automaticamente
git tag v1.0.0
git push origin v1.0.0
```

Isso automaticamente:
1. Roda todos os testes (unit + integration + E2E)
2. Builda a imagem Docker multi-arch (amd64 + arm64)
3. Publica no Docker Hub (`moreiracruz/erp-loja-roupas:1.0.0`)
4. Publica no GitHub Container Registry (`ghcr.io/moreiracruz/erp:1.0.0`)
5. Cria uma GitHub Release com changelog categorizado

### Tags de Imagem Docker

| Tag | Descrição |
|-----|-----------|
| `latest` | Última release estável |
| `1.0.0` | Versão exata |
| `1.0` | Latest patch da minor |
| `1` | Latest minor da major |

### Secrets Necessários no GitHub

Configure em **Settings → Secrets and variables → Actions**:

| Secret | Descrição |
|--------|-----------|
| `DB_PASSWORD` | ✅ Configurado |
| `JWT_SECRET` | ✅ Configurado |
| `GRAFANA_PASSWORD` | ✅ Configurado |
| `DOCKERHUB_USERNAME` | ✅ Configurado |
| `DOCKERHUB_TOKEN` | ⚠️ Configurar manualmente (Docker Hub → Security → Access Token) |

---

## Licença

Paulo André Moreira Cruz — Todos os direitos reservados.
