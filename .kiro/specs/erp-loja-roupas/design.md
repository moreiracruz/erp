# Design Document — ERP Loja de Roupas

## Overview

This document describes the technical design of the ERP system for small and medium clothing retail stores. The system is implemented as a **Modular Monolith** following **Clean Architecture** and **Hexagonal Architecture (Ports & Adapters)** principles with **DDD tactical patterns**. The backend is Java 21 + Spring Boot 3, persisting to PostgreSQL, packaged as a Docker container, and designed to scale to AWS ECS Fargate + RDS.

Key drivers:
- Single deployable unit (monolith) with enforceable module boundaries to allow future extraction into microservices
- Eventual consistency between modules via in-process domain events with at-least-once delivery
- Accurate real-time inventory with pessimistic locking to prevent overselling
- JWT-based stateless authentication with short-lived tokens and revokable refresh tokens

---

## Architecture

### 1. Modular Monolith Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Spring Boot Application                        │
│  bootstrap module (main class, Spring context, global config)           │
│                                                                         │
│  ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌───────────┐  │
│  │  auth    │ │ product  │ │ inventory │ │  sales   │ │ customer  │  │
│  │ module   │ │ module   │ │  module   │ │  module  │ │  module   │  │
│  └──────────┘ └──────────┘ └───────────┘ └──────────┘ └───────────┘  │
│  ┌──────────┐ ┌────────────────────────────────────────────────────┐   │
│  │ finance  │ │                    pricing module                  │   │
│  │ module   │ └────────────────────────────────────────────────────┘   │
│  └──────────┘                                                          │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                        shared kernel                            │   │
│  │  events | exceptions | utils | value-objects | domain-base      │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      infrastructure                             │   │
│  │  persistence (JPA/Flyway) | security (JWT) | event-bus |        │   │
│  │  observability (OTel/Prometheus/Loki) | web (filters, CORS)     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
          │                                         │
    ┌─────▼──────┐                         ┌───────▼──────┐
    │ PostgreSQL │                         │  Redis (opt) │
    │  (primary) │                         │ (rate-limit) │
    └────────────┘                         └──────────────┘
```

### 2. Hexagonal Architecture per Module

Each business module follows the same internal layout:

```
br.com.moreiracruz.erp.modules.<module>
├── domain
│   ├── model          ← Entities, Aggregates, Value Objects
│   ├── port
│   │   ├── in         ← Use Case interfaces (inbound ports)
│   │   └── out        ← Repository / external-service interfaces (outbound ports)
│   └── service        ← Domain Services (pure business logic)
├── application
│   └── usecase        ← Use Case implementations (orchestration)
└── adapter
    ├── in
    │   └── web        ← REST Controllers, Request/Response DTOs, mappers
    └── out
        ├── persistence ← JPA Entities, Repositories (Spring Data), mappers
        └── event       ← Event publishers / consumers
```

Module boundaries are enforced by ArchUnit rules in CI: no module may import from another module's `domain` or `application` packages directly; all cross-module communication goes through published Use Case interfaces or in-process events.

---

## Module Structure and Package Layout

```
br.com.moreiracruz.erp
├── bootstrap                         ← Spring Boot main class, app config
├── infrastructure
│   ├── config                        ← SecurityConfig, JpaConfig, ObservabilityConfig
│   ├── persistence                   ← DataSource, FlywayConfig
│   ├── security
│   │   ├── JwtTokenProvider
│   │   ├── JwtAuthenticationFilter
│   │   └── RefreshTokenRepository
│   ├── eventbus
│   │   ├── InProcessEventBus
│   │   ├── EventEnvelope
│   │   └── DeadLetterStore
│   └── observability
│       ├── OtelConfig
│       └── MetricsConfig
├── shared
│   ├── kernel
│   │   ├── AggregateRoot
│   │   ├── DomainEvent
│   │   └── Identifiable
│   ├── events
│   │   ├── SaleCompletedEvent
│   │   ├── StockReservedEvent
│   │   └── PaymentApprovedEvent
│   ├── exceptions
│   │   ├── BusinessException
│   │   ├── ValidationException
│   │   └── NotFoundException
│   └── utils
│       ├── CpfValidator
│       └── MoneyUtils
└── modules
    ├── auth
    ├── product
    ├── inventory
    ├── sales
    ├── customer
    ├── finance
    └── pricing
```

---

## Components and Interfaces

The system is composed of seven business modules plus shared infrastructure. Each module exposes its functionality exclusively through inbound port interfaces (Use Cases). Cross-module calls at runtime go through Spring-injected port interfaces, never through direct instantiation of other modules' classes.

| Component | Responsibility | Inbound Ports | Outbound Ports |
|-----------|---------------|---------------|----------------|
| `auth` | Authentication, JWT issuance, refresh tokens, lockout | `LoginUseCase`, `RefreshTokenUseCase`, `LogoutUseCase` | `UsuarioRepository`, `RefreshTokenRepository` |
| `product` | Product and variant catalog management | `RegisterProductUseCase`, `RegisterVariantUseCase`, `SearchVariantUseCase`, `DeactivateProductUseCase` | `ProdutoRepository`, `VarianteRepository` |
| `inventory` | Stock counters, movements, reservations | `RegisterEntryUseCase`, `ReserveStockUseCase`, `ReleaseReserveUseCase`, `CommitReserveUseCase` | `EstoqueItemRepository`, `MovimentoEstoqueRepository`, `ReservaEstoqueRepository` |
| `sales` | POS flow, item addition, finalization, cancellation | `OpenSaleUseCase`, `AddItemUseCase`, `FinalizeSaleUseCase`, `CancelSaleUseCase` | `VendaRepository`, `InventoryPort`, `PricingPort` |
| `customer` | Customer registration, search, deactivation | `RegisterCustomerUseCase`, `SearchCustomerUseCase`, `DeactivateCustomerUseCase` | `ClienteRepository` |
| `finance` | Revenue/expense entries, cash flow reports | `RegisterExpenseUseCase`, `GetCashFlowReportUseCase` | `LancamentoRepository` |
| `pricing` | Campaigns, coupons, discount calculation | `CreateCampaignUseCase`, `CalculateDiscountUseCase`, `ConfirmCouponUsageUseCase` | `CampanhaRepository`, `CupomRepository` |

Cross-module interfaces (`InventoryPort`, `PricingPort`) are defined in `shared-kernel` and implemented by the respective module's adapter layer, preventing direct domain-layer coupling.

---

## Data Models

The canonical data models for each module are described below as Java records / classes. Full SQL schema is in the [Database Schema](#database-schema) section.

### Auth Data Models
```
TokenPair        { String accessToken, String refreshToken, long expiresIn }
Credentials      { String username, String password }
Role             { ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, ROLE_FINANCE }
```

### Product Data Models
```
ProdutoResponse  { UUID uuid, String name, String brand, String category, boolean active }
VarianteResponse { UUID uuid, UUID produtoUuid, String sku, String size, String color,
                   String barcode, BigDecimal price, BigDecimal cost, boolean active }
```

### Inventory Data Models
```
StockResponse    { UUID varianteUuid, int physicalStock, int reservedStock, int availableStock }
MovimentoResponse{ UUID uuid, String operationType, int quantity, Instant occurredAt, UUID actorUuid }
ReservaResponse  { UUID uuid, UUID varianteUuid, UUID saleUuid, int quantity, Instant expiresAt }
```

### Sales Data Models
```
VendaResponse    { UUID uuid, UUID operatorUuid, String terminalId, UUID clienteUuid?,
                   String status, String paymentMethod, BigDecimal subtotal,
                   BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal total,
                   BigDecimal changeAmount?, List<ItemVendaResponse> items, Instant createdAt }
ItemVendaResponse{ UUID varianteUuid, String sku, int quantity, BigDecimal unitPrice, BigDecimal lineTotal }
```

### Customer Data Models
```
ClienteResponse  { UUID uuid, String fullName, String cpf, String email?, String phone?,
                   LocalDate birthDate?, boolean active }
```

### Finance Data Models
```
LancamentoResponse { UUID uuid, String type, BigDecimal amount, String paymentMethod?,
                     String description, String category?, LocalDate competenceDate }
CashFlowReport   { LocalDate from, LocalDate to, BigDecimal totalReceita, BigDecimal totalDespesa,
                   BigDecimal netBalance, List<DailyEntry> dailyEntries }
DailyEntry       { LocalDate date, List<LancamentoResponse> receitas,
                   List<LancamentoResponse> despesas, BigDecimal dailyBalance }
```

### Pricing Data Models
```
DiscountResult   { BigDecimal discountAmount, BigDecimal cashbackAmount, List<AppliedRule> appliedRules }
AppliedRule      { String type, String code?, BigDecimal discount }
CampanhaResponse { UUID uuid, String name, String type, String targetType, UUID targetUuid?,
                   String targetCategory?, BigDecimal discountValue, BigDecimal cashbackPct?,
                   Instant startsAt, Instant endsAt, boolean active }
```

---

## Domain Model

### Auth Module

**Entities / Aggregates**

```
Usuario (Aggregate Root)
  id          : Long (BIGSERIAL)
  uuid        : UUID
  username    : String          ← email or login name
  passwordHash: String          ← bcrypt
  role        : Role            ← Value Object: enum {MANAGER, CASHIER, STOCK, FINANCE}
  active      : boolean
  failedAttempts : int
  lockedUntil : Instant?
  createdAt   : Instant

RefreshToken (Entity)
  id          : Long
  tokenHash   : String          ← SHA-256 of raw token
  usuarioUuid : UUID
  expiresAt   : Instant
  revokedAt   : Instant?
  createdAt   : Instant
```

**Value Objects**: `Role`, `Credentials(username, password)`, `TokenPair(jwt, refreshToken)`

---

### Product Module

**Entities / Aggregates**

```
Produto (Aggregate Root)
  id          : Long (BIGSERIAL)
  uuid        : UUID
  name        : String          ← 1–255 chars, unique active (case-insensitive)
  brand       : String          ← 1–100 chars
  category    : String          ← 1–100 chars
  active      : boolean

VarianteProduto (Entity, owned by Produto)
  id          : Long (BIGSERIAL)
  uuid        : UUID
  produtoId   : Long (FK → produto.id)
  sku         : String          ← 1–50 chars, unique across all
  size        : String          ← 1–50 chars
  color       : String          ← 1–50 chars
  barcode     : String          ← 8–14 digits, unique across all
  price       : BigDecimal      ← 0.01–999999.99
  cost        : BigDecimal      ← 0.01–999999.99
  active      : boolean
```

**Value Objects**: `Sku(value)`, `Barcode(value)`, `Money(amount, currency)`

---

### Inventory Module

**Entities / Aggregates**

```
EstoqueItem (Aggregate Root)
  id              : Long (BIGSERIAL)
  varianteUuid    : UUID          ← FK reference (not JPA join)
  physicalStock   : int           ← ≥ 0
  reservedStock   : int           ← ≥ 0
  version         : Long          ← optimistic lock version (backup)

MovimentoEstoque (Entity)
  id              : Long (BIGSERIAL)
  uuid            : UUID
  varianteUuid    : UUID
  operationType   : OperationType ← ENTRADA | SAÍDA | RESERVA | LIBERAÇÃO_RESERVA
  quantity        : int
  occurredAt      : Instant
  actorUuid       : UUID?         ← null/"SYSTEM" for automated ops
  referenceUuid   : UUID?         ← sale UUID for reservations

ReservaEstoque (Entity)
  id              : Long
  uuid            : UUID
  varianteUuid    : UUID
  saleUuid        : UUID
  quantity        : int
  status          : ReservaStatus ← ACTIVE | COMMITTED | RELEASED | EXPIRED
  createdAt       : Instant
  expiresAt       : Instant       ← createdAt + configurable timeout (default 30 min)
```

**Invariants**:
- `availableStock = physicalStock - reservedStock`
- `physicalStock ≥ 0`, `reservedStock ≥ 0`, `availableStock ≥ 0`

---

### Sales Module

**Entities / Aggregates**

```
Venda (Aggregate Root)
  id              : Long (BIGSERIAL)
  uuid            : UUID
  operatorUuid    : UUID
  terminalId      : String
  clienteUuid     : UUID?         ← optional
  status          : VendaStatus   ← EM_ANDAMENTO | FINALIZADA | CANCELADA
  paymentMethod   : PaymentMethod ← DINHEIRO | DEBITO | CREDITO | PIX
  subtotal        : BigDecimal
  discountAmount  : BigDecimal
  taxAmount       : BigDecimal
  total           : BigDecimal
  changeAmount    : BigDecimal?   ← troco, only for DINHEIRO
  couponCode      : String?
  cancellationReason : String?
  createdAt       : Instant
  finalizedAt     : Instant?

ItemVenda (Entity, owned by Venda)
  id              : Long
  vendaId         : Long (FK)
  varianteUuid    : UUID
  sku             : String
  quantity        : int
  unitPrice       : BigDecimal
  lineTotal       : BigDecimal
```

**Value Objects**: `PaymentMethod`, `VendaStatus`, `Money`

---

### Customer Module

**Entities / Aggregates**

```
Cliente (Aggregate Root)
  id          : Long (BIGSERIAL)
  uuid        : UUID
  fullName    : String          ← 1–255 chars
  cpf         : Cpf             ← Value Object: exactly 11 digits, validated
  email       : Email?          ← Value Object: RFC 5321 format
  phone       : String?         ← 8–15 digits
  birthDate   : LocalDate?
  active      : boolean
  createdAt   : Instant
```

**Value Objects**: `Cpf(value)` (validates check-digit algorithm), `Email(value)`

---

### Finance Module

**Entities / Aggregates**

```
LancamentoFinanceiro (Aggregate Root)
  id              : Long (BIGSERIAL)
  uuid            : UUID
  type            : EntryType     ← RECEITA | DESPESA
  amount          : BigDecimal    ← 0.01–999,999,999.99
  paymentMethod   : PaymentMethod? ← required for RECEITA
  description     : String        ← 1–255 chars
  category        : String?       ← for DESPESA
  competenceDate  : LocalDate
  responsibleUuid : UUID
  saleUuid        : UUID?         ← idempotency key for auto-entries
  createdAt       : Instant
```

**Predefined DESPESA categories**: ALUGUEL, SALARIOS, FORNECEDORES, MARKETING, MANUTENCAO, OUTROS

---

### Pricing Module

**Entities / Aggregates**

```
Campanha (Aggregate Root)
  id              : Long (BIGSERIAL)
  uuid            : UUID
  name            : String
  type            : CampaignType  ← PERCENTAGE | FIXED | PROGRESSIVE
  targetType      : TargetType    ← PRODUTO | CATEGORY | ALL
  targetUuid      : UUID?         ← Produto UUID if targetType=PRODUTO
  targetCategory  : String?       ← category string if targetType=CATEGORY
  discountValue   : BigDecimal    ← percentage 0.01–100 or fixed 0.01–999999.99
  minQuantity     : int?          ← ≥ 2 for PROGRESSIVE
  cashbackPct     : BigDecimal?   ← 0.01–50.00
  startsAt        : Instant
  endsAt          : Instant
  active          : boolean

Cupom (Aggregate Root)
  id              : Long
  uuid            : UUID
  code            : String        ← unique, case-insensitive
  type            : CampaignType
  discountValue   : BigDecimal
  startsAt        : Instant
  endsAt          : Instant
  maxUsages       : int
  usageCount      : int           ← atomic increment on confirmation
  active          : boolean
  version         : Long          ← optimistic lock
```

---

## Hexagonal Architecture per Module

### Inbound Ports (Use Case Interfaces)

#### Auth
```java
// br.com.moreiracruz.erp.modules.auth.domain.port.in
interface LoginUseCase        { TokenPair login(Credentials credentials); }
interface RefreshTokenUseCase { TokenPair refresh(String rawRefreshToken); }
interface LogoutUseCase       { void logout(String rawRefreshToken); }
```

#### Product
```java
interface RegisterProductUseCase    { ProdutoResponse register(RegisterProductCommand cmd); }
interface UpdateProductUseCase      { ProdutoResponse update(UUID uuid, UpdateProductCommand cmd); }
interface DeactivateProductUseCase  { void deactivate(UUID uuid); }
interface RegisterVariantUseCase    { VarianteResponse register(UUID produtoUuid, RegisterVariantCommand cmd); }
interface SearchVariantUseCase      { VarianteResponse findBySku(String sku); VarianteResponse findByBarcode(String barcode); }
```

#### Inventory
```java
interface RegisterEntryUseCase      { void registerEntry(StockEntryCommand cmd); }
interface RegisterWithdrawalUseCase { void registerWithdrawal(StockWithdrawalCommand cmd); }
interface ReserveStockUseCase       { ReservaResponse reserve(StockReserveCommand cmd); }
interface ReleaseReserveUseCase     { void release(UUID reservaUuid); }
interface CommitReserveUseCase      { void commit(UUID saleUuid); }
interface GetStockUseCase           { StockResponse getStock(UUID varianteUuid); }
```

#### Sales
```java
interface OpenSaleUseCase           { VendaResponse open(OpenSaleCommand cmd); }
interface AddItemUseCase            { VendaResponse addItem(UUID vendaUuid, AddItemCommand cmd); }
interface FinalizeSaleUseCase       { FinalizationResponse finalize(UUID vendaUuid, FinalizeSaleCommand cmd); }
interface CancelSaleUseCase         { void cancel(UUID vendaUuid, CancelSaleCommand cmd); }
```

#### Customer
```java
interface RegisterCustomerUseCase   { ClienteResponse register(RegisterCustomerCommand cmd); }
interface DeactivateCustomerUseCase { void deactivate(UUID uuid); }
interface SearchCustomerUseCase     { Page<ClienteResponse> search(CustomerSearchQuery query); }
```

#### Finance
```java
interface RegisterExpenseUseCase    { LancamentoResponse register(RegisterExpenseCommand cmd); }
interface GetCashFlowReportUseCase  { CashFlowReport getCashFlow(LocalDate from, LocalDate to); }
```

#### Pricing
```java
interface CreateCampaignUseCase     { CampanhaResponse create(CreateCampaignCommand cmd); }
interface CreateCouponUseCase       { CupomResponse create(CreateCouponCommand cmd); }
interface CalculateDiscountUseCase  { DiscountResult calculate(DiscountQuery query); }
interface ConfirmCouponUsageUseCase { void confirm(String couponCode); }
```

### Outbound Ports (Repository Interfaces)

Each module defines its own outbound ports in `domain.port.out`:

```java
// Inventory example
interface EstoqueItemRepository {
    Optional<EstoqueItem> findByVarianteUuid(UUID uuid);
    EstoqueItem findByVarianteUuidForUpdate(UUID uuid); // ← issues SELECT FOR UPDATE
    void save(EstoqueItem item);
}
interface MovimentoEstoqueRepository {
    void save(MovimentoEstoque movimento);
}
interface ReservaEstoqueRepository {
    Optional<ReservaEstoque> findBySaleUuidAndVarianteUuid(UUID saleUuid, UUID varianteUuid);
    List<ReservaEstoque> findExpiredActive(Instant before);
    void save(ReservaEstoque reserva);
}
```

---

## Database Schema

All migrations live in `src/main/resources/db/migration/` with naming `V{n}__{description}.sql`.

### usuarios
```sql
CREATE TABLE usuarios (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    username        VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL CHECK (role IN ('ROLE_MANAGER','ROLE_CASHIER','ROLE_STOCK','ROLE_FINANCE')),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    failed_attempts INT             NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

### refresh_tokens
```sql
CREATE TABLE refresh_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    token_hash      VARCHAR(64)     NOT NULL UNIQUE,   -- SHA-256 hex
    usuario_uuid    UUID            NOT NULL REFERENCES usuarios(uuid),
    expires_at      TIMESTAMPTZ     NOT NULL,
    revoked_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_usuario ON refresh_tokens(usuario_uuid);
```

### produtos
```sql
CREATE TABLE produtos (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    brand           VARCHAR(100)    NOT NULL,
    category        VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_produto_name_active UNIQUE (name)  -- enforced via partial index below
);
-- Case-insensitive uniqueness for active products
CREATE UNIQUE INDEX uq_active_produto_name ON produtos (LOWER(name)) WHERE active = TRUE;
```

### variantes
```sql
CREATE TABLE variantes (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    produto_id      BIGINT          NOT NULL REFERENCES produtos(id),
    sku             VARCHAR(50)     NOT NULL UNIQUE,
    size            VARCHAR(50)     NOT NULL,
    color           VARCHAR(50)     NOT NULL,
    barcode         VARCHAR(14)     NOT NULL UNIQUE,
    price           NUMERIC(10,2)   NOT NULL CHECK (price BETWEEN 0.01 AND 999999.99),
    cost            NUMERIC(10,2)   NOT NULL CHECK (cost BETWEEN 0.01 AND 999999.99),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_variantes_sku         ON variantes(sku);
CREATE INDEX idx_variantes_barcode     ON variantes(barcode);
CREATE INDEX idx_variantes_produto_id  ON variantes(produto_id);
```

### estoque_items
```sql
CREATE TABLE estoque_items (
    id              BIGSERIAL       PRIMARY KEY,
    variante_uuid   UUID            NOT NULL UNIQUE REFERENCES variantes(uuid),
    physical_stock  INT             NOT NULL DEFAULT 0 CHECK (physical_stock >= 0),
    reserved_stock  INT             NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0),
    version         BIGINT          NOT NULL DEFAULT 0,
    -- Enforce available = physical - reserved >= 0
    CONSTRAINT chk_available_non_negative CHECK (physical_stock - reserved_stock >= 0)
);
```

### movimentos_estoque
```sql
CREATE TABLE movimentos_estoque (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    variante_uuid   UUID            NOT NULL REFERENCES variantes(uuid),
    operation_type  VARCHAR(25)     NOT NULL CHECK (operation_type IN ('ENTRADA','SAÍDA','RESERVA','LIBERAÇÃO_RESERVA')),
    quantity        INT             NOT NULL CHECK (quantity > 0),
    occurred_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    actor_uuid      UUID,           -- NULL means SYSTEM
    reference_uuid  UUID            -- sale UUID for reservas
);
CREATE INDEX idx_movimentos_variante ON movimentos_estoque(variante_uuid);
```

### reservas_estoque
```sql
CREATE TABLE reservas_estoque (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    variante_uuid   UUID            NOT NULL REFERENCES variantes(uuid),
    sale_uuid       UUID            NOT NULL,
    quantity        INT             NOT NULL CHECK (quantity > 0),
    status          VARCHAR(15)     NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','COMMITTED','RELEASED','EXPIRED')),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ     NOT NULL
);
CREATE INDEX idx_reservas_sale_uuid    ON reservas_estoque(sale_uuid);
CREATE INDEX idx_reservas_expires_at   ON reservas_estoque(expires_at) WHERE status = 'ACTIVE';
```

### vendas
```sql
CREATE TABLE vendas (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    operator_uuid       UUID            NOT NULL,
    terminal_id         VARCHAR(50)     NOT NULL,
    cliente_uuid        UUID,
    status              VARCHAR(15)     NOT NULL DEFAULT 'EM_ANDAMENTO'
                            CHECK (status IN ('EM_ANDAMENTO','FINALIZADA','CANCELADA')),
    payment_method      VARCHAR(10)     CHECK (payment_method IN ('DINHEIRO','DEBITO','CREDITO','PIX')),
    subtotal            NUMERIC(12,2),
    discount_amount     NUMERIC(12,2)   DEFAULT 0,
    tax_amount          NUMERIC(12,2)   DEFAULT 0,
    total               NUMERIC(12,2),
    change_amount       NUMERIC(12,2),
    coupon_code         VARCHAR(50),
    cancellation_reason VARCHAR(255),
    data_venda          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),  -- indexed
    finalized_at        TIMESTAMPTZ
);
CREATE INDEX idx_vendas_data_venda  ON vendas(data_venda);
CREATE INDEX idx_vendas_cliente_id  ON vendas(cliente_uuid);
CREATE INDEX idx_vendas_operator    ON vendas(operator_uuid);
```

### itens_venda
```sql
CREATE TABLE itens_venda (
    id              BIGSERIAL       PRIMARY KEY,
    venda_id        BIGINT          NOT NULL REFERENCES vendas(id),
    variante_uuid   UUID            NOT NULL REFERENCES variantes(uuid),
    sku             VARCHAR(50)     NOT NULL,
    quantity        INT             NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(10,2)   NOT NULL,
    line_total      NUMERIC(12,2)   NOT NULL
);
CREATE INDEX idx_itens_venda_venda_id ON itens_venda(venda_id);
```

### clientes
```sql
CREATE TABLE clientes (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    full_name       VARCHAR(255)    NOT NULL,
    cpf             CHAR(11)        NOT NULL UNIQUE,
    email           VARCHAR(255),
    phone           VARCHAR(15),
    birth_date      DATE,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_clientes_cpf  ON clientes(cpf);
CREATE INDEX idx_clientes_name ON clientes(LOWER(full_name));
```

### lancamentos_financeiros
```sql
CREATE TABLE lancamentos_financeiros (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    type                VARCHAR(10)     NOT NULL CHECK (type IN ('RECEITA','DESPESA')),
    amount              NUMERIC(15,2)   NOT NULL CHECK (amount BETWEEN 0.01 AND 999999999.99),
    payment_method      VARCHAR(10),
    description         VARCHAR(255)    NOT NULL,
    category            VARCHAR(20),
    competence_date     DATE            NOT NULL,
    responsible_uuid    UUID            NOT NULL,
    sale_uuid           UUID            UNIQUE,  -- idempotency key
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_lancamentos_competence ON lancamentos_financeiros(competence_date);
CREATE INDEX idx_lancamentos_type       ON lancamentos_financeiros(type);
```

### campanhas
```sql
CREATE TABLE campanhas (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    type            VARCHAR(15)     NOT NULL CHECK (type IN ('PERCENTAGE','FIXED','PROGRESSIVE')),
    target_type     VARCHAR(10)     NOT NULL CHECK (target_type IN ('PRODUTO','CATEGORY','ALL')),
    target_uuid     UUID,
    target_category VARCHAR(100),
    discount_value  NUMERIC(10,2)   NOT NULL,
    min_quantity    INT,
    cashback_pct    NUMERIC(5,2),
    starts_at       TIMESTAMPTZ     NOT NULL,
    ends_at         TIMESTAMPTZ     NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_campanhas_active_dates ON campanhas(starts_at, ends_at) WHERE active = TRUE;
```

### cupons
```sql
CREATE TABLE cupons (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    code            VARCHAR(50)     NOT NULL,
    type            VARCHAR(15)     NOT NULL CHECK (type IN ('PERCENTAGE','FIXED','PROGRESSIVE')),
    discount_value  NUMERIC(10,2)   NOT NULL,
    starts_at       TIMESTAMPTZ     NOT NULL,
    ends_at         TIMESTAMPTZ     NOT NULL,
    max_usages      INT             NOT NULL,
    usage_count     INT             NOT NULL DEFAULT 0,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    version         BIGINT          NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX uq_cupom_code ON cupons(LOWER(code));
```

### domain_events (dead-letter + outbox)
```sql
CREATE TABLE domain_events (
    id              BIGSERIAL       PRIMARY KEY,
    event_id        UUID            NOT NULL UNIQUE,  -- idempotency key
    event_type      VARCHAR(50)     NOT NULL,
    payload         JSONB           NOT NULL,
    occurred_at     TIMESTAMPTZ     NOT NULL,
    status          VARCHAR(10)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','DELIVERED','FAILED','DLQ')),
    retry_count     INT             NOT NULL DEFAULT 0,
    next_retry_at   TIMESTAMPTZ,
    last_error      TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_domain_events_status ON domain_events(status, next_retry_at)
    WHERE status IN ('PENDING','FAILED');
```

---

## API Design

All endpoints are prefixed with `/api/v1`. Authentication is via `Authorization: Bearer <jwt>`.

### Auth — `/api/v1/auth`

| Method | Path              | Body / Params | Response | Roles |
|--------|-------------------|---------------|----------|-------|
| POST   | `/login`          | `{username, password}` | `{accessToken, refreshToken, expiresIn}` | — |
| POST   | `/refresh`        | `{refreshToken}` | `{accessToken, refreshToken, expiresIn}` | — |
| POST   | `/logout`         | `{refreshToken}` | `204` | any authenticated |

**Login request:**
```json
{ "username": "joao@loja.com", "password": "S3nh@Segura!" }
```
**Login response:**
```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<opaque-token>",
  "expiresIn": 900
}
```

---

### Products — `/api/v1/products`

| Method | Path                              | Body / Params | Response | Roles |
|--------|-----------------------------------|---------------|----------|-------|
| POST   | `/`                               | RegisterProductRequest | 201 ProdutoResponse | MANAGER |
| GET    | `/{uuid}`                         | — | ProdutoResponse | MANAGER, STOCK |
| PUT    | `/{uuid}`                         | UpdateProductRequest | ProdutoResponse | MANAGER |
| DELETE | `/{uuid}/deactivate`              | — | 204 | MANAGER |
| POST   | `/{uuid}/variants`                | RegisterVariantRequest | 201 VarianteResponse | MANAGER |
| GET    | `/variants/by-sku/{sku}`          | — | VarianteResponse | MANAGER, CASHIER, STOCK |
| GET    | `/variants/by-barcode/{barcode}`  | — | VarianteResponse | MANAGER, CASHIER, STOCK |

**RegisterProductRequest:**
```json
{ "name": "Camiseta Básica", "brand": "HaveRed", "category": "Camisetas" }
```
**ProdutoResponse:**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Camiseta Básica",
  "brand": "HaveRed",
  "category": "Camisetas",
  "active": true
}
```
**RegisterVariantRequest:**
```json
{
  "sku": "CAM-P-BRANCA-001",
  "size": "P",
  "color": "Branca",
  "barcode": "7891234567890",
  "price": 59.90,
  "cost": 22.00
}
```

---

### Inventory — `/api/v1/inventory`

| Method | Path                        | Body / Params | Response | Roles |
|--------|-----------------------------|---------------|----------|-------|
| GET    | `/variants/{uuid}/stock`    | — | StockResponse | MANAGER, STOCK |
| POST   | `/variants/{uuid}/entries`  | `{quantity, actorUuid}` | 201 MovimentoResponse | MANAGER, STOCK |
| POST   | `/variants/{uuid}/withdrawals` | `{quantity, actorUuid}` | 201 MovimentoResponse | MANAGER, STOCK |
| GET    | `/variants/{uuid}/movements`| `?page&size&from&to` | Page\<MovimentoResponse\> | MANAGER, STOCK |

**StockResponse:**
```json
{
  "varianteUuid": "...",
  "physicalStock": 50,
  "reservedStock": 5,
  "availableStock": 45
}
```

---

### Sales — `/api/v1/sales`

| Method | Path                        | Body / Params | Response | Roles |
|--------|-----------------------------|---------------|----------|-------|
| POST   | `/`                         | OpenSaleRequest | 201 VendaResponse | CASHIER, MANAGER |
| POST   | `/{uuid}/items`             | AddItemRequest | VendaResponse | CASHIER, MANAGER |
| DELETE | `/{uuid}/items/{sku}`       | — | VendaResponse | CASHIER, MANAGER |
| POST   | `/{uuid}/finalize`          | FinalizeSaleRequest | FinalizationResponse | CASHIER, MANAGER |
| POST   | `/{uuid}/cancel`            | CancelSaleRequest | 204 | CASHIER, MANAGER |
| GET    | `/{uuid}`                   | — | VendaResponse | MANAGER, FINANCE |
| GET    | `/`                         | `?from&to&page&size` | Page\<VendaResponse\> | MANAGER, FINANCE |

**OpenSaleRequest:**
```json
{ "terminalId": "PDV-01", "clienteUuid": "..." }
```
**AddItemRequest:**
```json
{ "barcode": "7891234567890", "quantity": 2 }
```
**FinalizeSaleRequest:**
```json
{
  "paymentMethod": "DINHEIRO",
  "amountPaid": 130.00,
  "couponCode": "PROMO10",
  "expectedTotal": 119.80
}
```
**FinalizationResponse:**
```json
{
  "uuid": "...",
  "subtotal": 119.80,
  "discountAmount": 11.98,
  "taxAmount": 0.00,
  "total": 107.82,
  "changeAmount": 22.18,
  "paymentMethod": "DINHEIRO"
}
```

---

### Customers — `/api/v1/customers`

| Method | Path          | Body / Params | Response | Roles |
|--------|---------------|---------------|----------|-------|
| POST   | `/`           | RegisterCustomerRequest | 201 ClienteResponse | MANAGER |
| GET    | `/{uuid}`     | — | ClienteResponse | MANAGER, CASHIER |
| PUT    | `/{uuid}`     | UpdateCustomerRequest | ClienteResponse | MANAGER |
| DELETE | `/{uuid}/deactivate` | — | 204 | MANAGER |
| GET    | `/search`     | `?cpf=&name=&uuid=&page&size` | Page\<ClienteResponse\> | MANAGER, CASHIER |

**RegisterCustomerRequest:**
```json
{
  "fullName": "Maria Silva",
  "cpf": "12345678909",
  "email": "maria@email.com",
  "phone": "11987654321",
  "birthDate": "1990-03-15"
}
```

---

### Finance — `/api/v1/finance`

| Method | Path             | Body / Params | Response | Roles |
|--------|------------------|---------------|----------|-------|
| POST   | `/expenses`      | RegisterExpenseRequest | 201 LancamentoResponse | MANAGER, FINANCE |
| GET    | `/cash-flow`     | `?from=&to=` | CashFlowReport | MANAGER, FINANCE |
| GET    | `/entries/{uuid}`| — | LancamentoResponse | MANAGER, FINANCE |

**RegisterExpenseRequest:**
```json
{
  "amount": 1500.00,
  "description": "Aluguel do mês de janeiro",
  "category": "ALUGUEL",
  "competenceDate": "2025-01-01"
}
```
**CashFlowReport:**
```json
{
  "from": "2025-01-01",
  "to": "2025-01-31",
  "totalReceita": 45000.00,
  "totalDespesa": 12000.00,
  "netBalance": 33000.00,
  "dailyEntries": [
    {
      "date": "2025-01-01",
      "receitas": [...],
      "despesas": [...],
      "dailyBalance": 1200.00
    }
  ]
}
```

---

### Pricing — `/api/v1/pricing`

| Method | Path                           | Body / Params | Response | Roles |
|--------|--------------------------------|---------------|----------|-------|
| POST   | `/campaigns`                   | CreateCampaignRequest | 201 CampanhaResponse | MANAGER |
| GET    | `/campaigns`                   | `?active=&page&size` | Page\<CampanhaResponse\> | MANAGER |
| PUT    | `/campaigns/{uuid}/deactivate` | — | 204 | MANAGER |
| POST   | `/coupons`                     | CreateCouponRequest | 201 CupomResponse | MANAGER |
| POST   | `/calculate`                   | DiscountQuery | DiscountResult | CASHIER, MANAGER |
| POST   | `/coupons/{code}/confirm`      | `{saleUuid}` | 204 | CASHIER, MANAGER (internal) |

**DiscountQuery:**
```json
{
  "saleUuid": "...",
  "items": [{"varianteUuid": "...", "quantity": 2, "unitPrice": 59.90}],
  "subtotal": 119.80,
  "couponCode": "PROMO10"
}
```
**DiscountResult:**
```json
{
  "discountAmount": 11.98,
  "cashbackAmount": 0.00,
  "appliedRules": [
    {"type": "COUPON", "code": "PROMO10", "discount": 11.98}
  ]
}
```

---

## Domain Event System Design

### Event Envelope

```java
// br.com.moreiracruz.erp.shared.events.EventEnvelope
public record EventEnvelope<T>(
    UUID     eventId,        // UUIDv4 — idempotency key
    String   eventType,      // e.g. "SaleCompleted"
    Instant  occurredAt,     // ISO 8601
    T        payload
) {}
```

### Event Types

```java
// SaleCompletedEvent payload
public record SaleCompletedPayload(
    UUID saleUuid, UUID operatorUuid,
    List<SaleItem> items,   // {sku, quantity}
    BigDecimal total,
    String paymentMethod
) {}

// StockReservedEvent payload
public record StockReservedPayload(
    UUID varianteUuid, int reservedQuantity, UUID saleUuid
) {}

// PaymentApprovedEvent payload
public record PaymentApprovedPayload(
    UUID saleUuid, BigDecimal approvedAmount, String paymentMethod
) {}
```

### In-Process Event Bus

```
┌──────────────┐   publish(envelope)    ┌─────────────────────┐
│  Publisher   │ ──────────────────────▶│  InProcessEventBus  │
│  (use case)  │                        │  (Spring @Component) │
└──────────────┘                        └──────────┬──────────┘
                                                   │ dispatch to registered handlers
                                                   ▼
                                        ┌──────────────────────┐
                                        │  EventHandlerRegistry │
                                        │  Map<eventType,       │
                                        │    List<Handler>>     │
                                        └──────────┬───────────┘
                                                   │ for each handler:
                                                   ▼
                                        ┌──────────────────────┐
                                        │  Handler.handle()     │
                                        │  (checks idempotency  │
                                        │   key in DB first)    │
                                        └──────────────────────┘
```

**Bus implementation**:
- Synchronous dispatch within the same transaction by default
- If a handler throws, the bus catches the exception, records the event to `domain_events` table with `status=FAILED`, and schedules retry
- Retry scheduler runs every 10 seconds, queries `FAILED` events with `next_retry_at <= NOW()`, re-dispatches with exponential backoff: `nextRetry = now + (2^retryCount) seconds`; max 3 retries before marking `DLQ`

**Idempotency**:
Every handler implementation MUST check `domain_events.event_id` before processing:
```java
if (domainEventRepo.existsByEventId(envelope.eventId())) return; // discard duplicate
// process ...
domainEventRepo.markDelivered(envelope.eventId());
```

### Event Consumer Registration

Consumers register themselves via a Spring `@EventHandler` annotation (custom) that the `EventHandlerRegistry` scans at startup:

```java
@Component
public class FinanceEventConsumer {
    @EventHandler("SaleCompleted")
    public void onSaleCompleted(EventEnvelope<SaleCompletedPayload> event) { ... }
}
```

---

## Security Design

### JWT Filter Chain

```
HTTP Request
     │
     ▼
JwtAuthenticationFilter  (OncePerRequestFilter, order = -100)
     │
     ├─ Extract Bearer token from Authorization header
     ├─ Validate signature using HS256 secret (from ENV)
     ├─ Check exp claim — if expired, short-circuit to 401
     ├─ Extract uuid + role claims → build UsernamePasswordAuthenticationToken
     ├─ Set SecurityContextHolder
     │
     ▼
RoleAuthorizationFilter  (method-level via @PreAuthorize)
     │
     ├─ @PreAuthorize("hasRole('MANAGER') or hasRole('CASHIER')")
     ├─ On 403: AccessDeniedHandler returns {error, requiredRoles}
     │
     ▼
Controller
```

### Token Storage

| Token | Storage | TTL | Revocation |
|-------|---------|-----|------------|
| JWT | Stateless (client-side) | 15 min | Not directly revocable; short TTL |
| Refresh Token | `refresh_tokens` table (hashed) | 7 days | `revoked_at` timestamp set on use or logout |

Refresh token flow:
1. On successful login: generate opaque random 256-bit token, store `SHA-256(token)` in DB, return raw token to client
2. On `/refresh`: hash the presented token, look up in DB, verify `expires_at > now` and `revoked_at IS NULL`, then issue new JWT + new refresh token, immediately set `revoked_at = NOW()` on the used token (token rotation)
3. On `/logout`: set `revoked_at = NOW()` on the presented token

### Brute Force Protection

```
Login attempt:
  1. Load Usuario by username
  2. If lockedUntil > NOW() → return 401 "Credenciais inválidas"
  3. Verify password
  4. If WRONG:
       failedAttempts++
       IF failedAttempts >= 5:
         lockedUntil = NOW() + 15min
         log SECURITY_EVENT(username, count, timestamp)
  5. If CORRECT:
       failedAttempts = 0, lockedUntil = NULL
       issue tokens
```

### RBAC Matrix

| Endpoint Area | MANAGER | CASHIER | STOCK | FINANCE |
|---------------|---------|---------|-------|---------|
| Auth          | RW | RW (own) | RW (own) | RW (own) |
| Products (read) | ✓ | ✓ | ✓ | — |
| Products (write) | ✓ | — | — | — |
| Inventory (read) | ✓ | — | ✓ | — |
| Inventory (write) | ✓ | — | ✓ | — |
| Sales (write) | ✓ | ✓ | — | — |
| Sales (read) | ✓ | — | — | ✓ |
| Customers (read) | ✓ | ✓ | — | — |
| Customers (write) | ✓ | — | — | — |
| Finance (read) | ✓ | — | — | ✓ |
| Finance (write) | ✓ | — | — | ✓ |
| Pricing (read) | ✓ | ✓ | — | — |
| Pricing (write) | ✓ | — | — | — |

---

## Inventory Concurrency Design

### Pessimistic Lock Strategy

All stock mutation operations use `SELECT FOR UPDATE` at the database level, acquired inside a `@Transactional` block:

```java
@Transactional
public ReservaResponse reserve(StockReserveCommand cmd) {
    EstoqueItem item = estoqueRepo.findByVarianteUuidForUpdate(cmd.varianteUuid());
    // ^ issues: SELECT * FROM estoque_items WHERE variante_uuid = ? FOR UPDATE
    
    int available = item.physicalStock() - item.reservedStock();
    if (available < cmd.quantity()) {
        throw new InsufficientStockException(available);
    }
    item.incrementReserved(cmd.quantity());
    estoqueRepo.save(item);  // UPDATE estoque_items SET reserved_stock = ?
    
    ReservaEstoque reserva = ReservaEstoque.create(cmd, expiresAt);
    reservaRepo.save(reserva);
    
    eventBus.publish(new EventEnvelope<>(
        UUID.randomUUID(), "StockReserved", Instant.now(),
        new StockReservedPayload(cmd.varianteUuid(), cmd.quantity(), cmd.saleUuid())
    ));
    return ReservaResponse.from(reserva);
}
```

### Reservation Expiry Scheduler

```java
@Component
public class ReservationExpiryScheduler {

    @Scheduled(fixedDelayString = "${inventory.expiry.check-interval-ms:60000}")
    @Transactional
    public void expireStaleReservations() {
        List<ReservaEstoque> expired = reservaRepo.findExpiredActive(Instant.now());
        for (ReservaEstoque r : expired) {
            EstoqueItem item = estoqueRepo.findByVarianteUuidForUpdate(r.varianteUuid());
            item.decrementReserved(r.quantity());
            estoqueRepo.save(item);
            r.markExpired();
            reservaRepo.save(r);
            movimentoRepo.save(MovimentoEstoque.liberacao(r));
        }
    }
}
```

**Configuration** (application.yml / ENV):
```yaml
inventory:
  expiry:
    reservation-ttl-minutes: 30    # INVENTORY_EXPIRY_TTL_MINUTES
    check-interval-ms: 60000       # INVENTORY_EXPIRY_CHECK_MS
```

### Concurrency Guarantees

- `SELECT FOR UPDATE` prevents two concurrent transactions from reserving the same stock simultaneously
- The `chk_available_non_negative` DB constraint acts as a last-resort safety net
- The `version` column on `estoque_items` enables Spring Data's `@Version` for detecting stale reads in non-lock paths

---

## Observability Setup

### OpenTelemetry

Dependency: `opentelemetry-spring-boot-starter` (auto-instrumentation).

- Every inbound HTTP request automatically gets `trace_id` + `span_id`
- MDC is populated with `traceId` and `spanId` for log correlation
- Outgoing HTTP calls (RestTemplate / WebClient) propagate W3C `traceparent` header
- OTLP exporter sends traces to an OTel Collector sidecar (configured via `OTEL_EXPORTER_OTLP_ENDPOINT`)

### Prometheus Metrics

Spring Boot Actuator + Micrometer Prometheus registry:

```yaml
management:
  endpoints.web.exposure.include: health,prometheus,info
  endpoint.health.show-details: always
  metrics.distribution:
    percentiles-histogram:
      http.server.requests: true
    percentiles:
      http.server.requests: 0.5, 0.95, 0.99
```

Key custom metrics:
| Metric | Type | Labels |
|--------|------|--------|
| `erp_sale_finalized_total` | Counter | `payment_method` |
| `erp_stock_reservation_total` | Counter | `status` (success/rejected) |
| `erp_event_bus_dispatch_total` | Counter | `event_type`, `status` |
| `erp_coupon_applied_total` | Counter | `coupon_code` |

### Loki Structured Logging

Logback configuration (`logback-spring.xml`) uses `logstash-logback-encoder`:

```xml
<appender name="JSON_STDOUT" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <customFields>{"app":"erp-loja-roupas","env":"${ENV:local}"}</customFields>
    <fieldNames>
      <timestamp>timestamp</timestamp>
      <level>level</level>
      <message>message</message>
    </fieldNames>
    <includeMdcKeyName>traceId</includeMdcKeyName>
    <includeMdcKeyName>spanId</includeMdcKeyName>
    <includeMdcKeyName>module</includeMdcKeyName>
  </encoder>
</appender>
```

Every log entry emitted to stdout will be a single-line JSON:
```json
{
  "timestamp": "2025-01-15T14:32:00.123Z",
  "level": "INFO",
  "trace_id": "abc123",
  "module": "sales",
  "message": "Sale finalized",
  "saleUuid": "...",
  "total": 119.80
}
```

### Health Check

`/actuator/health` aggregates custom `HealthIndicator` beans:
- `DatabaseHealthIndicator` — runs `SELECT 1` with 1s timeout
- `EventQueueHealthIndicator` — checks count of `domain_events` with `status=FAILED` (DOWN if > threshold)

Response times must be ≤ 2 seconds (enforced by timeouts in each indicator).

---

## Infrastructure / Docker Compose Topology

```yaml
# docker-compose.yml
version: "3.9"
services:

  app:
    image: erp-loja-roupas:latest
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/erp
      SPRING_DATASOURCE_USERNAME: erp_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      INVENTORY_EXPIRY_TTL_MINUTES: 30
      OTEL_EXPORTER_OTLP_ENDPOINT: http://otel-collector:4318
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health/readiness"]
      interval: 15s
      timeout: 5s
      retries: 5

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: erp
      POSTGRES_USER: erp_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U erp_user -d erp"]
      interval: 5s
      timeout: 3s
      retries: 10

  otel-collector:
    image: otel/opentelemetry-collector-contrib:latest
    volumes:
      - ./otel-config.yml:/etc/otelcol/config.yaml
    ports:
      - "4318:4318"

  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD}

volumes:
  pgdata:
```

**AWS ECS Fargate (scale-out)**:
- ECS Task Definition references the same Docker image
- Secrets via AWS Secrets Manager → ECS secret injection (no hardcoded values)
- RDS PostgreSQL Multi-AZ replaces the `postgres` service
- ElastiCache Redis replaces in-memory rate-limit state (if needed for brute-force counters across replicas)
- ALB target group with `/actuator/health/readiness` as the health check path

**Dockerfile** (multi-stage):
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw -pl bootstrap -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/bootstrap/target/erp-bootstrap.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

---

## Maven Multi-Module Structure

```
erp/                               ← parent pom.xml
├── shared/
│   ├── shared-kernel/             ← pom.xml (AggregateRoot, DomainEvent, etc.)
│   ├── shared-events/             ← pom.xml (event payload records)
│   └── shared-exceptions/         ← pom.xml (BusinessException, etc.)
├── modules/
│   ├── auth/                      ← pom.xml
│   ├── product/                   ← pom.xml
│   ├── inventory/                 ← pom.xml
│   ├── sales/                     ← pom.xml
│   ├── customer/                  ← pom.xml
│   ├── finance/                   ← pom.xml
│   └── pricing/                   ← pom.xml
├── infrastructure/                ← pom.xml (JPA, JWT, EventBus, OTel)
└── bootstrap/                     ← pom.xml (main class, Spring Boot plugin)
```

Dependency direction: `bootstrap → modules/* + infrastructure`, `modules/* → shared/*`, no lateral `module-A → module-B` imports.

---

## Error Handling

All errors follow a uniform envelope:

```json
{
  "timestamp": "2025-01-15T14:32:00.123Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "traceId": "abc123",
  "violations": [
    { "field": "sku", "message": "SKU já cadastrado", "conflictingUuid": "..." }
  ]
}
```

Global `@RestControllerAdvice` maps domain exceptions to HTTP status:

| Exception | HTTP Status |
|-----------|-------------|
| `ValidationException` | 422 |
| `NotFoundException` | 404 |
| `AccessDeniedException` | 403 |
| `AuthenticationException` | 401 |
| `DateRangeException` | 400 |
| `ConflictException` (campaign overlap) | 422 |
| `Throwable` (unexpected) | 500 (logs stack trace with traceId) |

---

## Testing Strategy

### Approach

The system uses a **dual testing approach**:
- **Unit tests** for specific scenarios, edge cases, and error conditions within each module
- **Property-based tests** (PBT) for universal correctness properties using **jqwik** (Java PBT library)

**PBT Configuration**: minimum 100 iterations per property test (jqwik default is 1000). Each test references its design property via a `@Label` annotation.

### Unit Test Coverage
- Each use case: at least one happy-path and one error-path test
- Domain entities: all invariant checks
- Value Objects: all validation rules (`Cpf`, `Barcode`, `Money`)
- Integration tests: REST controllers with `@SpringBootTest` + Testcontainers (PostgreSQL)

---

## Correctness Properties


*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

#### Reflection: Consolidating Redundant Properties

Before listing final properties, redundant candidates are consolidated:

- Properties 1.1 (JWT expiry) and 1.5 (JWT claims) are combined — both concern JWT structure after login → **Property 1: JWT structure after login**
- Properties 1.3 (refresh invalidation) and 1.6 (logout invalidation) share the same invariant (token use/logout → token is revoked) → **Property 2: Refresh token single-use / revocation invariant**
- Properties 4.1-4.2 (stock invariant) and 4.3-4.4 (entry/withdrawal arithmetic) are consolidated — the invariant subsumes the arithmetic check → **Property 6: Stock counter invariant after any operation**
- Properties 4.6 (reservation on success) and 4.8 (release restores) are related round-trips → **Property 7: Reserve-then-release round trip**
- Properties 11.1-11.3 (event envelope structure) and 11.6 (idempotency) are consolidated into general event properties → **Property 13 and 14**
- Properties 7.2 (Finance idempotency) and the general 11.6 note are the same concept applied concretely → kept as **Property 12** only

---

### Property 1: JWT claims are always well-formed after successful login

*For any* valid user credentials that produce a successful login, the returned JWT SHALL always contain: the user's UUID in the `sub` claim, exactly one of the four valid roles in the `role` claim, and an `exp` claim equal to the current Unix epoch plus 900 seconds (within a 2-second tolerance). The refresh token SHALL have an expiry of exactly 7 days (604,800 seconds) from issuance.

**Validates: Requirements 1.1, 1.5**

---

### Property 2: Refresh token is a single-use credential

*For any* active refresh token that has been presented once (either via `/refresh` or `/logout`), any subsequent use of that same token SHALL always return HTTP 401, regardless of how much validity time remains. There is no input combination for which a token can be consumed and then accepted again.

**Validates: Requirements 1.3, 1.6**

---

### Property 3: Invalid credentials never reveal the failing field

*For any* credential pair `(username, password)` where the password does not match the stored hash for that username (or the username does not exist), the response SHALL always be HTTP 401 with body `{"message": "Credenciais inválidas"}` and no field indicating whether username or password was the mismatch.

**Validates: Requirements 1.2**

---

### Property 4: Account lockout activates after 5 consecutive failures

*For any* username, after 5 or more consecutive failed login attempts within a 15-minute window, all subsequent login attempts for that username SHALL return HTTP 401, even when the correct password is supplied, until the 15-minute lockout window has elapsed.

**Validates: Requirements 1.7**

---

### Property 5: Unknown or invalid roles in a JWT are always rejected

*For any* JWT whose `role` claim contains a value that is not one of `{ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, ROLE_FINANCE}`, every protected endpoint SHALL return HTTP 403, regardless of the endpoint or any other JWT field values.

**Validates: Requirements 2.4**

---

### Property 6: Stock counter invariant holds after every operation

*For any* `EstoqueItem` and *for any* sequence of stock operations (entries, withdrawals, reservations, or releases), the invariant `availableStock = physicalStock − reservedStock` SHALL hold true at all times, and neither `physicalStock` nor `reservedStock` nor `availableStock` shall ever be negative.

**Validates: Requirements 4.1, 4.2**

---

### Property 7: Reserve-then-release restores original stock state

*For any* `EstoqueItem` with `availableStock ≥ Q`, after reserving quantity `Q` and subsequently releasing that reservation, the `physicalStock`, `reservedStock`, and `availableStock` SHALL be identical to their values before the reservation was created.

**Validates: Requirements 4.6, 4.8**

---

### Property 8: Withdrawals that would create negative physicalStock are always rejected

*For any* `EstoqueItem` and *for any* withdrawal quantity `Q > physicalStock`, the withdrawal operation SHALL always return HTTP 422 containing the current `physicalStock` value, and the stock counters SHALL remain unchanged.

**Validates: Requirements 4.5**

---

### Property 9: Sale total is always computed on the backend (never trusted from client)

*For any* sale finalization request where the client-submitted `expectedTotal` differs by any amount — even one cent — from the backend-computed total (`sum(unitPrice × quantity) − discountAmount + taxAmount`), the request SHALL always be rejected with HTTP 422 and message `"Valor de total inválido"`.

**Validates: Requirements 5.5, 5.6**

---

### Property 10: Cash payment change calculation is exact

*For any* cash payment where `amountPaid ≥ total`, the returned `changeAmount` SHALL always equal `amountPaid − total` exactly (no rounding errors). *For any* cash payment where `amountPaid < total`, the operation SHALL always return HTTP 422 with the exact total due.

**Validates: Requirements 5.7, 5.8**

---

### Property 11: Product deactivation cascades to all variants atomically

*For any* `Produto` with `N ≥ 0` associated active `Variante` records, after a deactivation operation completes, **all** `N` variants SHALL have `active = false`. There is no input state for which deactivating a `Produto` leaves any of its variants active.

**Validates: Requirements 3.6**

---

### Property 12: SaleCompletedEvent idempotency — exactly one RECEITA entry

*For any* `SaleCompletedEvent` with a given `saleUuid`, regardless of how many times that event is delivered to the `Finance_Service` (`N ≥ 1` deliveries), exactly one `LancamentoFinanceiro` of type `RECEITA` with that `saleUuid` SHALL exist in the database.

**Validates: Requirements 7.2, 11.6**

---

### Property 13: Combined discount never exceeds sale total

*For any* sale total `T` and *for any* combination of applicable discount rules (coupon + campaign percentage + campaign fixed), the total discount amount `D` SHALL always satisfy `0 ≤ D ≤ T`, meaning the final payable amount `T − D ≥ 0` for all input combinations.

**Validates: Requirements 8.8**

---

### Property 14: Coupon usage count never exceeds its maximum limit

*For any* coupon with `maxUsages = N`, regardless of concurrent confirmation attempts (race conditions), the final `usageCount` SHALL never exceed `N`. Any confirmation attempt that would push the count beyond `N` SHALL always return HTTP 422, and the sale finalization SHALL be rolled back.

**Validates: Requirements 8.7**

---

### Property 15: Domain event envelope is always structurally valid

*For any* triggering business operation (sale completion, stock reservation, payment approval), the emitted `EventEnvelope` SHALL always contain: a non-null `eventId` (valid UUIDv4), the correct `eventType` string matching the event class, a non-null `occurredAt` timestamp in ISO 8601 format, and a non-null `payload` with all required fields present and non-null.

**Validates: Requirements 11.1, 11.2, 11.3**

---

### Property 16: Cash flow net balance equals sum of receitas minus sum of despesas

*For any* date range `[from, to]` with `from ≤ to` and range ≤ 366 days, the `netBalance` in the cash flow report SHALL always equal the exact arithmetic sum of all `RECEITA` amounts minus the exact arithmetic sum of all `DESPESA` amounts for entries whose `competenceDate` falls within the range (inclusive on both ends).

**Validates: Requirements 7.5, 7.8**

---

### Property 17: CPF validation is deterministic and algorithm-correct

*For any* string of exactly 11 digits, the CPF validation function SHALL always return the same result as the standard Brazilian CPF check-digit algorithm (verifying both check digits). The function is pure — given the same input it always produces the same output with no side effects.

**Validates: Requirements 6.2**

---

### Property 18: Duplicate CPF registration never exposes existing customer data

*For any* registration attempt using a CPF that already exists in the database (active or inactive), the HTTP 422 response body SHALL contain only `{"message": "CPF já cadastrado"}` and SHALL NOT contain any field from the existing customer record (UUID, name, email, phone, birthDate, or active status).

**Validates: Requirements 6.3**

---

### Property 19: Quantidade fora do intervalo [1, 100.000] é sempre rejeitada

*For any* stock entry or withdrawal operation with quantity `Q < 1` or `Q > 100,000`, the operation SHALL always return HTTP 422 specifying the allowed range, and the stock counters SHALL remain unchanged.

**Validates: Requirements 4.10**

---

## jqwik Test Configuration

Each property above maps to a single `@Property` test in jqwik. Configuration applied globally:

```java
@Property(tries = 1000)
@Label("Feature: erp-loja-roupas, Property 6: Stock counter invariant after any operation")
void stockCounterInvariant(@ForAll("stockOperationSequences") List<StockOp> ops) {
    // ... arrange, act, assert availableStock = physical - reserved >= 0
}
```

Tag format: `Feature: erp-loja-roupas, Property {N}: {property title}`

Libraries:
- **jqwik** `1.8.x` — property-based testing engine for JUnit 5
- **Testcontainers** — PostgreSQL container for integration-level property tests
- **AssertJ** — fluent assertions

Minimum runs: 1000 iterations per property (jqwik default). Pure-function properties run in-memory; persistence-level properties use Testcontainers PostgreSQL.
