# Requirements Document

## Introduction

This document describes the functional and non-functional requirements for the ERP system for managing small and medium-sized clothing retail stores. The system covers the Authentication, Products/Inventory, Sales (POS), Customers, Finance, and Pricing/Promotions modules, built on Java 21, Spring Boot 3, PostgreSQL, and Clean/Hexagonal architecture with a cloud-first vision on AWS.

---

## Glossary

- **System**: The ERP system as a whole
- **Auth_Service**: Module responsible for authentication and authorization
- **Product_Service**: Module responsible for product and variant registration
- **Inventory_Service**: Module responsible for inventory control
- **Sales_Service**: Module responsible for Point of Sale (POS) and sales recording
- **Finance_Service**: Module responsible for financial control
- **Customer_Service**: Module responsible for customer registration and management
- **Pricing_Service**: Module responsible for promotions, discounts, campaigns, cashback, and coupons
- **PDV**: Point of Sale — terminal for registering and finalizing sales in the physical store
- **Produto**: Entity representing a sellable item, with attributes such as name, brand, and category
- **Variante**: Specific combination of size, color, and SKU of a Produto
- **SKU**: Stock Keeping Unit — unique identification code for a Variante
- **Estoque_Físico**: Total quantity of units physically present in inventory
- **Estoque_Reservado**: Quantity of units blocked for orders in processing
- **Estoque_Disponível**: Quantity available for sale (Estoque_Físico − Estoque_Reservado)
- **SaleCompletedEvent**: Domain event emitted after successful sale completion
- **StockReservedEvent**: Domain event emitted after inventory reservation for an order
- **PaymentApprovedEvent**: Domain event emitted after payment approval
- **RBAC**: Role-Based Access Control
- **ROLE_MANAGER**: Role with full administrative access to the system
- **ROLE_CASHIER**: Role with access to the POS and sales module
- **ROLE_STOCK**: Role with access to the inventory module
- **ROLE_FINANCE**: Role with access to the financial module
- **JWT**: JSON Web Token — stateless authentication token
- **Refresh_Token**: Long-duration token used to renew an expired JWT
- **Flyway**: Versioned database migration tool
- **UUID**: Universally unique identifier — used as a public key in APIs

---

## Requirements

---

### Requirement 1: User Authentication

**User Story:** As a store operator, I want to authenticate in the system with secure credentials, so that I can access only the resources allowed for my role.

#### Acceptance Criteria

1. WHEN a user submits valid login credentials, THE Auth_Service SHALL return a JWT with 15-minute validity and a Refresh_Token with 7-day validity.
2. WHEN a user submits invalid login credentials, THE Auth_Service SHALL return HTTP 401 with the generic error message "Credenciais inválidas" without indicating which specific field is incorrect.
3. WHEN an expired JWT is received together with a valid Refresh_Token, THE Auth_Service SHALL issue a new JWT and immediately invalidate the presented Refresh_Token so it cannot be reused.
4. WHEN an expired or invalid Refresh_Token is presented, THE Auth_Service SHALL return HTTP 401 and require a new full login.
5. THE Auth_Service SHALL include in the JWT payload the user identifier (UUID), the role (exactly one of ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, ROLE_FINANCE), and the expiration timestamp (exp claim in Unix epoch seconds).
6. WHEN a user performs logout, THE Auth_Service SHALL invalidate the Refresh_Token associated with the session; subsequent use of that Refresh_Token SHALL return HTTP 401 regardless of its remaining validity period.
7. IF a user accumulates more than 5 consecutive failed login attempts within a 15-minute window, THEN THE Auth_Service SHALL reject all further login attempts for that username for 15 minutes and record a security event log entry containing the username, attempt count, and timestamp.
8. WHEN a user successfully authenticates after a lockout period has elapsed, THE Auth_Service SHALL reset the failed-attempt counter for that username to zero.

---

### Requirement 2: Role-Based Access Control (RBAC)

**User Story:** As a store manager, I want each employee to access only the features of their role, so that sensitive data is protected against unauthorized access.

#### Acceptance Criteria

1. THE System SHALL enforce access control on every API endpoint by validating that the authenticated user's role is in the authorized role set defined for that resource.
2. WHEN an authenticated request accesses a resource not permitted for the user's role, THE System SHALL return HTTP 403 with a body containing the fields `error: "Acesso negado"` and `requiredRoles` listing the roles that are allowed.
3. WHEN an unauthenticated request (no JWT or expired JWT without valid Refresh_Token) accesses any protected endpoint, THE System SHALL return HTTP 401.
4. THE System SHALL recognize exactly four valid roles: ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, and ROLE_FINANCE; any JWT bearing a role value not in this set SHALL be rejected with HTTP 403.
5. WHILE a user holds the ROLE_MANAGER role, THE System SHALL grant read and write access to all modules: Auth, Product, Inventory, Sales, Finance, Customer, and Pricing.
6. WHILE a user holds the ROLE_CASHIER role, THE System SHALL grant write access to the Sales module and read-only access to the Product and Customer modules; access to Finance, Inventory write operations, and Pricing configuration SHALL be denied.
7. WHILE a user holds the ROLE_STOCK role, THE System SHALL grant write access to the Inventory module and read-only access to the Product module; access to Sales, Finance, Customer, and Pricing modules SHALL be denied.
8. WHILE a user holds the ROLE_FINANCE role, THE System SHALL grant write access to the Finance module and read-only access to financial reports; access to Sales write operations, Inventory, Product management, Customer, and Pricing modules SHALL be denied.

---

### Requirement 3: Product Registration

**User Story:** As a manager, I want to register products with their variants (size, color, SKU), so that the store catalog is kept up to date and consistent.

#### Acceptance Criteria

1. THE Product_Service SHALL store for each Produto the attributes: internal identifier (BIGSERIAL), public UUID, name (1–255 characters), brand (1–100 characters), category (1–100 characters), and active status.
2. THE Product_Service SHALL store for each Variante the attributes: internal identifier (BIGSERIAL), public UUID, reference to Produto, SKU (1–50 characters), size (1–50 characters), color (1–50 characters), barcode (8–14 digits), sale price (0.01–999,999.99), cost (0.01–999,999.99), and active status.
3. WHEN a new Produto is registered, THE Product_Service SHALL validate that name and category are provided and that the name is unique among active Produtos in the database (case-insensitive comparison).
4. WHEN a new Variante is registered, THE Product_Service SHALL validate that the SKU and barcode are each unique across all Variantes in the database regardless of active status.
5. IF a duplicate SKU or barcode is provided when registering a Variante, THEN THE Product_Service SHALL return HTTP 422 with a body indicating the conflicting field name and the existing Variante's UUID.
6. WHEN a Produto is deactivated, THE Product_Service SHALL set active status to false for all associated Variantes within the same transaction.
7. THE Product_Service SHALL expose a search endpoint for Variante by SKU and by barcode with response time under 200ms at the 95th percentile under normal load.
8. IF a Produto registration request omits the name or category field, THEN THE Product_Service SHALL return HTTP 422 with a body listing each missing required field.
9. WHEN a new Variante is registered, THE Product_Service SHALL verify that the referenced Produto exists and has active status true; IF the Produto does not exist or is inactive, THEN THE Product_Service SHALL return HTTP 422 with message "Produto inexistente ou inativo".

---

### Requirement 4: Inventory Management

**User Story:** As a stock manager, I want to view and move inventory for each variant, so that product availability is always accurate and overselling is prevented.

#### Acceptance Criteria

1. THE Inventory_Service SHALL maintain for each Variante the counters: Estoque_Físico (non-negative integer), Estoque_Reservado (non-negative integer), and Estoque_Disponível (non-negative integer).
2. THE Inventory_Service SHALL guarantee that Estoque_Disponível equals Estoque_Físico minus Estoque_Reservado at all times, enforced as a database constraint or equivalent atomic operation.
3. WHEN a stock entry is registered for a Variante, THE Inventory_Service SHALL atomically increment Estoque_Físico by the specified quantity (minimum 1, maximum 100,000 units per operation) and record the movement with operation type "ENTRADA", variant UUID, quantity, timestamp, and the UUID of the responsible user.
4. WHEN a stock withdrawal is registered for a Variante with Estoque_Físico greater than or equal to the requested quantity, THE Inventory_Service SHALL atomically decrement Estoque_Físico by the specified quantity and record the movement with operation type "SAÍDA", variant UUID, quantity, timestamp, and responsible user UUID.
5. IF a stock withdrawal would result in a negative Estoque_Físico, THEN THE Inventory_Service SHALL reject the operation and return HTTP 422 with the current Estoque_Físico value.
6. WHEN the Sales_Service requests a stock reservation for a Variante with Estoque_Disponível greater than or equal to the requested quantity, THE Inventory_Service SHALL atomically increment Estoque_Reservado by the requested quantity using a SELECT FOR UPDATE or equivalent pessimistic lock and emit the StockReservedEvent.
7. IF Estoque_Disponível is insufficient to fulfill a reservation, THEN THE Inventory_Service SHALL reject the reservation and return HTTP 422 with the current Estoque_Disponível value.
8. WHEN a sale is cancelled or a reservation expires (after a configurable timeout, default 30 minutes), THE Inventory_Service SHALL atomically decrement Estoque_Reservado by the reserved quantity and record the movement with operation type "LIBERAÇÃO_RESERVA", variant UUID, quantity, timestamp, and system as actor.
9. THE Inventory_Service SHALL record all inventory movements with operation type, variant UUID, quantity, timestamp, and user UUID (or "SYSTEM" for automated operations such as reservation releases).
10. IF an entry or withdrawal operation specifies a quantity less than 1 or greater than 100,000, THEN THE Inventory_Service SHALL return HTTP 422 with a validation message specifying the allowed range.

---

### Requirement 5: Point of Sale (POS)

**User Story:** As a cashier operator, I want to register sales quickly and securely, so that each transaction is processed correctly with total and change calculations.

#### Acceptance Criteria

1. WHEN an operator initiates a new sale at the POS, THE Sales_Service SHALL create a sale with status "EM_ANDAMENTO" associated with the authenticated operator's UUID and the current timestamp.
2. WHEN an item is added to the sale by barcode or SKU, THE Sales_Service SHALL request a stock reservation from the Inventory_Service for that Variante and only confirm the item addition after receiving a successful reservation confirmation.
3. IF a POS item addition request references a barcode or SKU that does not exist in the product catalog, THEN THE Sales_Service SHALL return HTTP 422 with message "Produto não encontrado".
4. IF the Inventory_Service rejects the reservation for an item being added to the sale, THEN THE Sales_Service SHALL reject the item addition and return to the operator the current Estoque_Disponível value returned by the Inventory_Service.
5. THE Sales_Service SHALL calculate on the backend the subtotal (sum of unit prices multiplied by quantities), applicable discounts (obtained exclusively from the Pricing_Service), taxes, and total for the sale.
6. IF the Sales_Service receives a finalization request containing a pre-calculated total that differs from the backend-computed total, THEN THE Sales_Service SHALL reject the request with HTTP 422 and message "Valor de total inválido".
7. WHEN the payment method is cash and the submitted payment amount is less than the backend-computed total, THE Sales_Service SHALL reject the payment with HTTP 422 and return the total due.
8. WHEN the payment method is cash and the submitted payment amount is greater than or equal to the backend-computed total, THE Sales_Service SHALL calculate the change (troco) as payment amount minus total and include it in the finalization response.
9. WHEN the sale is successfully finalized, THE Sales_Service SHALL emit the SaleCompletedEvent containing the sale UUID, operator UUID, list of items with SKU and quantity, total value, and payment method.
10. WHEN the SaleCompletedEvent is emitted, THE Inventory_Service SHALL convert each stock reservation into a definitive withdrawal for each item in the sale.
11. THE Sales_Service SHALL support the payment methods: cash (dinheiro), debit card (cartão de débito), credit card (cartão de crédito), and PIX.
12. WHEN a sale is cancelled before finalization, THE Sales_Service SHALL release all stock reservations associated with the sale and record the cancellation reason (1–255 characters) and operator UUID.
13. WHEN a sale is finalized, THE Sales_Service SHALL record the operator UUID, finalization datetime (ISO 8601), and terminal identifier associated with the sale.

---

### Requirement 6: Customer Management

**User Story:** As a manager, I want to register and manage customers, so that purchase history and contact data are available for analysis and loyalty programs.

#### Acceptance Criteria

1. THE Customer_Service SHALL store for each Cliente the attributes: public UUID, full name (1–255 characters), CPF (exactly 11 digits), email (valid RFC 5321 format, optional), phone (8–15 digits, optional), date of birth (optional), and active status.
2. WHEN a new Cliente is registered, THE Customer_Service SHALL validate that the CPF passes the Brazilian CPF check-digit algorithm and that no active or inactive Cliente with the same CPF already exists in the database.
3. IF a CPF that already exists in the database is provided during registration, THEN THE Customer_Service SHALL return HTTP 422 with message "CPF já cadastrado" without exposing the existing customer's UUID or other data.
4. WHEN a sale is associated with a Cliente, THE Sales_Service SHALL record the Cliente's UUID in the sale record; the association is optional (anonymous sales are permitted).
5. THE Customer_Service SHALL expose a search endpoint for Cliente by exact CPF, partial name (minimum 3 characters), and UUID, with response time under 300ms at the 95th percentile.
6. WHEN a Cliente is deactivated, THE Customer_Service SHALL set the active status to false; subsequent attempts to associate a new sale with that Cliente SHALL be rejected by the Sales_Service with HTTP 422 and message "Cliente inativo".
7. WHEN a Cliente is deactivated, THE System SHALL preserve all historical sale records associated with that Cliente as read-only, accessible via the sales history endpoint.

---

### Requirement 7: Financial Module

**User Story:** As a financial manager, I want to register and view revenues, expenses, and cash flow, so that the financial health of the store is tracked accurately.

#### Acceptance Criteria

1. THE Finance_Service SHALL define a financial entry as a record with the attributes: UUID, type (RECEITA or DESPESA), amount (positive decimal, 0.01–999,999,999.99), payment method (for RECEITA entries), description (1–255 characters), competence date, responsible user UUID, and reference to the originating sale UUID (nullable).
2. WHEN the SaleCompletedEvent is received, THE Finance_Service SHALL create a RECEITA financial entry using the sale UUID as idempotency key; IF a RECEITA entry with the same sale UUID already exists, THEN THE Finance_Service SHALL discard the duplicate event without creating a second entry.
3. WHEN a RECEITA entry is created from a SaleCompletedEvent, THE Finance_Service SHALL set the payment method field to the payment method from the event payload and the amount to the total sale value.
4. WHEN a manual expense is registered, THE Finance_Service SHALL validate that amount is between 0.01 and 999,999,999.99, description is between 1 and 255 characters, and category is one of the predefined expense categories; IF any validation fails, THEN THE Finance_Service SHALL return HTTP 422 listing each failed constraint.
5. WHEN a cash flow report is requested for a date range, THE Finance_Service SHALL return, grouped by calendar day within the range, the list of RECEITA entries, the list of DESPESA entries, and the daily balance (sum of RECEITA amounts minus sum of DESPESA amounts for that day).
6. IF the start date is after the end date in a cash flow query, THEN THE Finance_Service SHALL return HTTP 400 with message "Data de início deve ser anterior à data de fim".
7. IF the requested date range exceeds 366 days, THEN THE Finance_Service SHALL return HTTP 400 with message "Intervalo máximo de 366 dias".
8. WHEN the Finance_Service calculates the period balance, THE Finance_Service SHALL return the total RECEITA sum, total DESPESA sum, and net balance (RECEITA minus DESPESA) for the requested period.
9. IF a user without ROLE_FINANCE or ROLE_MANAGER attempts to create, edit, or delete a financial entry, THEN THE Finance_Service SHALL return HTTP 403.

---

### Requirement 8: Pricing, Promotions, and Coupons

**User Story:** As a manager, I want to configure promotions, discounts, campaigns, cashback, and coupons, so that commercial strategies are automatically applied to sales.

#### Acceptance Criteria

1. THE Pricing_Service SHALL support discount rule types: percentage discount (0.01%–100%) applied to the sale total, fixed value discount (0.01–999,999.99 BRL) applied to the sale total, and progressive discount (minimum quantity threshold of 2–999 items with an associated percentage or fixed discount).
2. WHEN a coupon is applied to a sale, THE Pricing_Service SHALL validate that the coupon code is active, the current datetime is within the coupon's start and end validity dates (inclusive), and the usage count has not reached the maximum usage limit.
3. IF an invalid, expired, or exhausted coupon is applied, THEN THE Pricing_Service SHALL return HTTP 422 with a body containing `reason` set to one of: "CUPOM_INATIVO", "CUPOM_EXPIRADO", or "CUPOM_ESGOTADO".
4. WHEN a new campaign is created, THE Pricing_Service SHALL verify that no other active campaign of the same type (percentage, fixed, or progressive) already targets the same Produto UUID or category value for an overlapping date range; IF a conflict exists, THEN THE Pricing_Service SHALL return HTTP 422 with message "Conflito de campanha".
5. WHEN cashback is configured for a campaign, THE Pricing_Service SHALL calculate the cashback value as `totalSaleValue × cashbackPercentage / 100`, where cashbackPercentage must be between 0.01 and 50.00.
6. WHEN the Sales_Service requests the discount calculation for a sale, THE Pricing_Service SHALL return the applicable discount amount based on active rules and the provided coupon code without incrementing the coupon usage counter or modifying any campaign state.
7. WHEN a sale is finalized and a coupon was used, THE Sales_Service SHALL call the Pricing_Service coupon confirmation endpoint, which SHALL atomically increment the coupon usage counter using an optimistic lock or database-level atomic increment; IF the increment would exceed the maximum limit (race condition), THE Pricing_Service SHALL return HTTP 422 and THE Sales_Service SHALL roll back the finalization.
8. WHEN multiple discount rules are applicable to the same sale, THE Pricing_Service SHALL apply them in the following priority order: coupon discount first, then campaign percentage discount, then campaign fixed discount; the combined discount SHALL NOT exceed the sale total.

---

### Requirement 9: Database Migrations

**User Story:** As a developer, I want all database schema changes to be versioned and automatically applied, so that the environment is reproducible and auditable.

#### Acceptance Criteria

1. THE System SHALL use Flyway to manage and apply all PostgreSQL database schema migrations on every application startup.
2. WHEN the System starts, THE System SHALL execute all pending Flyway migrations before the application begins accepting HTTP requests.
3. IF a Flyway migration fails during startup, THEN THE System SHALL halt the startup process and log an error entry containing the failed migration version number (e.g., V3), the migration script filename, and the underlying database error message before the process exits.
4. IF a Flyway checksum mismatch is detected for a previously applied migration script, THEN THE System SHALL halt the startup process and log an error entry identifying the affected migration version and the expected versus actual checksum values.
5. THE System SHALL use BIGSERIAL as the internal primary key and UUID (generated as UUIDv4) as the public identifier exposed in all APIs for the entities: Produto, Variante, Cliente, and Venda.
6. THE System SHALL maintain database indexes on: `sku` (on the variantes table), `codigo_barras` (on the variantes table), `data_venda` (on the vendas table), and `cliente_id` (on the vendas table).
7. IF the database is unavailable when Flyway attempts to execute migrations at startup, THEN THE System SHALL log an error entry with the connection failure reason and halt the startup process.

---

### Requirement 10: Observability and Monitoring

**User Story:** As a technical lead, I want the system to expose metrics, logs, and distributed tracing, so that failures and bottlenecks can be quickly identified.

#### Acceptance Criteria

1. WHEN an HTTP request is received, THE System SHALL attach a distributed tracing context via OpenTelemetry containing a `trace_id` and `span_id`, propagate these values to all downstream module calls within the same request, and include them in the response headers.
2. THE System SHALL expose metrics in Prometheus format at the `/actuator/prometheus` endpoint, including: request counter by HTTP method and endpoint path, response latency histograms per endpoint at p50/p95/p99, and error counter by HTTP status code class (4xx, 5xx).
3. THE System SHALL emit structured logs in JSON format for levels INFO, WARN, and ERROR to stdout, with each log entry containing the fields: `timestamp` (ISO 8601), `level`, `trace_id`, `module`, and `message`.
4. WHEN an HTTP 5xx error occurs, THE System SHALL log an ERROR-level entry containing the full stack trace and the `trace_id` of the originating request.
5. THE System SHALL expose a health check endpoint at `/actuator/health` that returns HTTP 200 with a JSON body listing the status (UP or DOWN) of each component: database, cache, and event queue; IF any component status is DOWN, THE System SHALL return HTTP 503; the health check response SHALL be returned within 2 seconds.

---

### Requirement 11: Domain Events

**User Story:** As a system architect, I want domain events to be emitted at key points in the business flow, so that modules can react in a decoupled manner without direct synchronous calls.

#### Acceptance Criteria

1. WHEN a sale is successfully completed, THE Sales_Service SHALL emit the SaleCompletedEvent with an envelope containing: `eventId` (UUIDv4), `eventType` ("SaleCompleted"), `occurredAt` (ISO 8601 timestamp), and `payload` with sale UUID, operator UUID, list of items (each with SKU and quantity), total value, and payment method.
2. WHEN a stock reservation is confirmed, THE Inventory_Service SHALL emit the StockReservedEvent with an envelope containing: `eventId` (UUIDv4), `eventType` ("StockReserved"), `occurredAt` (ISO 8601 timestamp), and `payload` with variant UUID, reserved quantity, and associated order/sale UUID.
3. WHEN a payment is approved by the payment processor, THE Sales_Service SHALL emit the PaymentApprovedEvent with an envelope containing: `eventId` (UUIDv4), `eventType` ("PaymentApproved"), `occurredAt` (ISO 8601 timestamp), and `payload` with sale UUID, approved amount, and payment method.
4. THE System SHALL guarantee at-least-once delivery for each domain event to all registered consumers.
5. IF the processing of a domain event fails, THEN THE System SHALL retry with exponential backoff starting at 1 second, doubling on each attempt, for up to 3 retry attempts before moving the event to a dead-letter queue, preserving the original event envelope and failure reason.
6. WHEN a consumer receives a domain event, THE consumer SHALL use the event's `eventId` as an idempotency key to detect and discard duplicate deliveries, ensuring that processing a given event more than once produces the same outcome as processing it once.

---

### Requirement 12: Infrastructure and Deployment

**User Story:** As an IT manager, I want the system to be packaged in Docker containers and deployable on AWS, so that the environment is reproducible and scalable.

#### Acceptance Criteria

1. THE System SHALL be packaged as a Docker image and orchestrated via Docker Compose for the development and MVP environments, exposing a single `docker-compose up` command to start all required services (application, PostgreSQL, Redis if applicable).
2. THE System SHALL source all sensitive configuration exclusively from environment variables, including database credentials, JWT signing keys, and external service endpoints; no sensitive values SHALL be hardcoded in source code or committed configuration files.
3. WHERE the environment is AWS ECS Fargate, THE System SHALL be deployable via ECS task definitions referencing the Docker image, without requiring any configuration specific to EC2 instance types or SSH access.
4. WHEN all Flyway migrations have been applied successfully and the database connection is available, THE System SHALL respond to a GET request to `/actuator/health/readiness` with HTTP 200 and body `{"status": "UP"}`.
5. IF the database connection is unavailable or Flyway migrations have not completed at the time of a readiness check, THEN THE System SHALL respond to GET `/actuator/health/readiness` with HTTP 503 and body `{"status": "DOWN", "reason": "<failure description>"}`.
6. THE System SHALL write all application logs as structured JSON to stdout and stderr, one JSON object per line, so that container log collectors (CloudWatch Logs, Loki) can capture them without any additional file-based log configuration.
