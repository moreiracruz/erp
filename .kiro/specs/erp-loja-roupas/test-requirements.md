# Requirements Document

## Introduction

This document specifies the requirements for the comprehensive test infrastructure of the ERP system for clothing retail stores. It addresses the test pyramid (unit, integration, component, end-to-end), security testing, concurrency verification, shared infrastructure concerns (Testcontainers, cleanup, Flyway, CI), observability of test execution, and specific pending tests that require database-backed property-based testing. This is an extension of the main requirements.md — it does not replace existing domain requirements but defines the testability infrastructure that validates them.

---

## Glossary

- **Test_Infrastructure**: The shared configuration, utilities, and conventions that enable all test layers to execute correctly
- **Unit_Test**: A test that exercises a single class or function in isolation, with all external dependencies mocked or stubbed
- **Integration_Test**: A test that exercises the persistence layer (JPA repositories) against a real PostgreSQL database via Testcontainers
- **Component_Test**: A test that boots a full Spring context for a single module with real database and mocked cross-module ports
- **E2E_Test**: A test that boots the full application context and exercises the REST API via MockMvc or WebTestClient
- **Property_Test**: A test using jqwik that generates randomized inputs to verify correctness invariants over many iterations
- **Testcontainers**: Library that manages Docker-based PostgreSQL instances for integration and higher-level tests
- **Test_Database**: A PostgreSQL instance managed by Testcontainers, ephemeral per test class or suite
- **Object_Mother**: A pattern for creating pre-configured test objects (builders/factories) to reduce test setup boilerplate
- **Surefire_Plugin**: Maven plugin that executes unit tests during the `test` phase
- **Failsafe_Plugin**: Maven plugin that executes integration/component/e2e tests during the `verify` phase
- **JaCoCo**: Java Code Coverage library used to measure test coverage per module and aggregated
- **DLQ**: Dead-Letter Queue — storage for domain events that failed processing after all retry attempts
- **Pessimistic_Lock**: Database-level SELECT FOR UPDATE lock used to prevent concurrent modifications to inventory
- **Optimistic_Lock**: Version-based concurrency control used on coupon usage counter
- **Truncate_Strategy**: Database cleanup approach that truncates all tables between tests to ensure isolation
- **CI_Pipeline**: Continuous Integration pipeline (GitHub Actions or equivalent) that runs all test suites on each commit

---

## Requirements

---

### Requirement 1: Test Pyramid Structure and Execution Separation

**User Story:** As a developer, I want the test suite to be organized into clearly separated layers (unit, integration, component, e2e) with independent execution, so that fast feedback is available during development and slow tests run only when needed.

#### Acceptance Criteria

1. THE Test_Infrastructure SHALL classify tests into four layers: Unit_Test (suffix `*Test.java` or `*Properties.java`), Integration_Test (suffix `*IT.java`), Component_Test (suffix `*CT.java`), and E2E_Test (suffix `*E2ETest.java`).
2. THE Surefire_Plugin SHALL execute only Unit_Test and Property_Test files (matching `**/*Test.java` and `**/*Properties.java`) during the Maven `test` phase; THE Surefire_Plugin SHALL exclude files matching `**/*IT.java`, `**/*CT.java`, and `**/*E2ETest.java`.
3. THE Failsafe_Plugin SHALL execute Integration_Test, Component_Test, and E2E_Test files (matching `**/*IT.java`, `**/*CT.java`, and `**/*E2ETest.java`) during the Maven `verify` phase.
4. WHEN the Maven `test` phase completes for all modules, THE Unit_Test suite SHALL finish execution within 30 seconds on a standard CI runner (4 vCPU, 8GB RAM).
5. WHEN the Maven `verify` phase executes Integration_Test files, THE Integration_Test suite SHALL finish execution within 2 minutes on a standard CI runner.
6. WHEN the Maven `verify` phase executes E2E_Test files, THE E2E_Test suite SHALL finish execution within 3 minutes on a standard CI runner.
7. THE Test_Infrastructure SHALL ensure that Unit_Test files do not depend on Spring context, Testcontainers, or any external service; IF a Unit_Test file imports `org.testcontainers` or `@SpringBootTest`, THEN the ArchUnit validation SHALL fail with a descriptive message.
8. THE CI_Pipeline SHALL execute the test layers in sequence: Unit_Test first, then Integration_Test, then Component_Test, then E2E_Test; IF any earlier layer fails, THEN the CI_Pipeline SHALL skip subsequent layers and report the failure.

---

### Requirement 2: Testcontainers PostgreSQL Shared Configuration

**User Story:** As a developer, I want a single shared Testcontainers PostgreSQL configuration reusable across all modules, so that integration tests use a real database without duplicating container setup.

#### Acceptance Criteria

1. THE Test_Infrastructure SHALL provide a shared abstract base class or configuration (e.g., `AbstractIntegrationTest`) in a `test-support` module that starts a single PostgreSQL Testcontainers instance per JVM using the singleton container pattern.
2. WHEN a Testcontainers-based test class starts, THE Test_Database SHALL use the same PostgreSQL version as production (PostgreSQL 16) to ensure behavioral parity.
3. THE Test_Infrastructure SHALL configure the shared container with the connection properties (`spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`) injected via `@DynamicPropertySource` or equivalent mechanism.
4. WHEN the Test_Database container starts for the first time in a JVM session, THE Test_Infrastructure SHALL apply all Flyway migrations from `bootstrap/src/main/resources/db/migration/` to bring the schema to the latest version before any test executes.
5. THE Test_Infrastructure SHALL reuse the single PostgreSQL container across all test classes within the same Maven module execution to minimize container startup overhead.
6. IF Docker is unavailable on the CI runner, THEN the Integration_Test, Component_Test, and E2E_Test suites SHALL be skipped with a clear warning message; Unit_Test execution SHALL NOT be affected.

---

### Requirement 3: Test Database Cleanup Between Tests

**User Story:** As a developer, I want the test database to be automatically cleaned between test executions, so that tests are fully isolated and do not depend on execution order.

#### Acceptance Criteria

1. WHEN a Testcontainers-based test method completes (regardless of pass or fail), THE Test_Infrastructure SHALL truncate all application tables and reset sequences to ensure no data leaks between tests.
2. THE Truncate_Strategy SHALL truncate tables in dependency order (respecting foreign key constraints) or use `TRUNCATE ... CASCADE` to avoid constraint violation errors during cleanup.
3. THE Truncate_Strategy SHALL NOT truncate the `flyway_schema_history` table, preserving migration state across tests within the same JVM session.
4. THE Test_Infrastructure SHALL implement the cleanup as a JUnit 5 extension or Spring `TestExecutionListener` that is automatically applied to all classes extending the shared base class.
5. WHEN a test method is annotated with `@Transactional` (for Spring-managed rollback), THE Test_Infrastructure SHALL allow the standard Spring rollback behavior to take precedence over truncation for that specific test.

---

### Requirement 4: Test Data Builders and Fixtures

**User Story:** As a developer, I want reusable test data builders (Object Mother pattern) for all domain entities, so that test setup is concise, readable, and maintainable.

#### Acceptance Criteria

1. THE Test_Infrastructure SHALL provide builder classes for each domain entity: `UsuarioTestBuilder`, `ProdutoTestBuilder`, `VarianteTestBuilder`, `EstoqueItemTestBuilder`, `VendaTestBuilder`, `ClienteTestBuilder`, `LancamentoTestBuilder`, `CampanhaTestBuilder`, and `CupomTestBuilder`.
2. WHEN a builder is instantiated with default values, THE builder SHALL produce a valid domain object that passes all domain validation rules without requiring any additional setter calls.
3. THE Test_Infrastructure SHALL provide builders for both domain objects (in-memory) and JPA entities (persistence layer), with a clear naming convention: `*TestBuilder` for domain objects and `*JpaTestBuilder` for persistence entities.
4. WHEN a builder method is called with custom values, THE builder SHALL override only the specified attributes while keeping all other attributes at their valid defaults (fluent API pattern).
5. THE Test_Infrastructure SHALL provide jqwik `Arbitrary` providers for each domain entity (e.g., `UsuarioArbitraries`, `VendaArbitraries`) that generate randomized but valid instances for property-based testing.
6. THE Test_Infrastructure SHALL locate all builder classes in a shared `test-support` module accessible to all other modules as a test-scoped dependency.

---

### Requirement 5: Spring Context Isolation for Component Tests

**User Story:** As a developer, I want each module's component tests to run with an isolated Spring context that does not load other modules' beans, so that tests are fast, focused, and free from cross-module pollution.

#### Acceptance Criteria

1. WHEN a Component_Test boots the Spring context, THE Test_Infrastructure SHALL load only the beans belonging to the module under test plus the shared infrastructure beans (datasource, Flyway, security filters).
2. THE Test_Infrastructure SHALL provide mock implementations (via `@MockBean` or dedicated test doubles) for all cross-module ports (`InventoryPort`, `PricingPort`) within a Component_Test context so that the module under test is isolated.
3. WHEN two Component_Test classes from different modules execute in the same Maven run, THE Test_Infrastructure SHALL ensure they use separate Spring contexts (via `@DirtiesContext` or context caching with distinct configurations) to prevent bean definition conflicts.
4. THE Component_Test context SHALL NOT load REST controllers from other modules; only the controllers of the module under test SHALL be present in the context.
5. IF a Component_Test inadvertently imports beans from another module's domain or application package, THEN an ArchUnit rule SHALL detect and fail the build with a message identifying the violating import.

---

### Requirement 6: End-to-End REST API Testing

**User Story:** As a developer, I want end-to-end tests that exercise the full REST API with a real database and all modules loaded, so that the complete request flow is validated from HTTP request to database persistence.

#### Acceptance Criteria

1. WHEN an E2E_Test boots the Spring context, THE Test_Infrastructure SHALL load the full application context (all modules, infrastructure, security) with a real Testcontainers PostgreSQL database.
2. THE E2E_Test suite SHALL use MockMvc or WebTestClient to send HTTP requests to the application, including proper `Authorization: Bearer <jwt>` headers for authenticated endpoints.
3. THE Test_Infrastructure SHALL provide a helper utility (`TestJwtGenerator`) that generates valid JWT tokens for any role (ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, ROLE_FINANCE) for use in E2E_Test assertions.
4. WHEN an E2E_Test verifies the POS flow, THE E2E_Test SHALL execute the complete sequence: authenticate → open sale → add items → finalize → verify SaleCompletedEvent side-effects (finance entry created, stock committed).
5. THE E2E_Test suite SHALL verify HTTP response status codes, response body structure (JSON paths), and database state after each operation.
6. WHEN an E2E_Test fails, THE test report SHALL include the full HTTP request (method, path, headers, body) and response (status, headers, body) for debugging purposes.

---

### Requirement 7: JWT and Authentication Security Testing

**User Story:** As a security engineer, I want dedicated tests that verify JWT token generation, validation, expiry, and rotation correctness, so that authentication cannot be bypassed or abused.

#### Acceptance Criteria

1. WHEN a valid login request is processed, THE Auth security tests SHALL verify that the returned JWT contains the correct claims: `sub` (user UUID), `role` (one of ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, ROLE_FINANCE), and `exp` (exactly 15 minutes from issuance in Unix epoch seconds).
2. WHEN a JWT is tampered with (modified payload or signature), THE Auth security tests SHALL verify that the System returns HTTP 401 and does not process the request.
3. WHEN a JWT with an expired `exp` claim is presented without a valid Refresh_Token, THE Auth security tests SHALL verify that the System returns HTTP 401.
4. WHEN a Refresh_Token is used to obtain a new JWT, THE Auth security tests SHALL verify that the old Refresh_Token is immediately invalidated and cannot be reused (rotation correctness).
5. FOR ALL generated JWT tokens (property test), THE Auth security tests SHALL verify that `decode(token).sub` equals the original user UUID and `decode(token).role` equals the assigned role (round-trip property).
6. WHEN a user performs logout, THE Auth security tests SHALL verify that subsequent requests with the revoked Refresh_Token return HTTP 401, regardless of the token's remaining validity period.
7. THE Auth security tests SHALL verify that no JWT is issued with an empty or null role claim, and that the System rejects any JWT bearing a role value not in the defined set {ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, ROLE_FINANCE}.

---

### Requirement 8: RBAC Enforcement Testing

**User Story:** As a security engineer, I want every protected endpoint to be tested with both authorized and unauthorized roles, so that access control cannot be bypassed through misconfiguration.

#### Acceptance Criteria

1. FOR EACH protected API endpoint, THE RBAC tests SHALL verify that a request with a JWT bearing an authorized role returns a success status (2xx) when the request payload is valid.
2. FOR EACH protected API endpoint, THE RBAC tests SHALL verify that a request with a JWT bearing an unauthorized role returns HTTP 403 with body containing `error: "Acesso negado"`.
3. FOR EACH protected API endpoint, THE RBAC tests SHALL verify that a request without any JWT (unauthenticated) returns HTTP 401.
4. THE RBAC tests SHALL be implemented as a parameterized test matrix: endpoint × role × expected outcome, covering all combinations defined in Requirements Requirement 2 (main requirements.md).
5. WHEN a new endpoint is added to the system without a corresponding RBAC test entry in the matrix, THE ArchUnit validation SHALL detect the untested endpoint and fail the build.
6. THE RBAC tests SHALL verify that ROLE_MANAGER has access to all module endpoints (read and write) as defined in the role permissions matrix.

---

### Requirement 9: Brute-Force Lockout Verification

**User Story:** As a security engineer, I want property-based tests that verify the brute-force lockout mechanism under varied inputs, so that the account lockout policy cannot be circumvented.

#### Acceptance Criteria

1. WHEN a user accumulates more than 5 consecutive failed login attempts within a 15-minute window, THE lockout property tests SHALL verify that subsequent login attempts (even with correct credentials) are rejected for 15 minutes.
2. FOR ALL sequences of N failed attempts (where N > 5) followed by a correct attempt within the lockout window (property test with real database), THE lockout tests SHALL verify that the correct attempt is still rejected.
3. WHEN the lockout period elapses, THE lockout tests SHALL verify that a valid login attempt succeeds and the failed-attempt counter resets to zero.
4. FOR ALL sequences of N failed attempts (where N ≤ 5) followed by a successful login (property test), THE lockout tests SHALL verify that the counter resets to zero and no lockout is applied.
5. THE lockout property tests SHALL use Testcontainers PostgreSQL to verify the persistence of the `failed_attempts` and `locked_until` fields across multiple service calls.
6. WHEN the lockout test generates randomized usernames and attempt sequences (jqwik), THE test SHALL record the jqwik seed for reproducibility.

---

### Requirement 10: Concurrency and Pessimistic Lock Verification

**User Story:** As a developer, I want tests that verify inventory pessimistic locking under concurrent access, so that overselling is impossible even under race conditions.

#### Acceptance Criteria

1. WHEN multiple threads (minimum 10) simultaneously request stock reservation for the same Variante with Estoque_Disponível of N units, THE concurrency tests SHALL verify that the total reserved quantity across all successful reservations never exceeds N.
2. FOR ALL concurrent reservation scenarios (property test with varying thread counts 2–20 and stock levels 1–100), THE concurrency tests SHALL verify the invariant: `sum(successful_reservations) ≤ initial_available_stock`.
3. WHEN concurrent threads attempt to reserve stock that would result in negative Estoque_Disponível, THE concurrency tests SHALL verify that exactly the correct number of requests are rejected (total_requested - available_stock requests fail).
4. THE concurrency tests SHALL use Testcontainers PostgreSQL with real SELECT FOR UPDATE semantics to validate that the pessimistic lock prevents data corruption.
5. THE concurrency tests SHALL complete within 30 seconds per test method, using a `CountDownLatch` or equivalent synchronization primitive to ensure all threads start simultaneously.
6. WHEN a concurrent reservation test completes, THE test SHALL verify the database state: `physical_stock` unchanged, `reserved_stock` equals the sum of successful reservations, and `physical_stock - reserved_stock ≥ 0`.

---

### Requirement 11: Coupon Optimistic Lock Under Race Conditions

**User Story:** As a developer, I want tests that verify coupon usage confirmation under concurrent access, so that the maximum usage limit is never exceeded even with simultaneous coupon applications.

#### Acceptance Criteria

1. WHEN multiple threads (minimum 10) simultaneously confirm usage of the same coupon with `max_usages` of M, THE optimistic lock tests SHALL verify that the final `usage_count` never exceeds M.
2. FOR ALL concurrent coupon confirmation scenarios (property test with varying thread counts 2–20 and max_usages 1–50), THE tests SHALL verify the invariant: `final_usage_count ≤ max_usages`.
3. WHEN an optimistic lock conflict occurs during coupon confirmation (version mismatch), THE tests SHALL verify that the conflicting request receives an HTTP 422 response and does not increment the counter.
4. THE optimistic lock tests SHALL use Testcontainers PostgreSQL with the `version` column-based optimistic locking to ensure the database-level behavior matches application expectations.
5. WHEN all threads have completed, THE test SHALL query the database directly and assert that `usage_count` equals the number of successful confirmations (no phantom increments or missed decrements).

---

### Requirement 12: Domain Event Idempotency Verification

**User Story:** As a developer, I want tests that verify domain event consumers are idempotent, so that at-least-once delivery does not cause duplicate side-effects.

#### Acceptance Criteria

1. FOR ALL domain events delivered N times (where N is 1–10, property test), THE idempotency tests SHALL verify that the consumer produces exactly 1 side-effect regardless of the delivery count.
2. WHEN the SaleCompletedEvent is delivered to the Finance_Service multiple times with the same `eventId`, THE idempotency tests SHALL verify that exactly one RECEITA entry exists in the `lancamentos_financeiros` table with the corresponding `sale_uuid`.
3. WHEN the SaleCompletedEvent is delivered to the Inventory_Service multiple times with the same `eventId`, THE idempotency tests SHALL verify that stock is committed exactly once (no double withdrawal).
4. THE idempotency tests SHALL use Testcontainers PostgreSQL to verify the database-level idempotency guarantees (UNIQUE constraint on `sale_uuid` in `lancamentos_financeiros`, event deduplication via `event_id`).
5. FOR ALL event payloads generated by jqwik (varying sale amounts, item counts, payment methods), THE idempotency tests SHALL verify the invariant: `count(side_effects) = 1` after N deliveries.
6. WHEN an event is moved to the DLQ after 3 failed retries, THE idempotency tests SHALL verify that the original event envelope and failure reason are preserved in the `domain_events` table with status `DLQ`.

---

### Requirement 13: Stock Invariant Under Concurrent Operations

**User Story:** As a developer, I want property-based tests that verify stock invariants hold under any sequence of concurrent operations, so that inventory data integrity is mathematically guaranteed.

#### Acceptance Criteria

1. FOR ALL sequences of stock operations (entries, withdrawals, reservations, releases) applied concurrently (property test), THE stock invariant tests SHALL verify: `available_stock = physical_stock - reserved_stock` at all times.
2. FOR ALL sequences of stock operations (property test), THE stock invariant tests SHALL verify: `physical_stock ≥ 0`, `reserved_stock ≥ 0`, and `available_stock ≥ 0` after every operation.
3. WHEN a mix of entry operations (+) and reservation operations (-) are applied to the same Variante concurrently, THE tests SHALL verify that the final `physical_stock` equals the initial value plus the sum of all successful entries minus the sum of all successful committed withdrawals.
4. THE stock invariant property tests SHALL generate operation sequences of length 5–50 with randomized quantities (1–100 per operation) and randomized operation types using jqwik.
5. THE stock invariant tests SHALL use Testcontainers PostgreSQL with real transaction isolation to validate that database constraints (`chk_available_non_negative`) are enforced under concurrency.

---

### Requirement 14: Customer CPF Data Privacy Testing

**User Story:** As a security engineer, I want tests that verify no customer data (CPF, name, email) is ever leaked in error responses, so that privacy regulations are maintained.

#### Acceptance Criteria

1. WHEN a duplicate CPF is submitted during customer registration, THE privacy tests SHALL verify that the error response contains only the message "CPF já cadastrado" and does NOT contain the existing customer's UUID, name, email, phone, or any partial CPF.
2. FOR ALL invalid customer registration attempts (property test with randomized invalid inputs), THE privacy tests SHALL verify that error responses do not contain any data from existing customers in the database.
3. WHEN a customer search returns no results, THE privacy tests SHALL verify that the response does not reveal whether the searched CPF exists in an inactive state.
4. THE privacy tests SHALL scan all HTTP error responses (4xx) for patterns matching CPF format (11 consecutive digits or formatted XXX.XXX.XXX-XX) and fail if any are found outside of authorized success responses.
5. FOR ALL authentication error responses (property test), THE privacy tests SHALL verify that the response contains only "Credenciais inválidas" and does not indicate whether the username exists or the password is incorrect.

---

### Requirement 15: Full POS Flow Integration Test

**User Story:** As a QA engineer, I want an integration test that exercises the complete Point of Sale flow from sale opening to financial entry creation, so that the entire business flow is validated end-to-end.

#### Acceptance Criteria

1. WHEN the POS flow integration test executes, THE test SHALL perform the complete sequence: (1) authenticate as ROLE_CASHIER, (2) open a new sale, (3) add 1–5 items with stock reservation, (4) apply a coupon (optional), (5) finalize the sale with a payment method, (6) verify SaleCompletedEvent emission, (7) verify Finance_Service received the event and created a RECEITA entry, (8) verify Inventory_Service committed all reservations.
2. WHEN the POS flow test adds items to the sale, THE test SHALL verify that each item's stock reservation is persisted in the `reservas_estoque` table with status `ACTIVE` and correct `expires_at` timestamp.
3. WHEN the POS flow test finalizes the sale, THE test SHALL verify: the sale record has status `FINALIZADA`, the `lancamentos_financeiros` table contains exactly one RECEITA entry with the sale's total value, and all reservations have status `COMMITTED`.
4. WHEN the POS flow test uses cash payment with overpayment, THE test SHALL verify that the `change_amount` (troco) is correctly calculated as `payment_amount - total`.
5. THE POS flow integration test SHALL use Testcontainers PostgreSQL and a full Spring context, executing the real domain event bus (not mocked) to validate event-driven side-effects.
6. WHEN the POS flow test completes, THE test SHALL verify the aggregate financial state: total RECEITA for the day equals the sum of all finalized sale totals.

---

### Requirement 16: Test Observability and Reporting

**User Story:** As a tech lead, I want clear test naming, separated reports, coverage metrics, and reproducible property test seeds, so that test failures are quickly diagnosed and coverage gaps are identified.

#### Acceptance Criteria

1. THE Test_Infrastructure SHALL enforce a naming convention where every test method has a `@DisplayName` (JUnit 5) or `@Label` (jqwik) annotation that describes the business scenario in natural language.
2. THE Test_Infrastructure SHALL configure Surefire reports in `target/surefire-reports/` for unit tests and Failsafe reports in `target/failsafe-reports/` for integration/component/e2e tests, ensuring separate XML and HTML report outputs.
3. THE Test_Infrastructure SHALL configure JaCoCo to generate per-module coverage reports and an aggregate coverage report in the `bootstrap` module combining all module coverage data.
4. WHEN a jqwik property test fails, THE test report SHALL include the random seed used for the failing run so that the failure can be reproduced deterministically by setting the seed in subsequent executions.
5. THE Test_Infrastructure SHALL configure jqwik with a fixed database (`.jqwik-database` file per module) to store shrunk failing examples for regression testing.
6. WHEN a test class contains more than 20 test methods, THE ArchUnit validation SHALL emit a warning suggesting the class be split for maintainability.
7. THE JaCoCo aggregate report SHALL exclude test support classes, builders, and configuration from coverage calculation.

---

### Requirement 17: CI-Friendly Test Execution

**User Story:** As a DevOps engineer, I want the test suite to be fully executable in CI without special Docker daemon configuration for unit tests, so that the feedback loop is fast and reliable.

#### Acceptance Criteria

1. THE Unit_Test suite SHALL execute without Docker, Testcontainers, or any external service dependency; all external collaborators SHALL be mocked or stubbed at the unit test level.
2. WHEN the CI_Pipeline executes the `test` phase, THE pipeline SHALL NOT require Docker daemon access; only the `verify` phase (integration and above) SHALL require Docker.
3. THE Test_Infrastructure SHALL configure Testcontainers to use the `reuse` feature (`testcontainers.reuse.enable=true`) in local development to avoid restarting containers between test runs during development.
4. THE CI_Pipeline SHALL set Testcontainers `reuse` to `false` in CI environments to ensure clean state for every pipeline execution.
5. WHEN a Testcontainers-based test detects that Docker is unavailable, THE test SHALL be skipped (not failed) with a JUnit 5 `@EnabledIf` condition or equivalent mechanism, and a clear log message SHALL indicate the reason.
6. THE CI_Pipeline SHALL cache Maven dependencies and Docker images between runs to minimize pipeline execution time.
7. THE Test_Infrastructure SHALL provide a Maven profile (`-Pfast-tests`) that executes only unit tests and skips all Testcontainers-dependent tests for rapid local feedback.

---

### Requirement 18: Flyway Migrations in Test Context

**User Story:** As a developer, I want Flyway migrations to be automatically applied in the test database, so that tests always run against the same schema as production.

#### Acceptance Criteria

1. WHEN the Test_Database container starts, THE Test_Infrastructure SHALL configure Spring Boot to apply all Flyway migrations from the production migration folder (`db/migration/`) before any test executes.
2. THE Test_Infrastructure SHALL NOT use separate migration scripts for test environments; THE same migration scripts used in production SHALL be applied to ensure schema parity.
3. IF a new Flyway migration is added to the production set, THE Test_Database SHALL automatically pick it up on the next test execution without manual configuration.
4. WHEN a Flyway migration fails during test database initialization, THE test execution SHALL halt immediately with a clear error message identifying the failing migration version and SQL error.
5. THE Test_Infrastructure SHALL configure Flyway with `flyway.clean-disabled=false` only in test profile, allowing programmatic schema cleanup when needed for edge cases.

---

### Requirement 19: Specific Pending Property Tests (Database-Backed)

**User Story:** As a developer, I want to enable the specific property tests that require a real database (auth lockout, idempotency, concurrency), so that the 12 existing in-memory tests are complemented by database-backed verification.

#### Acceptance Criteria

1. THE Test_Infrastructure SHALL provide a base class `AbstractDatabasePropertyTest` that extends the shared Testcontainers configuration and integrates with jqwik lifecycle hooks for database cleanup between property attempts.
2. WHEN a jqwik property test annotated with `@Property` extends `AbstractDatabasePropertyTest`, THE test SHALL receive a clean database state (all tables truncated) before each property trial (each input combination).
3. THE Test_Infrastructure SHALL enable the following pending property tests: Auth Property 1 (JWT claims correctness), Auth Property 2 (Refresh_Token rotation), Auth Property 3 (lockout after N>5 failures), Auth Property 4 (lockout reset on success), Auth Property 5 (role enforcement across endpoints).
4. THE Test_Infrastructure SHALL enable Customer Property 18 (CPF duplicate registration never exposes existing customer data in error response).
5. THE Test_Infrastructure SHALL enable Finance Property 12 (SaleCompletedEvent idempotency with real database — N deliveries produce exactly 1 RECEITA entry).
6. THE Test_Infrastructure SHALL enable the Inventory concurrent reservation property test (N threads reserving stock with pessimistic lock verification).
7. FOR ALL database-backed property tests, THE Test_Infrastructure SHALL configure jqwik with `tries = 100` as the default and allow per-test override via `@Property(tries = N)`.
8. WHEN a database-backed property test fails, THE jqwik shrinking process SHALL preserve the smallest failing example and the random seed in the `.jqwik-database` file for deterministic replay.

