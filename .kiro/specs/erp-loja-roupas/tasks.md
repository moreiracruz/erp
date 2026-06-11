# Implementation Plan: ERP Loja de Roupas

## Overview

Implementation of a Modular Monolith ERP for clothing retail stores using Java 21, Spring Boot 3, Maven multi-module, PostgreSQL, Flyway, and jqwik for property-based testing. The architecture follows Hexagonal (Ports & Adapters) principles per module, with an in-process domain event bus, pessimistic locking for inventory, and JWT-based RBAC.

The plan is organized into four sprints:
- **Sprint 1**: Project scaffold + Auth + Product + Inventory
- **Sprint 2**: Sales/PDV + Pricing + Payment
- **Sprint 3**: Customers + Finance + Domain Events
- **Sprint 4**: Observability + Infrastructure + PBT test suite

---

## Tasks

---

## Sprint 1: Project Scaffold + Auth + Product + Inventory

- [x] 1. Set up Maven multi-module project structure and shared kernel
  - [x] 1.1 Create parent `pom.xml` and all module `pom.xml` files
    - Create root `pom.xml` with `<modules>` listing: `shared/shared-kernel`, `shared/shared-events`, `shared/shared-exceptions`, `modules/auth`, `modules/product`, `modules/inventory`, `modules/sales`, `modules/customer`, `modules/finance`, `modules/pricing`, `infrastructure`, `bootstrap`
    - Set Java 21, Spring Boot 3 BOM, jqwik 1.8.x, Testcontainers, and AssertJ dependency versions in the parent POM
    - Configure `maven-compiler-plugin` for Java 21 and `spring-boot-maven-plugin` in `bootstrap` only
    - _Requirements: 9.1, 12.1_

  - [x] 1.2 Implement `shared-kernel` module
    - Create `com.erp.shared.kernel.AggregateRoot` base class with `id` and `uuid` fields
    - Create `com.erp.shared.kernel.DomainEvent` marker interface
    - Create `com.erp.shared.kernel.Identifiable` interface
    - Create `com.erp.shared.utils.MoneyUtils` (BigDecimal rounding utilities)
    - _Requirements: 9.5_

  - [x] 1.3 Implement `shared-exceptions` module
    - Create `com.erp.shared.exceptions.BusinessException` (base runtime exception)
    - Create `com.erp.shared.exceptions.ValidationException` (maps to HTTP 422)
    - Create `com.erp.shared.exceptions.NotFoundException` (maps to HTTP 404)
    - Create `com.erp.shared.exceptions.ConflictException` (maps to HTTP 422 for campaign overlap)
    - Create `com.erp.shared.exceptions.DateRangeException` (maps to HTTP 400)
    - _Requirements: 3.5, 4.5, 7.6_

  - [x] 1.4 Implement `shared-events` module
    - Create `com.erp.shared.events.EventEnvelope<T>` record with `eventId`, `eventType`, `occurredAt`, `payload`
    - Create `com.erp.shared.events.SaleCompletedPayload` record
    - Create `com.erp.shared.events.StockReservedPayload` record
    - Create `com.erp.shared.events.PaymentApprovedPayload` record
    - _Requirements: 11.1, 11.2, 11.3_

  - [x] 1.5 Set up `infrastructure` module: Flyway, JPA, error handling, ArchUnit
    - Configure `FlywayConfig` in `com.erp.infrastructure.persistence`
    - Configure `JpaConfig` with `@EnableJpaRepositories` and `@EnableTransactionManagement`
    - Create global `@RestControllerAdvice` (`GlobalExceptionHandler`) mapping all shared exceptions to their HTTP status codes and the uniform error envelope JSON
    - Add ArchUnit dependency and create `ModuleBoundaryTest` to enforce no lateral cross-module domain imports
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [x] 1.6 Write first Flyway migrations (V1–V3): core tables
    - `V1__create_usuarios_refresh_tokens.sql`: `usuarios` and `refresh_tokens` tables
    - `V2__create_produtos_variantes.sql`: `produtos`, `variantes` tables with all indexes listed in design
    - `V3__create_estoque_tables.sql`: `estoque_items`, `movimentos_estoque`, `reservas_estoque` tables
    - _Requirements: 9.1, 9.5, 9.6_

  - [x] 1.7 Set up `bootstrap` module and `application.yml`
    - Create `com.erp.bootstrap.ErpApplication` main class with `@SpringBootApplication`
    - Create `src/main/resources/application.yml` sourcing all secrets from environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `INVENTORY_EXPIRY_TTL_MINUTES`)
    - _Requirements: 12.1, 12.2_

- [x] 2. Checkpoint — Build passes, Flyway migrations applied cleanly
  - Run `./mvnw clean package -DskipTests` and verify all modules compile
  - Start Postgres via Docker Compose and verify Flyway applies V1–V3 without errors
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 3. Implement Auth module
  - [x] 3.1 Implement Auth domain model and value objects
    - Create `com.erp.modules.auth.domain.model.Usuario` aggregate root with all fields from design
    - Create `com.erp.modules.auth.domain.model.RefreshToken` entity
    - Create `Role` enum: `ROLE_MANAGER`, `ROLE_CASHIER`, `ROLE_STOCK`, `ROLE_FINANCE`
    - Implement brute-force lockout logic inside `Usuario.recordFailedAttempt()` and `Usuario.resetAttempts()`
    - _Requirements: 1.1, 1.5, 1.7, 1.8_

  - [x] 3.2 Implement Auth inbound ports and use case implementations
    - Define `LoginUseCase`, `RefreshTokenUseCase`, `LogoutUseCase` interfaces in `domain.port.in`
    - Implement `LoginUseCaseImpl`: validate credentials, check lockout, verify bcrypt hash, issue JWT + refresh token, reset counter on success
    - Implement `RefreshTokenUseCaseImpl`: hash presented token, verify DB record, rotate (revoke old, issue new pair)
    - Implement `LogoutUseCaseImpl`: set `revoked_at` on presented token
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.6, 1.7, 1.8_

  - [x] 3.3 Implement JWT infrastructure (`infrastructure` module)
    - Create `JwtTokenProvider` in `com.erp.infrastructure.security`: sign HS256 JWT with `sub` (UUID), `role`, `exp` (now + 900s) using secret from ENV
    - Create `JwtAuthenticationFilter` (`OncePerRequestFilter`, order -100): extract Bearer token, validate signature and `exp`, populate `SecurityContextHolder`
    - Create `RefreshTokenRepository` JPA implementation backed by `refresh_tokens` table (store SHA-256 hash of raw token)
    - Configure `SecurityConfig`: disable CSRF for API, configure stateless session, register `JwtAuthenticationFilter`, configure `AccessDeniedHandler` to return `{error: "Acesso negado", requiredRoles: [...]}`
    - _Requirements: 1.1, 1.3, 1.5, 2.2, 2.3_

  - [x] 3.4 Implement Auth REST adapter
    - Create `AuthController` at `/api/v1/auth` with `POST /login`, `POST /refresh`, `POST /logout`
    - Create request/response DTOs: `LoginRequest`, `TokenPairResponse`
    - Apply `@PreAuthorize` rules per endpoint; `logout` requires any authenticated role
    - _Requirements: 1.1, 1.3, 1.6, 2.1_

  - [ ]* 3.5 Write property test: JWT claims are always well-formed (Property 1)
    - **Property 1: JWT claims are always well-formed after successful login**
    - Generate arbitrary valid `Credentials` using jqwik `@ForAll`; for each: invoke `LoginUseCase`, parse the returned JWT, assert `sub` == user UUID, `role` ∈ valid set, `exp` == now + 900 ± 2s, refresh token expiry == now + 604800s
    - **Validates: Requirements 1.1, 1.5**

  - [ ]* 3.6 Write property test: Refresh token is single-use (Property 2)
    - **Property 2: Refresh token is a single-use credential**
    - For any active token, call `/refresh` once (succeeds), then attempt to use the same raw token again — assert HTTP 401 on second use regardless of remaining validity
    - **Validates: Requirements 1.3, 1.6**

  - [ ]* 3.7 Write property test: Invalid credentials never reveal the failing field (Property 3)
    - **Property 3: Invalid credentials never reveal the failing field**
    - For any `(username, password)` pair where either username is unknown or password is wrong, assert response is always HTTP 401 with body `{"message": "Credenciais inválidas"}` and no other fields
    - **Validates: Requirements 1.2**

  - [ ]* 3.8 Write property test: Account lockout activates after 5 consecutive failures (Property 4)
    - **Property 4: Account lockout activates after 5 consecutive failures**
    - Submit ≥ 5 consecutive wrong-password requests for any username, then submit the correct password — assert HTTP 401 is returned
    - **Validates: Requirements 1.7**

  - [ ]* 3.9 Write property test: Unknown/invalid roles in a JWT are always rejected (Property 5)
    - **Property 5: Unknown or invalid roles in a JWT are always rejected**
    - Generate JWTs with arbitrary role values outside `{ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, ROLE_FINANCE}`, assert every protected endpoint returns HTTP 403
    - **Validates: Requirements 2.4**

- [ ] 4. Implement Product module
  - [x] 4.1 Implement Product domain model and value objects
    - Create `com.erp.modules.product.domain.model.Produto` aggregate root
    - Create `VarianteProduto` entity owned by `Produto`
    - Create value objects `Sku(value)` (1–50 chars), `Barcode(value)` (8–14 digits), `Money(amount)` (0.01–999999.99)
    - Implement `Produto.deactivate()`: sets `active = false` on self and all `VarianteProduto` children
    - _Requirements: 3.1, 3.2, 3.6_

  - [x] 4.2 Implement Product inbound ports and use case implementations
    - Define `RegisterProductUseCase`, `UpdateProductUseCase`, `DeactivateProductUseCase`, `RegisterVariantUseCase`, `SearchVariantUseCase` in `domain.port.in`
    - Implement `RegisterProductUseCaseImpl`: validate name uniqueness (case-insensitive, active only), check required fields
    - Implement `RegisterVariantUseCaseImpl`: verify parent `Produto` is active, validate SKU/barcode uniqueness across all variants regardless of active status
    - Implement `DeactivateProductUseCaseImpl`: call `Produto.deactivate()` atomically within `@Transactional`
    - Implement `SearchVariantUseCaseImpl`: find by SKU or barcode
    - _Requirements: 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9_

  - [x] 4.3 Implement Product persistence adapters
    - Create JPA entities `ProdutoJpaEntity` and `VarianteJpaEntity` in `adapter.out.persistence`
    - Implement `ProdutoRepository` and `VarianteRepository` outbound port interfaces using Spring Data JPA
    - Add unique constraint enforcement (partial index `uq_active_produto_name` via `@Query` or native query check)
    - _Requirements: 3.3, 3.4, 9.6_

  - [x] 4.4 Implement Product REST adapter
    - Create `ProductController` at `/api/v1/products` with all seven endpoints from the API design
    - Apply `@PreAuthorize` per endpoint: write endpoints = `MANAGER`, read endpoints = `MANAGER or STOCK`, search by SKU/barcode = `MANAGER or CASHIER or STOCK`
    - _Requirements: 2.5, 2.7, 3.3–3.9_

  - [x]* 4.5 Write property test: Product deactivation cascades atomically (Property 11)
    - **Property 11: Product deactivation cascades to all variants atomically**
    - For any `Produto` with `N ≥ 0` generated variants, after calling `DeactivateProductUseCase`, assert all `N` variants have `active = false` in the database (Testcontainers)
    - **Validates: Requirements 3.6**

- [ ] 5. Implement Inventory module
  - [x] 5.1 Implement Inventory domain model
    - Create `com.erp.modules.inventory.domain.model.EstoqueItem` aggregate root with `physicalStock`, `reservedStock`, `version`
    - Add computed method `availableStock()`: returns `physicalStock - reservedStock`
    - Add `incrementPhysical(int qty)`, `decrementPhysical(int qty)`, `incrementReserved(int qty)`, `decrementReserved(int qty)` — each must guard `≥ 0` invariant; throw `ValidationException` on violation
    - Create `MovimentoEstoque` entity and `OperationType` enum: `ENTRADA`, `SAÍDA`, `RESERVA`, `LIBERAÇÃO_RESERVA`
    - Create `ReservaEstoque` entity with `ReservaStatus` enum: `ACTIVE`, `COMMITTED`, `RELEASED`, `EXPIRED`
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 5.2 Implement Inventory inbound ports and use case implementations
    - Define `RegisterEntryUseCase`, `RegisterWithdrawalUseCase`, `ReserveStockUseCase`, `ReleaseReserveUseCase`, `CommitReserveUseCase`, `GetStockUseCase` in `domain.port.in`
    - Implement `RegisterEntryUseCaseImpl`: validate quantity ∈ [1, 100000], call `incrementPhysical`, save `MovimentoEstoque` with type `ENTRADA`
    - Implement `RegisterWithdrawalUseCaseImpl`: validate quantity ∈ [1, 100000], call `decrementPhysical` (throws if insufficient), save `MovimentoEstoque` with type `SAÍDA`
    - Implement `ReserveStockUseCaseImpl`: acquire `findByVarianteUuidForUpdate` (SELECT FOR UPDATE), check `availableStock ≥ qty`, call `incrementReserved`, save `ReservaEstoque`, emit `StockReservedEvent`
    - Implement `ReleaseReserveUseCaseImpl`: load reservation, call `decrementReserved`, mark reservation `RELEASED`, save `MovimentoEstoque` type `LIBERAÇÃO_RESERVA`
    - Implement `CommitReserveUseCaseImpl`: for each `ACTIVE` reservation with given `saleUuid`, mark `COMMITTED` (reserved stock has already been decremented from physical by the sale completion flow)
    - _Requirements: 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10_

  - [x] 5.3 Implement Inventory persistence adapters with pessimistic lock
    - Create JPA entities for `EstoqueItem`, `MovimentoEstoque`, `ReservaEstoque`
    - Implement `EstoqueItemRepository` outbound port: `findByVarianteUuidForUpdate` using `@Lock(LockModeType.PESSIMISTIC_WRITE)` and `@QueryHints(@QueryHint(name = HINT_SPEC_LOCK_TIMEOUT, value = "3000"))`
    - Implement `MovimentoEstoqueRepository` and `ReservaEstoqueRepository`
    - _Requirements: 4.2, 4.6, 9.6_

  - [x] 5.4 Implement Reservation Expiry Scheduler
    - Create `ReservationExpiryScheduler` in `com.erp.modules.inventory.application` with `@Scheduled(fixedDelayString = "${inventory.expiry.check-interval-ms:60000}")`
    - Query `reservas_estoque` where `status = 'ACTIVE'` and `expires_at <= NOW()`, release each via `ReleaseReserveUseCase`
    - _Requirements: 4.8_

  - [x] 5.5 Implement Inventory REST adapter
    - Create `InventoryController` at `/api/v1/inventory` with four endpoints from API design
    - Apply `@PreAuthorize`: read = `MANAGER or STOCK`, entry/withdrawal = `MANAGER or STOCK`
    - _Requirements: 2.5, 2.7, 4.3, 4.4_

  - [x]* 5.6 Write property test: Stock counter invariant after any operation (Property 6)
    - **Property 6: Stock counter invariant holds after every operation**
    - Generate arbitrary sequences of `StockOp` (ENTRADA qty, SAÍDA qty, RESERVA qty, LIBERAÇÃO qty) using jqwik `@ForAll`; for each sequence apply operations to an `EstoqueItem` in-memory; assert `availableStock == physicalStock - reservedStock ≥ 0` after every step; invalid ops (would go negative) should throw and leave counters unchanged
    - **Validates: Requirements 4.1, 4.2**

  - [x]* 5.7 Write property test: Reserve-then-release round trip (Property 7)
    - **Property 7: Reserve-then-release restores original stock state**
    - For any `EstoqueItem` with `availableStock ≥ Q`, reserve Q then release — assert final `physicalStock`, `reservedStock`, `availableStock` are identical to pre-reservation values
    - **Validates: Requirements 4.6, 4.8**

  - [x]* 5.8 Write property test: Withdrawals that would create negative stock are always rejected (Property 8)
    - **Property 8: Withdrawals that would create negative physicalStock are always rejected**
    - For any `Q > physicalStock`, assert `RegisterWithdrawalUseCase` returns HTTP 422 with current `physicalStock`, counters unchanged
    - **Validates: Requirements 4.5**

  - [x]* 5.9 Write property test: Quantity out of [1, 100000] is always rejected (Property 19)
    - **Property 19: Quantidade fora do intervalo [1, 100.000] é sempre rejeitada**
    - For any `Q < 1` or `Q > 100000`, assert entry and withdrawal endpoints return HTTP 422 with allowed range message, counters unchanged
    - **Validates: Requirements 4.10**

- [x] 6. Sprint 1 Checkpoint — All tests pass, Auth + Product + Inventory functional
  - Verify ArchUnit module boundary tests pass (no lateral cross-module imports)
  - Run `./mvnw test` with Testcontainers; all unit and property tests green
  - Ensure all tests pass, ask the user if questions arise.

---

## Sprint 2: Sales/PDV + Pricing + Payment

- [x] 7. Write Flyway migrations V4–V5 for Sales and Pricing tables
  - [x] 7.1 Create `V4__create_vendas_tables.sql`
    - `vendas` table with all columns from schema, including `data_venda` index, `cliente_uuid` index, `operator_uuid` index
    - `itens_venda` table with `venda_id` index
    - _Requirements: 9.5, 9.6_

  - [x] 7.2 Create `V5__create_pricing_tables.sql`
    - `campanhas` table with composite index on `(starts_at, ends_at) WHERE active = TRUE`
    - `cupons` table with unique index on `LOWER(code)`
    - _Requirements: 9.1, 9.6_

- [x] 8. Implement Pricing module
  - [x] 8.1 Implement Pricing domain model
    - Create `Campanha` aggregate root with `CampaignType` enum (`PERCENTAGE`, `FIXED`, `PROGRESSIVE`), `TargetType` enum (`PRODUTO`, `CATEGORY`, `ALL`)
    - Create `Cupom` aggregate root with `usageCount` and `version` fields for optimistic locking
    - Implement `Campanha.overlaps(Campanha other)`: returns true if same type, same target, and date ranges overlap
    - Implement `Cupom.isValidAt(Instant now)`: checks `active`, date range, and `usageCount < maxUsages`
    - _Requirements: 8.1, 8.2, 8.4, 8.5_

  - [x] 8.2 Implement Pricing inbound ports and use case implementations
    - Define `CreateCampaignUseCase`, `CreateCouponUseCase`, `CalculateDiscountUseCase`, `ConfirmCouponUsageUseCase` in `domain.port.in`
    - Implement `CreateCampaignUseCaseImpl`: check for conflicting active campaigns before saving; throw `ConflictException("Conflito de campanha")` if overlap found
    - Implement `CalculateDiscountUseCaseImpl`: apply discounts in priority order (coupon → percentage campaign → fixed campaign); ensure combined discount ≤ subtotal; calculate cashback; do NOT increment usage counter
    - Implement `ConfirmCouponUsageUseCaseImpl`: atomically increment `usage_count` using optimistic lock (`version`); if `usageCount + 1 > maxUsages` throw `ValidationException` (HTTP 422)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8_

  - [x] 8.3 Implement Pricing persistence adapters
    - Create JPA entities `CampanhaJpaEntity`, `CupomJpaEntity` with `@Version` on Cupom for optimistic lock
    - Implement `CampanhaRepository` (find active overlapping by type+target+dateRange) and `CupomRepository` outbound port interfaces
    - _Requirements: 8.4, 8.7_

  - [x] 8.4 Implement Pricing REST adapter
    - Create `PricingController` at `/api/v1/pricing` with all six endpoints from API design
    - Apply `@PreAuthorize`: campaign/coupon management = `MANAGER`; `POST /calculate` = `CASHIER or MANAGER`; `POST /coupons/{code}/confirm` = `CASHIER or MANAGER`
    - _Requirements: 2.5, 2.6, 8.1–8.8_

  - [x]* 8.5 Write property test: Combined discount never exceeds sale total (Property 13)
    - **Property 13: Combined discount never exceeds sale total**
    - For any sale total `T` and any combination of applicable discount rules generated by jqwik, assert `0 ≤ D ≤ T` and `T - D ≥ 0`
    - **Validates: Requirements 8.8**

  - [x]* 8.6 Write property test: Coupon usage count never exceeds maximum limit (Property 14)
    - **Property 14: Coupon usage count never exceeds its maximum limit**
    - Simulate concurrent confirmation attempts using jqwik (repeated calls) for a coupon with `maxUsages = N`; assert `usageCount ≤ N` after all attempts; confirm HTTP 422 is returned on the attempt that would exceed `N`
    - **Validates: Requirements 8.7**

- [x] 9. Implement Sales module
  - [x] 9.1 Implement Sales domain model
    - Create `Venda` aggregate root with `VendaStatus` enum (`EM_ANDAMENTO`, `FINALIZADA`, `CANCELADA`) and `PaymentMethod` enum (`DINHEIRO`, `DEBITO`, `CREDITO`, `PIX`)
    - Create `ItemVenda` entity owned by `Venda`
    - Implement `Venda.computeTotal(BigDecimal discountAmount, BigDecimal taxAmount)`: returns `subtotal - discountAmount + taxAmount`
    - Implement `Venda.computeChange(BigDecimal amountPaid)`: only for `DINHEIRO`; returns `amountPaid - total`
    - Implement `Venda.cancel(String reason, UUID operatorUuid)`: sets status to `CANCELADA`, stores reason
    - Define outbound ports `InventoryPort` and `PricingPort` in `shared-kernel` (cross-module boundaries)
    - _Requirements: 5.1, 5.5, 5.7, 5.8, 5.11, 5.12, 5.13_

  - [x] 9.2 Implement Sales inbound ports and use case implementations
    - Define `OpenSaleUseCase`, `AddItemUseCase`, `FinalizeSaleUseCase`, `CancelSaleUseCase` in `domain.port.in`
    - Implement `OpenSaleUseCaseImpl`: create `Venda` with status `EM_ANDAMENTO`, associate operator UUID, terminal ID, optional `clienteUuid`
    - Implement `AddItemUseCaseImpl`: look up variant by barcode/SKU via `SearchVariantUseCase` (cross-module via port); call `InventoryPort.reserve()` — if rejected, propagate `availableStock` in HTTP 422; add `ItemVenda` to sale
    - Implement `FinalizeSaleUseCaseImpl`:
      1. Call `PricingPort.calculateDiscount()` to get `discountAmount`
      2. Compute backend total
      3. Compare with `expectedTotal` from request; reject if mismatch (HTTP 422)
      4. For `DINHEIRO`: validate `amountPaid ≥ total`; compute `changeAmount`
      5. Call `PricingPort.confirmCouponUsage()` if coupon present
      6. Update `Venda` to `FINALIZADA`
      7. Emit `SaleCompletedEvent`
      8. Emit `PaymentApprovedEvent`
    - Implement `CancelSaleUseCaseImpl`: release all `ACTIVE` reservations for this sale via `InventoryPort.release()`, set status `CANCELADA`
    - _Requirements: 5.1–5.13_

  - [x] 9.3 Implement Sales persistence adapters
    - Create JPA entities `VendaJpaEntity`, `ItemVendaJpaEntity` in `adapter.out.persistence`
    - Implement `VendaRepository` outbound port: include paginated query by date range and by operator
    - Implement `InventoryPort` adapter: delegates to `ReserveStockUseCase`, `ReleaseReserveUseCase`, `CommitReserveUseCase` via Spring injection
    - Implement `PricingPort` adapter: delegates to `CalculateDiscountUseCase` and `ConfirmCouponUsageUseCase`
    - _Requirements: 5.1, 5.10, 5.13, 9.5, 9.6_

  - [x] 9.4 Implement Sales REST adapter
    - Create `SalesController` at `/api/v1/sales` with all seven endpoints from API design
    - Apply `@PreAuthorize`: write (open/add item/finalize/cancel) = `CASHIER or MANAGER`; read = `MANAGER or FINANCE`
    - _Requirements: 2.5, 2.6, 5.1–5.13_

  - [x]* 9.5 Write property test: Sale total computed on backend, never trusted from client (Property 9)
    - **Property 9: Sale total is always computed on the backend (never trusted from client)**
    - For any sale with any set of items and discount, generate arbitrary `expectedTotal` values that differ from backend total by any amount ≥ 0.01; assert HTTP 422 with `"Valor de total inválido"` for all mismatches
    - **Validates: Requirements 5.5, 5.6**

  - [x]* 9.6 Write property test: Cash payment change calculation is exact (Property 10)
    - **Property 10: Cash payment change calculation is exact**
    - For any `amountPaid ≥ total`, assert `changeAmount == amountPaid - total` with no rounding errors; for any `amountPaid < total`, assert HTTP 422 with exact total due
    - **Validates: Requirements 5.7, 5.8**

- [x] 10. Sprint 2 Checkpoint — Sales + Pricing functional, POS flow end-to-end
  - Run full integration test: open sale → add items (stock reserved) → apply coupon → finalize (stock committed, events emitted)
  - Ensure all tests pass, ask the user if questions arise.

---

## Sprint 3: Customers + Finance + Domain Events

- [x] 11. Write Flyway migrations V6–V8 for Customer, Finance, and event tables
  - [x] 11.1 Create `V6__create_clientes.sql`
    - `clientes` table with `cpf` unique constraint, `created_at` default
    - Indexes on `cpf` and `LOWER(full_name)`
    - _Requirements: 6.1, 9.6_

  - [x] 11.2 Create `V7__create_lancamentos_financeiros.sql`
    - `lancamentos_financeiros` table with `sale_uuid UNIQUE` for idempotency
    - Indexes on `competence_date` and `type`
    - _Requirements: 7.1, 9.6_

  - [x] 11.3 Create `V8__create_domain_events.sql`
    - `domain_events` table with partial index on `(status, next_retry_at) WHERE status IN ('PENDING','FAILED')`
    - _Requirements: 11.4, 11.5, 11.6_

- [x] 12. Implement Customer module
  - [x] 12.1 Implement Customer domain model and value objects
    - Create `Cliente` aggregate root with all fields from design
    - Create value object `Cpf(value)`: implement the two-pass check-digit algorithm (first and second verifier digits)
    - Create value object `Email(value)`: validate RFC 5321 format via regex
    - _Requirements: 6.1, 6.2_

  - [x] 12.2 Implement Customer inbound ports and use case implementations
    - Define `RegisterCustomerUseCase`, `DeactivateCustomerUseCase`, `SearchCustomerUseCase` in `domain.port.in`
    - Implement `RegisterCustomerUseCaseImpl`: validate CPF using `Cpf` value object, check uniqueness (active and inactive), throw `ValidationException("CPF já cadastrado")` without leaking existing data on duplicate
    - Implement `DeactivateCustomerUseCaseImpl`: set `active = false`; historical sales remain readable
    - Implement `SearchCustomerUseCaseImpl`: support search by exact CPF, partial name (min 3 chars), or UUID with pagination
    - _Requirements: 6.1, 6.2, 6.3, 6.5, 6.6, 6.7_

  - [x] 12.3 Implement Customer persistence adapters
    - Create JPA entity `ClienteJpaEntity`
    - Implement `ClienteRepository` outbound port: include custom queries for partial name search (`LOWER(full_name) LIKE LOWER(:name)%`)
    - _Requirements: 6.5_

  - [x] 12.4 Implement Customer REST adapter
    - Create `CustomerController` at `/api/v1/customers` with five endpoints from API design
    - Apply `@PreAuthorize`: write (register/update/deactivate) = `MANAGER`; read = `MANAGER or CASHIER`
    - Enforce `Sales_Service` rejection of inactive customer association (validate in `AddItemUseCase` / `OpenSaleUseCase` when `clienteUuid` is present)
    - _Requirements: 2.5, 6.1–6.7_

  - [x]* 12.5 Write property test: CPF validation is deterministic and algorithm-correct (Property 17)
    - **Property 17: CPF validation is deterministic and algorithm-correct**
    - For any string of exactly 11 digits generated by jqwik `@ForAll`, assert `Cpf.isValid(input)` returns the same result as an independent reference implementation of the Brazilian check-digit algorithm; verify the function is pure (same input → same output always)
    - **Validates: Requirements 6.2**

  - [ ]* 12.6 Write property test: Duplicate CPF registration never exposes existing customer data (Property 18)
    - **Property 18: Duplicate CPF registration never exposes existing customer data**
    - For any registration attempt with an already-existing CPF, assert HTTP 422 response body contains only `{"message": "CPF já cadastrado"}` and no UUID, name, email, phone, or birthDate fields from the existing record
    - **Validates: Requirements 6.3**

- [x] 13. Implement Finance module
  - [x] 13.1 Implement Finance domain model
    - Create `LancamentoFinanceiro` aggregate root with `EntryType` enum (`RECEITA`, `DESPESA`)
    - Define predefined `DESPESA` categories as enum or constant set: `ALUGUEL`, `SALARIOS`, `FORNECEDORES`, `MARKETING`, `MANUTENCAO`, `OUTROS`
    - _Requirements: 7.1_

  - [x] 13.2 Implement Finance inbound ports and use case implementations
    - Define `RegisterExpenseUseCase`, `GetCashFlowReportUseCase` in `domain.port.in`
    - Implement `RegisterExpenseUseCaseImpl`: validate amount ∈ [0.01, 999999999.99], description length ∈ [1, 255], category ∈ predefined set; throw `ValidationException` listing each failed constraint
    - Implement `GetCashFlowReportUseCaseImpl`:
      - Validate `from ≤ to` (throw `DateRangeException("Data de início deve ser anterior à data de fim")`)
      - Validate range ≤ 366 days (throw `DateRangeException("Intervalo máximo de 366 dias")`)
      - Aggregate RECEITA and DESPESA by calendar day; compute `netBalance`
    - _Requirements: 7.1–7.8_

  - [x] 13.3 Implement Finance event consumer for `SaleCompletedEvent`
    - Create `FinanceEventConsumer` in `adapter.out.event` annotated with `@EventHandler("SaleCompleted")`
    - Check idempotency key (`domain_events.event_id`) before processing; if already delivered, discard
    - Create `LancamentoFinanceiro` of type `RECEITA` using `sale_uuid` as idempotency key (UNIQUE DB constraint prevents duplicate insert)
    - Set `paymentMethod` and `amount` from event payload
    - _Requirements: 7.2, 7.3, 11.6_

  - [x] 13.4 Implement Finance persistence adapters
    - Create JPA entity `LancamentoFinanceiroJpaEntity` with unique constraint on `sale_uuid`
    - Implement `LancamentoRepository` outbound port: include aggregation query for cash flow (group by `competence_date`, sum by type)
    - _Requirements: 7.5, 7.8_

  - [x] 13.5 Implement Finance REST adapter
    - Create `FinanceController` at `/api/v1/finance` with three endpoints from API design
    - Apply `@PreAuthorize`: all endpoints = `MANAGER or FINANCE`
    - _Requirements: 2.5, 2.8, 7.1–7.9_

  - [x]* 13.6 Write property test: SaleCompletedEvent idempotency — exactly one RECEITA entry (Property 12)
    - **Property 12: SaleCompletedEvent idempotency — exactly one RECEITA entry**
    - For any `SaleCompletedEvent` with a given `saleUuid`, deliver the same event `N ≥ 1` times (Testcontainers); assert exactly one `LancamentoFinanceiro` of type `RECEITA` with that `saleUuid` exists in the database
    - **Validates: Requirements 7.2, 11.6**

  - [x]* 13.7 Write property test: Cash flow net balance arithmetic (Property 16)
    - **Property 16: Cash flow net balance equals sum of receitas minus sum of despesas**
    - For any date range `[from, to]` (from ≤ to, ≤ 366 days) generated by jqwik, with arbitrary RECEITA and DESPESA entries inserted, assert `netBalance == sum(RECEITA) - sum(DESPESA)` exactly using `BigDecimal` arithmetic
    - **Validates: Requirements 7.5, 7.8**

- [x] 14. Implement Domain Event Bus infrastructure
  - [x] 14.1 Implement `InProcessEventBus` and `EventHandlerRegistry`
    - Create `com.erp.infrastructure.eventbus.InProcessEventBus` Spring `@Component`
    - Implement `EventHandlerRegistry`: scan for all `@EventHandler`-annotated methods at startup, register in `Map<eventType, List<Handler>>`
    - Dispatch is synchronous within the same transaction by default; catch handler exceptions, persist event to `domain_events` with `status=FAILED`, schedule retry
    - Implement `DeadLetterStore`: persist events to `domain_events` with `status=DLQ` after 3 failed retries
    - _Requirements: 11.4, 11.5_

  - [x] 14.2 Implement retry scheduler with exponential backoff
    - Create `EventRetryScheduler` with `@Scheduled(fixedDelay = 10000)`: query `domain_events` where `status='FAILED'` and `next_retry_at <= NOW()`
    - Re-dispatch each event; on success mark `DELIVERED`; on failure: `retryCount++`, `nextRetry = now + 2^retryCount` seconds; after 3 retries mark `DLQ`
    - _Requirements: 11.5_

  - [x] 14.3 Implement event idempotency guard in all consumers
    - For every `@EventHandler` method, add idempotency check: `if (domainEventRepo.existsByEventId(envelope.eventId())) return;`
    - After successful processing: `domainEventRepo.markDelivered(envelope.eventId())`
    - Apply to `FinanceEventConsumer` and `InventoryEventConsumer` (for `SaleCompletedEvent` → commit reservations)
    - _Requirements: 11.6_

  - [x] 14.4 Implement `InventoryEventConsumer` for `SaleCompletedEvent`
    - Create consumer in `modules/inventory/adapter/out/event` annotated with `@EventHandler("SaleCompleted")`
    - On receiving `SaleCompletedEvent`: call `CommitReserveUseCase` for each item SKU+quantity, converting reservations to definitive withdrawals
    - Apply idempotency guard from task 14.3
    - _Requirements: 5.10, 11.6_

  - [x]* 14.5 Write property test: Domain event envelope is always structurally valid (Property 15)
    - **Property 15: Domain event envelope is always structurally valid**
    - For any triggering business operation (`SaleCompleted`, `StockReserved`, `PaymentApproved`) exercised by jqwik, assert the emitted `EventEnvelope` always has: non-null `eventId` (valid UUIDv4), correct `eventType` string, non-null `occurredAt` (ISO 8601), non-null `payload` with all required fields present and non-null
    - **Validates: Requirements 11.1, 11.2, 11.3**

- [x] 15. Sprint 3 Checkpoint — Customers + Finance + Domain Events functional
  - Run integration test: complete sale → `SaleCompletedEvent` emitted → `FinanceEventConsumer` creates `RECEITA` → `InventoryEventConsumer` commits reservations → re-deliver same event → no duplicate `RECEITA`
  - Ensure all tests pass, ask the user if questions arise.

---

## Sprint 4: Observability + Infrastructure + PBT Test Suite

- [x] 16. Implement Observability
  - [x] 16.1 Configure OpenTelemetry auto-instrumentation
    - Add `opentelemetry-spring-boot-starter` dependency to `infrastructure` module
    - Configure `OtelConfig` in `com.erp.infrastructure.observability`: set OTLP exporter endpoint from `OTEL_EXPORTER_OTLP_ENDPOINT` env var
    - Ensure `traceId` and `spanId` are populated in MDC for every inbound HTTP request
    - Configure W3C `traceparent` header propagation for all outgoing calls
    - _Requirements: 10.1_

  - [x] 16.2 Configure Prometheus metrics with Micrometer
    - Add `micrometer-registry-prometheus` to `infrastructure` module
    - Configure `application.yml` with percentile histograms for `http.server.requests` at p50/p95/p99 and expose `/actuator/prometheus`
    - Create `MetricsConfig` registering custom counters: `erp_sale_finalized_total` (label: `payment_method`), `erp_stock_reservation_total` (label: `status`), `erp_event_bus_dispatch_total` (labels: `event_type`, `status`), `erp_coupon_applied_total` (label: `coupon_code`)
    - Increment the appropriate counter in each use case implementation
    - _Requirements: 10.2_

  - [x] 16.3 Configure structured JSON logging with Logback + logstash-logback-encoder
    - Add `logstash-logback-encoder` dependency to `infrastructure`
    - Create `src/main/resources/logback-spring.xml` with `JSON_STDOUT` appender using `LogstashEncoder`; include `traceId`, `spanId`, `module` MDC keys; add custom fields `app` and `env`
    - Ensure every log entry at INFO/WARN/ERROR contains: `timestamp` (ISO 8601), `level`, `trace_id`, `module`, `message`
    - Configure `GlobalExceptionHandler` to log ERROR + full stack trace + `traceId` on all 5xx responses
    - _Requirements: 10.3, 10.4, 12.6_

  - [x] 16.4 Implement health check indicators
    - Create `DatabaseHealthIndicator`: runs `SELECT 1` with 1s connection timeout; returns `UP` or `DOWN`
    - Create `EventQueueHealthIndicator`: queries count of `domain_events` with `status='FAILED'`; returns `DOWN` if count > configurable threshold (default 100)
    - Expose `/actuator/health` (aggregated, with details) returning HTTP 200 when all UP, HTTP 503 when any DOWN
    - Expose `/actuator/health/readiness`: returns HTTP 200 `{"status": "UP"}` when DB available and Flyway migrations complete; HTTP 503 `{"status": "DOWN", "reason": "..."}` otherwise
    - _Requirements: 10.5, 12.4, 12.5_

- [x] 17. Finalize Docker Compose and Dockerfile
  - [x] 17.1 Write multi-stage `Dockerfile`
    - Stage 1 (`build`): `eclipse-temurin:21-jdk-alpine`, run `./mvnw -pl bootstrap -am package -DskipTests`
    - Stage 2 (`runtime`): `eclipse-temurin:21-jre-alpine`, copy `bootstrap/target/erp-bootstrap.jar` as `app.jar`, `EXPOSE 8080`, `ENTRYPOINT ["java","-jar","app.jar"]`
    - _Requirements: 12.1_

  - [x] 17.2 Write `docker-compose.yml` with full service topology
    - Services: `app` (with all required env vars from design), `postgres:16-alpine` (with healthcheck), `otel-collector`, `prometheus`, `loki`, `grafana`
    - `app` `depends_on: postgres: condition: service_healthy`
    - App healthcheck: `curl -f http://localhost:8080/actuator/health/readiness`
    - All sensitive values sourced from environment variables (no hardcoded secrets)
    - _Requirements: 12.1, 12.2_

  - [x] 17.3 Write `.env.example` and validate environment variable coverage
    - List all required environment variables: `DB_PASSWORD`, `JWT_SECRET`, `GRAFANA_PASSWORD`, `OTEL_EXPORTER_OTLP_ENDPOINT`, `INVENTORY_EXPIRY_TTL_MINUTES`
    - Add validation in `bootstrap` startup that fails fast with descriptive error if any required ENV var is missing
    - _Requirements: 12.2, 12.3_

- [x] 18. Implement ArchUnit module boundary enforcement
  - [x] 18.1 Create and run ArchUnit boundary tests
    - Add `archunit-junit5` to `bootstrap` test scope
    - Implement `ModuleBoundaryTest`: assert no class in `com.erp.modules.auth` imports from `com.erp.modules.product`, etc. (all lateral cross-module domain/application imports forbidden)
    - Assert all cross-module calls go through `shared-kernel` port interfaces or in-process events
    - _Requirements: 9.1 (architectural integrity)_

- [ ] 19. Complete PBT test suite — run all 19 properties
  - [ ] 19.1 Verify and run Properties 1–5 (Auth)
    - Confirm tests 3.5–3.9 are complete, run them with `./mvnw -pl modules/auth test`
    - Each test uses `@Property(tries = 1000)` and `@Label` matching design property number
    - **Properties 1, 2, 3, 4, 5 — Validates: Requirements 1.1, 1.2, 1.3, 1.5, 1.6, 1.7, 2.4**

  - [ ] 19.2 Verify and run Properties 6–8 + 19 (Inventory)
    - Confirm tests 5.6–5.9 are complete, run them with `./mvnw -pl modules/inventory test`
    - **Properties 6, 7, 8, 19 — Validates: Requirements 4.1, 4.2, 4.5, 4.6, 4.8, 4.10**

  - [ ] 19.3 Verify and run Property 11 (Product)
    - Confirm test 4.5 is complete, run with `./mvnw -pl modules/product test`
    - **Property 11 — Validates: Requirements 3.6**

  - [ ] 19.4 Verify and run Properties 9–10 (Sales)
    - Confirm tests 9.5–9.6 are complete, run with `./mvnw -pl modules/sales test`
    - **Properties 9, 10 — Validates: Requirements 5.5, 5.6, 5.7, 5.8**

  - [ ] 19.5 Verify and run Properties 13–14 (Pricing)
    - Confirm tests 8.5–8.6 are complete, run with `./mvnw -pl modules/pricing test`
    - **Properties 13, 14 — Validates: Requirements 8.7, 8.8**

  - [ ] 19.6 Verify and run Properties 17–18 (Customer)
    - Confirm tests 12.5–12.6 are complete, run with `./mvnw -pl modules/customer test`
    - **Properties 17, 18 — Validates: Requirements 6.2, 6.3**

  - [ ] 19.7 Verify and run Properties 12 + 16 (Finance)
    - Confirm tests 13.6–13.7 are complete, run with `./mvnw -pl modules/finance test`
    - **Properties 12, 16 — Validates: Requirements 7.2, 7.5, 7.8, 11.6**

  - [ ] 19.8 Verify and run Property 15 (Events)
    - Confirm test 14.5 is complete, run with `./mvnw -pl infrastructure test`
    - **Property 15 — Validates: Requirements 11.1, 11.2, 11.3**

- [x] 20. Final Checkpoint — All tests pass, full system integration verified
  - Run `./mvnw clean verify` across all modules (unit + property + integration tests)
  - Run `docker compose up --build` and verify: app starts, Flyway applies all migrations, `/actuator/health/readiness` returns HTTP 200, `/actuator/prometheus` returns metrics, structured JSON logs appear in stdout
  - Verify ArchUnit boundary tests pass
  - Ensure all tests pass, ask the user if questions arise.

---

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at each sprint boundary
- All PBT tasks use jqwik 1.8.x with `@Property(tries = 1000)` and `@Label("Feature: erp-loja-roupas, Property N: ...")`
- Pure-function properties (CPF, Money, stock invariant) run in-memory; persistence-level properties use Testcontainers PostgreSQL
- Cross-module communication MUST go through port interfaces in `shared-kernel` or in-process events — never direct domain/application class imports
- Environment variables must be validated at startup; no sensitive value hardcoded in source

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3", "1.4"] },
    { "id": 2, "tasks": ["1.5", "1.6", "1.7"] },
    { "id": 3, "tasks": ["3.1", "4.1", "5.1"] },
    { "id": 4, "tasks": ["3.2", "3.3", "4.2", "5.2", "5.3"] },
    { "id": 5, "tasks": ["3.4", "4.3", "5.4", "5.5"] },
    { "id": 6, "tasks": ["3.5", "3.6", "3.7", "3.8", "3.9", "4.4", "4.5", "5.6", "5.7", "5.8", "5.9"] },
    { "id": 7, "tasks": ["7.1", "7.2"] },
    { "id": 8, "tasks": ["8.1", "9.1"] },
    { "id": 9, "tasks": ["8.2", "8.3", "9.2"] },
    { "id": 10, "tasks": ["8.4", "9.3"] },
    { "id": 11, "tasks": ["8.5", "8.6", "9.4"] },
    { "id": 12, "tasks": ["9.5", "9.6"] },
    { "id": 13, "tasks": ["11.1", "11.2", "11.3"] },
    { "id": 14, "tasks": ["12.1", "13.1"] },
    { "id": 15, "tasks": ["12.2", "13.2", "14.1"] },
    { "id": 16, "tasks": ["12.3", "13.3", "14.2", "14.3"] },
    { "id": 17, "tasks": ["12.4", "13.4", "14.4"] },
    { "id": 18, "tasks": ["12.5", "12.6", "13.5", "13.6", "13.7", "14.5"] },
    { "id": 19, "tasks": ["16.1", "16.2", "16.3", "16.4"] },
    { "id": 20, "tasks": ["17.1", "17.2", "17.3", "18.1"] },
    { "id": 21, "tasks": ["19.1", "19.2", "19.3", "19.4", "19.5", "19.6", "19.7", "19.8"] }
  ]
}
```
