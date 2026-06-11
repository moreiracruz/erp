# Test Infrastructure Design Document — ERP Loja de Roupas

## Overview

This document specifies the technical design of the test infrastructure for the ERP clothing store system. It defines a new Maven module `shared/test-support` that provides reusable base classes, utilities, builders, and jqwik Arbitrary providers to support all test layers (unit, integration, component, E2E) across the modular monolith.

Key design goals:
- **Single container, many tests**: Singleton Testcontainers PostgreSQL 16 per JVM, shared across all integration/component/E2E tests
- **Deterministic isolation**: Automatic database truncation (CASCADE) between tests via JUnit 5 Extension
- **Fast feedback**: Surefire runs unit + property tests (no Docker); Failsafe runs integration+ (with Docker)
- **Property-based testing at scale**: jqwik with real database for concurrency, idempotency, and lockout verification
- **Security coverage**: JWT round-trip properties, RBAC parameterized matrix, privacy assertion utilities

---

## Architecture

### Test Module Dependency Graph

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Maven Module Dependencies                           │
│                                                                             │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐           │
│  │ module-auth│  │module-sales│  │module-inv  │  │module-fin  │  ...       │
│  │   (test)   │  │   (test)   │  │   (test)   │  │   (test)   │           │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘           │
│        │                │                │                │                  │
│        └────────────────┴────────────────┴────────────────┘                  │
│                                    │                                         │
│                         ┌──────────▼──────────┐                              │
│                         │  shared/test-support │ (scope: test)                │
│                         │                      │                              │
│                         │ • AbstractIntegrationTest                           │
│                         │ • AbstractDatabasePropertyTest                      │
│                         │ • DatabaseCleaner(Extension)                        │
│                         │ • TestJwtGenerator                                 │
│                         │ • ConcurrentTestRunner                             │
│                         │ • builders/*                                        │
│                         │ • arbitraries/*                                     │
│                         └──────────┬──────────┘                              │
│                                    │ depends on                               │
│                         ┌──────────▼──────────┐                              │
│                         │   infrastructure    │ (for JwtTokenProvider,        │
│                         │   shared-kernel     │  domain models, etc.)         │
│                         └─────────────────────┘                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Test Layer Execution Model

```
┌─────────────────────────────────────────────────────────────────┐
│                     Maven Build Lifecycle                        │
│                                                                 │
│  Phase: test (Surefire)          Phase: verify (Failsafe)       │
│  ┌─────────────────────────┐    ┌─────────────────────────────┐│
│  │ *Test.java              │    │ *IT.java (Integration)      ││
│  │ *Properties.java        │    │ *CT.java (Component)        ││
│  │                         │    │ *E2ETest.java (End-to-End)  ││
│  │ NO Docker required      │    │ Docker + Testcontainers     ││
│  │ NO Spring context       │    │ Spring context + real DB    ││
│  │ Target: <30s            │    │ Target: <5min total         ││
│  └─────────────────────────┘    └─────────────────────────────┘│
│                                                                 │
│  Profile: -Pfast-tests          Profile: -Pintegration          │
│  (skips TC, Docker)             (includes Failsafe)             │
└─────────────────────────────────────────────────────────────────┘
```

### CI Pipeline Stages

```
┌────────────────────────────────────────────────────────────────────────┐
│                       GitHub Actions Pipeline                          │
│                                                                        │
│  Stage 1: Unit Tests             Stage 2: Integration        Stage 3   │
│  ┌──────────────────┐           ┌──────────────────┐    ┌───────────┐ │
│  │ mvn test         │──pass──►  │ mvn verify       │──► │ JaCoCo    │ │
│  │ -Pfast-tests     │           │ -Pintegration    │    │ aggregate │ │
│  │ (no Docker)      │           │ (Docker + TC)    │    │ upload    │ │
│  └──────────────────┘           └──────────────────┘    └───────────┘ │
│         │                              │                               │
│      fail → STOP                    fail → STOP                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Components and Interfaces

### Package Structure

```
shared/test-support/src/main/java/com/erp/test/
├── AbstractIntegrationTest.java
├── AbstractDatabasePropertyTest.java
├── DatabaseCleaner.java
├── DatabaseCleanerExtension.java
├── TestJwtGenerator.java
├── ConcurrentTestRunner.java
├── ConcurrentTestResult.java
├── RbacTestCase.java
├── builders/
│   ├── UsuarioTestBuilder.java
│   ├── UsuarioJpaTestBuilder.java
│   ├── ProdutoTestBuilder.java
│   ├── ProdutoJpaTestBuilder.java
│   ├── VarianteTestBuilder.java
│   ├── VarianteJpaTestBuilder.java
│   ├── EstoqueItemTestBuilder.java
│   ├── EstoqueItemJpaTestBuilder.java
│   ├── VendaTestBuilder.java
│   ├── VendaJpaTestBuilder.java
│   ├── ClienteTestBuilder.java
│   ├── ClienteJpaTestBuilder.java
│   ├── LancamentoTestBuilder.java
│   ├── LancamentoJpaTestBuilder.java
│   ├── CampanhaTestBuilder.java
│   ├── CampanhaJpaTestBuilder.java
│   ├── CupomTestBuilder.java
│   └── CupomJpaTestBuilder.java
└── arbitraries/
    ├── UsuarioArbitraries.java
    ├── VendaArbitraries.java
    ├── InventoryArbitraries.java
    ├── ClienteArbitraries.java
    ├── PricingArbitraries.java
    └── FinanceArbitraries.java
```

---

### AbstractIntegrationTest

Base class for all integration, component, and E2E tests. Manages the Testcontainers singleton.

```java
package com.erp.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest
@ExtendWith(DatabaseCleanerExtension.class)
@Testcontainers
public abstract class AbstractIntegrationTest {

    // Singleton container — shared across ALL test classes in the same JVM
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("erp_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(isLocalDev());

    static {
        POSTGRES.start(); // Started once, reused for entire JVM lifetime
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.clean-disabled", () -> "false");
    }

    private static boolean isLocalDev() {
        return !"true".equals(System.getenv("CI"));
    }
}
```

**Design Rationale**: The singleton pattern avoids ~3s container startup per test class. A single PostgreSQL 16 container serves all modules. Flyway auto-migrates on first Spring context load (Spring Boot auto-configuration). The `reuse` flag allows local developers to keep the container running across Maven invocations.

---

### AbstractDatabasePropertyTest

Base class for jqwik property tests that need a real PostgreSQL database.

```java
package com.erp.test;

import net.jqwik.api.lifecycle.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public abstract class AbstractDatabasePropertyTest implements AroundTryHook {

    private static final PostgreSQLContainer<?> POSTGRES =
            AbstractIntegrationTest.POSTGRES; // Reuse same singleton

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    /**
     * Called BEFORE each jqwik try (each generated input combination).
     * Ensures clean DB state for every property trial.
     */
    @Override
    public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry,
                                         java.util.List<Object> parameters) {
        databaseCleaner.clean();
        return aTry.execute(parameters);
    }

    @Override
    public int proximity() {
        return -10; // Execute before other hooks
    }
}
```

**Design Rationale**: jqwik's `AroundTryHook` provides per-try lifecycle control, unlike JUnit's `@BeforeEach` which only runs once per property method. This ensures each of the 100+ generated inputs operates on a fresh database.

---

### DatabaseCleaner

Utility that truncates all application tables while preserving Flyway history.

```java
package com.erp.test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatabaseCleaner {

    private static final List<String> EXCLUDED_TABLES = List.of("flyway_schema_history");

    private final JdbcTemplate jdbcTemplate;
    private List<String> tablesToTruncate;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Truncates all application tables using CASCADE to handle FK dependencies.
     * Resets sequences to 1 for deterministic ID generation in tests.
     */
    @Transactional
    public void clean() {
        if (tablesToTruncate == null) {
            tablesToTruncate = discoverTables();
        }
        if (!tablesToTruncate.isEmpty()) {
            String tableList = String.join(", ", tablesToTruncate);
            jdbcTemplate.execute("TRUNCATE TABLE " + tableList + " RESTART IDENTITY CASCADE");
        }
    }

    private List<String> discoverTables() {
        return jdbcTemplate.queryForList(
                """
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_type = 'BASE TABLE'
                  AND table_name NOT IN (?)
                ORDER BY table_name
                """,
                String.class,
                String.join(",", EXCLUDED_TABLES)
        ).stream()
         .filter(t -> !EXCLUDED_TABLES.contains(t))
         .toList();
    }
}
```

---

### DatabaseCleanerExtension

JUnit 5 Extension that invokes `DatabaseCleaner` after each test method.

```java
package com.erp.test;

import org.junit.jupiter.api.extension.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class DatabaseCleanerExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        // Skip if test is @Transactional (Spring manages rollback)
        if (isTransactional(context)) {
            return;
        }

        var applicationContext = SpringExtension.getApplicationContext(context);
        var cleaner = applicationContext.getBean(DatabaseCleaner.class);
        cleaner.clean();
    }

    private boolean isTransactional(ExtensionContext context) {
        return context.getRequiredTestMethod()
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class)
            || context.getRequiredTestClass()
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class);
    }
}
```

---

### TestJwtGenerator

Generates JWT tokens for test authentication against all roles.

```java
package com.erp.test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class TestJwtGenerator {

    // Must match the secret configured in application-test.yml
    private static final String TEST_SECRET =
            "test-secret-key-that-is-at-least-256-bits-long-for-hs256-signing";
    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRY_MINUTES = 15;

    /**
     * Generate a valid JWT for the given role and user UUID.
     */
    public static String generateToken(UUID userUuid, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userUuid.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(EXPIRY_MINUTES, ChronoUnit.MINUTES)))
                .signWith(KEY)
                .compact();
    }

    /** Generate token for a specific role with a random user UUID. */
    public static String generateToken(String role) {
        return generateToken(UUID.randomUUID(), role);
    }

    /** Generate an expired JWT. */
    public static String generateExpired(UUID userUuid, String role) {
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(userUuid.toString())
                .claim("role", role)
                .issuedAt(Date.from(past.minus(15, ChronoUnit.MINUTES)))
                .expiration(Date.from(past))
                .signWith(KEY)
                .compact();
    }

    /** Generate a JWT with a role value not in the valid set. */
    public static String generateWithInvalidRole(String invalidRole) {
        return generateToken(UUID.randomUUID(), invalidRole);
    }

    /** Generate a JWT with tampered signature (last char changed). */
    public static String generateTampered(UUID userUuid, String role) {
        String valid = generateToken(userUuid, role);
        char lastChar = valid.charAt(valid.length() - 1);
        char replacement = (lastChar == 'A') ? 'B' : 'A';
        return valid.substring(0, valid.length() - 1) + replacement;
    }

    /** Get the test secret for application-test.yml configuration. */
    public static String getTestSecret() {
        return TEST_SECRET;
    }
}
```

---

### ConcurrentTestRunner

Utility for executing concurrent operations and collecting results.

```java
package com.erp.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class ConcurrentTestRunner {

    /**
     * Execute a task concurrently across N threads. All threads start simultaneously
     * using a CountDownLatch to maximize contention.
     *
     * @param threadCount number of concurrent threads
     * @param task        the operation to execute (returns true for success, false for failure)
     * @return aggregated result with success/failure counts and any exceptions
     */
    public static ConcurrentTestResult run(int threadCount, Supplier<Boolean> task) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait until all threads are ready
                    Boolean result = task.get();
                    results.add(result);
                } catch (Exception e) {
                    errors.add(e);
                    results.add(false);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Release all threads simultaneously
        try {
            boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                throw new AssertionError("Concurrent test did not complete within 30 seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Concurrent test interrupted", e);
        } finally {
            executor.shutdownNow();
        }

        long successCount = results.stream().filter(Boolean::booleanValue).count();
        long failureCount = results.stream().filter(r -> !r).count();
        return new ConcurrentTestResult(successCount, failureCount, errors);
    }

    /**
     * Overload: execute a Runnable task (exceptions count as failures).
     */
    public static ConcurrentTestResult run(int threadCount, Runnable task) {
        return run(threadCount, () -> {
            task.run();
            return true;
        });
    }
}
```

```java
package com.erp.test;

import java.util.List;

public record ConcurrentTestResult(
        long successCount,
        long failureCount,
        List<Throwable> errors
) {
    public long totalAttempts() {
        return successCount + failureCount;
    }
}
```

---

### RbacTestCase

Data structure for parameterized RBAC testing.

```java
package com.erp.test;

public record RbacTestCase(
        String httpMethod,
        String path,
        String role,
        int expectedStatus,
        String description
) {
    /** Factory for authorized cases */
    public static RbacTestCase allowed(String method, String path, String role) {
        return new RbacTestCase(method, path, role, 200, role + " → " + method + " " + path);
    }

    /** Factory for forbidden cases */
    public static RbacTestCase forbidden(String method, String path, String role) {
        return new RbacTestCase(method, path, role, 403, role + " → " + method + " " + path);
    }
}
```

---

### Test Data Builders — Fluent API

Each builder follows the same pattern. Example for `UsuarioTestBuilder`:

```java
package com.erp.test.builders;

import com.erp.modules.auth.domain.model.Role;
import com.erp.modules.auth.domain.model.Usuario;

import java.time.Instant;
import java.util.UUID;

public class UsuarioTestBuilder {

    private UUID uuid = UUID.randomUUID();
    private String username = "user_" + uuid.toString().substring(0, 8) + "@example.com";
    private String passwordHash = "$2a$10$dummyBcryptHashForTestingPurposes000000000000000000";
    private Role role = Role.ROLE_CASHIER;
    private boolean active = true;
    private int failedAttempts = 0;
    private Instant lockedUntil = null;

    private UsuarioTestBuilder() {}

    // Named constructors for common personas
    public static UsuarioTestBuilder aManager() {
        return new UsuarioTestBuilder().withRole(Role.ROLE_MANAGER);
    }

    public static UsuarioTestBuilder aCashier() {
        return new UsuarioTestBuilder().withRole(Role.ROLE_CASHIER);
    }

    public static UsuarioTestBuilder aStockOperator() {
        return new UsuarioTestBuilder().withRole(Role.ROLE_STOCK);
    }

    public static UsuarioTestBuilder aFinanceOperator() {
        return new UsuarioTestBuilder().withRole(Role.ROLE_FINANCE);
    }

    public static UsuarioTestBuilder aLockedUser() {
        return new UsuarioTestBuilder()
                .withFailedAttempts(6)
                .withLockedUntil(Instant.now().plusSeconds(900));
    }

    // Fluent setters
    public UsuarioTestBuilder withUuid(UUID uuid) { this.uuid = uuid; return this; }
    public UsuarioTestBuilder withUsername(String username) { this.username = username; return this; }
    public UsuarioTestBuilder withPasswordHash(String hash) { this.passwordHash = hash; return this; }
    public UsuarioTestBuilder withRole(Role role) { this.role = role; return this; }
    public UsuarioTestBuilder withActive(boolean active) { this.active = active; return this; }
    public UsuarioTestBuilder withFailedAttempts(int n) { this.failedAttempts = n; return this; }
    public UsuarioTestBuilder withLockedUntil(Instant locked) { this.lockedUntil = locked; return this; }

    /** Build domain object (in-memory, no persistence) */
    public Usuario build() {
        return Usuario.reconstitute(uuid, username, passwordHash, role, active,
                failedAttempts, lockedUntil, Instant.now());
    }
}
```

All other builders (`ProdutoTestBuilder`, `VarianteTestBuilder`, `EstoqueItemTestBuilder`, `VendaTestBuilder`, `ClienteTestBuilder`, `LancamentoTestBuilder`, `CampanhaTestBuilder`, `CupomTestBuilder`) follow the same pattern with:
- Default values that produce valid domain objects
- Named constructors for common test scenarios
- Fluent setter methods
- Corresponding `*JpaTestBuilder` for persistence entities

---

### Jqwik Arbitrary Providers

```java
package com.erp.test.arbitraries;

import com.erp.modules.auth.domain.model.Role;
import com.erp.modules.auth.domain.model.Usuario;
import net.jqwik.api.*;

import java.util.UUID;

public class UsuarioArbitraries {

    public static Arbitrary<Usuario> validUsuario() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                validUsername(),
                validRole()
        ).as((uuid, username, role) ->
                Usuario.reconstitute(uuid, username,
                        "$2a$10$dummyHash0000000000000000000000000000000000000000",
                        role, true, 0, null, java.time.Instant.now())
        );
    }

    public static Arbitrary<Role> validRole() {
        return Arbitraries.of(Role.ROLE_MANAGER, Role.ROLE_CASHIER,
                Role.ROLE_STOCK, Role.ROLE_FINANCE);
    }

    public static Arbitrary<String> validUsername() {
        return Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(3).ofMaxLength(20)
                .map(s -> s.toLowerCase() + "@example.com");
    }

    public static Arbitrary<Integer> failedAttemptsBelowThreshold() {
        return Arbitraries.integers().between(1, 5);
    }

    public static Arbitrary<Integer> failedAttemptsAboveThreshold() {
        return Arbitraries.integers().between(6, 20);
    }
}
```

```java
package com.erp.test.arbitraries;

import net.jqwik.api.*;
import java.math.BigDecimal;
import java.util.*;

public class VendaArbitraries {

    public static Arbitrary<BigDecimal> validUnitPrice() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999.99"))
                .ofScale(2);
    }

    public static Arbitrary<Integer> validQuantity() {
        return Arbitraries.integers().between(1, 100);
    }

    public static Arbitrary<String> validPaymentMethod() {
        return Arbitraries.of("DINHEIRO", "DEBITO", "CREDITO", "PIX");
    }

    public static Arbitrary<BigDecimal> validDiscount() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("100.00"))
                .ofScale(2);
    }
}
```

```java
package com.erp.test.arbitraries;

import net.jqwik.api.*;
import java.util.*;

public class InventoryArbitraries {

    public static Arbitrary<Integer> validStockLevel() {
        return Arbitraries.integers().between(1, 100);
    }

    public static Arbitrary<Integer> validThreadCount() {
        return Arbitraries.integers().between(2, 20);
    }

    public static Arbitrary<Integer> validOperationQuantity() {
        return Arbitraries.integers().between(1, 100);
    }

    public enum StockOperation { ENTRY, WITHDRAWAL, RESERVE, RELEASE }

    public static Arbitrary<StockOperation> stockOperation() {
        return Arbitraries.of(StockOperation.values());
    }

    public static Arbitrary<List<StockOperation>> operationSequence() {
        return stockOperation().list().ofMinSize(5).ofMaxSize(50);
    }
}
```

---

## Data Models

### Configuration: test-support `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.erp</groupId>
        <artifactId>erp-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>test-support</artifactId>
    <name>ERP - Test Support</name>
    <description>Shared test utilities, builders, arbitraries, and base classes</description>

    <dependencies>
        <!-- Internal modules (compile scope — builders need domain models) -->
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>shared-kernel</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>shared-events</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>shared-exceptions</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>module-auth</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>module-product</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>module-inventory</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>module-sales</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>module-customer</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>module-finance</artifactId>
        </dependency>
        <dependency>
            <groupId>com.erp</groupId>
            <artifactId>module-pricing</artifactId>
        </dependency>

        <!-- Spring Boot Test (provides @SpringBootTest, DynamicPropertySource) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
        </dependency>

        <!-- Testcontainers -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
        </dependency>

        <!-- PostgreSQL driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>

        <!-- jqwik -->
        <dependency>
            <groupId>net.jqwik</groupId>
            <artifactId>jqwik</artifactId>
        </dependency>

        <!-- JWT for TestJwtGenerator -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
        </dependency>

        <!-- AssertJ -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
        </dependency>

        <!-- ArchUnit -->
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit-junit5</artifactId>
        </dependency>
    </dependencies>
</project>
```

### Parent POM Additions

Add to parent `pom.xml` modules list:
```xml
<module>shared/test-support</module>
```

Add to `<dependencyManagement>`:
```xml
<dependency>
    <groupId>com.erp</groupId>
    <artifactId>test-support</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

### Maven Plugin Configuration (Parent POM)

```xml
<pluginManagement>
    <plugins>
        <!-- Surefire: Unit + Property tests only -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.5.1</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                    <include>**/*Tests.java</include>
                    <include>**/*Properties.java</include>
                </includes>
                <excludes>
                    <exclude>**/*IT.java</exclude>
                    <exclude>**/*CT.java</exclude>
                    <exclude>**/*E2ETest.java</exclude>
                </excludes>
            </configuration>
        </plugin>

        <!-- Failsafe: Integration, Component, E2E tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.5.1</version>
            <configuration>
                <includes>
                    <include>**/*IT.java</include>
                    <include>**/*CT.java</include>
                    <include>**/*E2ETest.java</include>
                </includes>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- JaCoCo: coverage per module -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.12</version>
            <configuration>
                <excludes>
                    <exclude>com/erp/test/**</exclude>
                    <exclude>**/builders/**</exclude>
                    <exclude>**/arbitraries/**</exclude>
                    <exclude>**/*TestBuilder*</exclude>
                </excludes>
            </configuration>
            <executions>
                <execution>
                    <id>prepare-agent</id>
                    <goals><goal>prepare-agent</goal></goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>verify</phase>
                    <goals><goal>report</goal></goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</pluginManagement>
```

### JaCoCo Aggregate in Bootstrap Module

Add to `bootstrap/pom.xml`:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>report-aggregate</id>
            <phase>verify</phase>
            <goals><goal>report-aggregate</goal></goals>
            <configuration>
                <dataFileIncludes>
                    <dataFileInclude>**/jacoco.exec</dataFileInclude>
                </dataFileIncludes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Maven Profile: `-Pfast-tests`

Add to parent `pom.xml`:
```xml
<profiles>
    <profile>
        <id>fast-tests</id>
        <properties>
            <skipITs>true</skipITs>
        </properties>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <excludedGroups>testcontainers</excludedGroups>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                    <configuration>
                        <skip>true</skip>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>

    <profile>
        <id>integration</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

### Testcontainers Configuration Files

`src/test/resources/testcontainers.properties` (committed to repo):
```properties
# Local development: reuse containers across runs
testcontainers.reuse.enable=true
```

CI environment variable override:
```yaml
env:
  TESTCONTAINERS_REUSE_ENABLE: "false"
```

### application-test.yml

```yaml
spring:
  datasource:
    url: ${spring.datasource.url}
    username: ${spring.datasource.username}
    password: ${spring.datasource.password}
  flyway:
    enabled: true
    locations: classpath:db/migration
    clean-disabled: false
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

app:
  jwt:
    secret: "test-secret-key-that-is-at-least-256-bits-long-for-hs256-signing"
    expiration-minutes: 15
    refresh-expiration-days: 7
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: JWT Claims Round-Trip

*For any* valid user UUID and valid role, generating a JWT and then decoding it SHALL yield the same `sub` (user UUID) and `role` values, with `exp` exactly 15 minutes from issuance (±1 second tolerance).

**Validates: Requirements 6.3, 7.1, 7.5**

### Property 2: Tampered JWT Rejection

*For any* valid JWT token, any modification to its payload bytes or signature bytes SHALL cause the authentication filter to reject the token with HTTP 401.

**Validates: Requirements 7.2**

### Property 3: Invalid Role Rejection

*For any* string that is not in the set {ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, ROLE_FINANCE}, attempting to create or present a JWT with that role value SHALL be rejected by the system.

**Validates: Requirements 7.7**

### Property 4: Refresh Token Rotation Invalidates Old Token

*For any* valid refresh token, after a successful token rotation (refresh), the old refresh token SHALL be immediately unusable — any subsequent refresh attempt with it SHALL return HTTP 401.

**Validates: Requirements 7.4**

### Property 5: RBAC Enforcement Matrix

*For any* (endpoint, role) combination, the system SHALL return HTTP 2xx when the role is authorized, HTTP 403 when unauthorized, and HTTP 401 when unauthenticated — as defined in the role permissions matrix.

**Validates: Requirements 8.1, 8.2, 8.3, 8.6**

### Property 6: Lockout After N > 5 Failures

*For any* user and *for any* N consecutive failed login attempts where N > 5, all subsequent login attempts (including with correct credentials) SHALL be rejected for 15 minutes, and the `locked_until` field SHALL be set in the database.

**Validates: Requirements 9.1, 9.2**

### Property 7: Counter Reset On Success After ≤ 5 Failures

*For any* user and *for any* N consecutive failed login attempts where 1 ≤ N ≤ 5, a subsequent successful login SHALL reset the `failed_attempts` counter to zero and no lockout SHALL be applied.

**Validates: Requirements 9.4**

### Property 8: Concurrent Stock Reservation Invariant

*For any* initial stock level S and *for any* number of concurrent threads T (2 ≤ T ≤ 20) each requesting quantity Q, the sum of all successful reservations SHALL never exceed S, the `physical_stock` SHALL remain unchanged, `reserved_stock` SHALL equal the sum of successful reservations, and `physical_stock - reserved_stock ≥ 0`.

**Validates: Requirements 10.1, 10.2, 10.3, 10.6**

### Property 9: Coupon Optimistic Lock Invariant

*For any* coupon with `max_usages` M and *for any* number of concurrent threads T (2 ≤ T ≤ 20) confirming usage, the final `usage_count` in the database SHALL never exceed M, and SHALL equal exactly the count of threads that received a success response.

**Validates: Requirements 11.1, 11.2, 11.5**

### Property 10: Domain Event Idempotency

*For any* domain event (with varying sale amounts, item counts, and payment methods) delivered N times (1 ≤ N ≤ 10) with the same `eventId`, the system SHALL produce exactly 1 side-effect (exactly 1 RECEITA entry in lancamentos_financeiros, stock committed exactly once).

**Validates: Requirements 12.1, 12.2, 12.3, 12.5**

### Property 11: Stock Invariants Under Operations

*For any* sequence of stock operations (entries, withdrawals, reservations, releases) of length 5–50 with random quantities 1–100, the following invariants SHALL hold after every operation: `available_stock = physical_stock - reserved_stock`, `physical_stock ≥ 0`, `reserved_stock ≥ 0`, `available_stock ≥ 0`, and `final_physical = initial + sum(entries) - sum(committed_withdrawals)`.

**Validates: Requirements 13.1, 13.2, 13.3, 13.4**

### Property 12: Customer Data Privacy in Error Responses

*For any* error response (4xx) from customer registration or search endpoints, the response body SHALL NOT contain any CPF pattern (11 digits or XXX.XXX.XXX-XX format), existing customer UUID, name, email, or phone number.

**Validates: Requirements 14.1, 14.2, 14.4**

### Property 13: Authentication Error Privacy

*For any* failed authentication attempt (wrong username, wrong password, non-existent user), the error response SHALL contain only "Credenciais inválidas" and SHALL NOT indicate whether the username exists or the password is incorrect.

**Validates: Requirements 14.5**

### Property 14: Builder Defaults Produce Valid Objects

*For any* test builder class, calling `build()` with no custom overrides SHALL produce a domain object that passes all domain validation rules without throwing exceptions.

**Validates: Requirements 4.2**

### Property 15: Builder Fluent Override Isolation

*For any* test builder and *for any* single field override, all other fields SHALL remain at their default values — overriding one attribute SHALL NOT affect any other attribute.

**Validates: Requirements 4.4**

### Property 16: Database Cleanup Isolation

*For any* integration test method that completes execution, all application tables (except `flyway_schema_history`) SHALL contain zero rows after cleanup, and `flyway_schema_history` row count SHALL remain unchanged.

**Validates: Requirements 3.1, 3.3, 19.2**

### Property 17: Unit Test Independence from Infrastructure

*For any* test class matching the `*Test.java` or `*Properties.java` pattern (unit test layer), the class SHALL NOT import `org.testcontainers`, SHALL NOT use `@SpringBootTest`, and SHALL NOT depend on any external service.

**Validates: Requirements 1.7**

---

## Error Handling

### Test Infrastructure Error Scenarios

| Scenario | Handling Strategy |
|----------|-------------------|
| Docker unavailable | `@EnabledIf(DockerAvailableCondition.class)` skips TC tests with log warning |
| Testcontainer fails to start | `@BeforeAll` fails fast with clear error; no retries |
| Flyway migration fails | Spring context fails to load; test halted with migration error in stack trace |
| Database cleanup fails (FK violation) | `CASCADE` in TRUNCATE prevents this; if triggered, test fails with full SQL error |
| Concurrent test timeout (>30s) | `@Timeout(30, unit = SECONDS)` on test method; clear timeout assertion error |
| jqwik shrinking exceeds time | Configure `jqwik.shrinking.seconds=60` in `jqwik.properties` |
| Port conflict on container | Testcontainers uses random ports; no conflict possible |
| Out-of-memory with many containers | Singleton pattern = only 1 container; no OOM risk |

### Docker Availability Condition

```java
package com.erp.test;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

public class DockerAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            DockerClientFactory.instance().client();
            return ConditionEvaluationResult.enabled("Docker is available");
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled(
                    "Docker is not available — skipping Testcontainers test: " + e.getMessage());
        }
    }
}
```

---

## Testing Strategy

### Test Layer Distribution

| Layer | Naming Pattern | Runner | Docker | Spring Context | Target Time |
|-------|---------------|--------|--------|----------------|-------------|
| Unit | `*Test.java`, `*Properties.java` | Surefire | No | No | <30s |
| Integration | `*IT.java` | Failsafe | Yes | Partial | <2min |
| Component | `*CT.java` | Failsafe | Yes | Module-scoped | <2min |
| E2E | `*E2ETest.java` | Failsafe | Yes | Full | <3min |

### Property-Based Testing Configuration

- **Library**: jqwik 1.8.5 (already in parent POM)
- **Default iterations**: 100 per property (configurable via `@Property(tries = N)`)
- **Shrinking**: Enabled with `.jqwik-database` per module for regression replay
- **Tag format**: `@Label("Feature: erp-loja-roupas, Property {N}: {description}")`
- **Database-backed properties**: Extend `AbstractDatabasePropertyTest` for per-try cleanup

### jqwik Configuration (`src/test/resources/jqwik.properties`)

```properties
jqwik.defaultTries=100
jqwik.shrinking.enabled=true
jqwik.shrinking.seconds=60
jqwik.database=.jqwik-database
jqwik.reporting.onlyFailures=false
```

### Unit Test Guidelines

Unit tests focus on:
- Domain model invariants (already covered by 12 existing property tests)
- Use case logic with mocked ports
- Value object validation (CPF, Email, Money)
- Specific error conditions and edge cases

Unit tests do NOT:
- Boot Spring context
- Use Testcontainers
- Access real databases or external services

### Integration Test Guidelines

Integration tests focus on:
- JPA repository correctness (save/load/query)
- Flyway migration compatibility
- Database constraint enforcement
- Cross-module event delivery

### Property Test Mapping to Requirements

| Property | Test Class | Layer | DB Required |
|----------|-----------|-------|-------------|
| Property 1 (JWT round-trip) | `JwtClaimsRoundTripProperties.java` | Unit | No |
| Property 2 (tampered JWT) | `JwtTamperRejectionProperties.java` | Unit | No |
| Property 3 (invalid role) | `JwtInvalidRoleProperties.java` | Unit | No |
| Property 4 (refresh rotation) | `RefreshTokenRotationIT.java` | Integration | Yes |
| Property 5 (RBAC matrix) | `RbacEnforcementE2ETest.java` | E2E | Yes |
| Property 6 (lockout >5) | `LockoutAfterFailuresIT.java` | Integration | Yes |
| Property 7 (counter reset) | `LockoutResetOnSuccessIT.java` | Integration | Yes |
| Property 8 (concurrent stock) | `ConcurrentStockReservationIT.java` | Integration | Yes |
| Property 9 (coupon lock) | `CouponOptimisticLockIT.java` | Integration | Yes |
| Property 10 (event idempotency) | `EventIdempotencyIT.java` | Integration | Yes |
| Property 11 (stock invariants) | `StockInvariantSequenceIT.java` | Integration | Yes |
| Property 12 (CPF privacy) | `CustomerDataPrivacyIT.java` | Integration | Yes |
| Property 13 (auth error privacy) | `AuthErrorPrivacyIT.java` | Integration | Yes |
| Property 14 (builder defaults) | `BuilderDefaultsValidProperties.java` | Unit | No |
| Property 15 (builder isolation) | `BuilderOverrideIsolationProperties.java` | Unit | No |
| Property 16 (DB cleanup) | Implicit via `DatabaseCleanerExtension` | Integration | Yes |
| Property 17 (unit independence) | `UnitTestIndependenceArchTest.java` | Unit (ArchUnit) | No |

### ArchUnit Rules

```java
package com.erp.test;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.erp")
public class TestLayerArchRules {

    @ArchTest
    static final ArchRule unitTestsMustNotUseTestcontainers =
            noClasses()
                    .that().haveSimpleNameEndingWith("Test")
                    .and().resideInAnyPackage("..test..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.testcontainers..");

    @ArchTest
    static final ArchRule unitTestsMustNotUseSpringBootTest =
            noClasses()
                    .that().haveSimpleNameEndingWith("Test")
                    .and().resideInAnyPackage("..test..")
                    .should().beAnnotatedWith(
                            org.springframework.boot.test.context.SpringBootTest.class);
}
```

### CI Pipeline Definition (GitHub Actions)

```yaml
name: ERP Test Pipeline

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - name: Run unit tests
        run: mvn test -Pfast-tests -B
      - name: Upload surefire reports
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: surefire-reports
          path: '**/target/surefire-reports/'

  integration-tests:
    needs: unit-tests
    runs-on: ubuntu-latest
    services:
      docker:
        image: docker:dind
    env:
      TESTCONTAINERS_REUSE_ENABLE: "false"
      CI: "true"
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - name: Run integration + E2E tests
        run: mvn verify -Pintegration -B
      - name: Upload failsafe reports
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: failsafe-reports
          path: '**/target/failsafe-reports/'

  coverage:
    needs: integration-tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - name: Generate coverage report
        run: mvn verify -Pintegration jacoco:report-aggregate -B
      - name: Upload JaCoCo report
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: 'bootstrap/target/site/jacoco-aggregate/'
```

### Integration Points Summary

| Component | Integrates With | Mechanism |
|-----------|----------------|-----------|
| `AbstractIntegrationTest` | Spring Boot, Testcontainers, Flyway | `@DynamicPropertySource`, singleton container |
| `AbstractDatabasePropertyTest` | jqwik, Spring, DatabaseCleaner | `AroundTryHook` for per-try cleanup |
| `DatabaseCleanerExtension` | JUnit 5, Spring ApplicationContext | `AfterEachCallback` |
| `TestJwtGenerator` | JJWT library (same secret as app) | Shared `application-test.yml` secret |
| `ConcurrentTestRunner` | Java concurrency (ExecutorService) | `CountDownLatch` for synchronized start |
| Builders | Domain models from all modules | Compile-time dependency on module artifacts |
| Arbitraries | jqwik `Arbitrary` API, domain models | `@Provide` methods and `Combinators` |
| ArchUnit rules | All source packages | `@AnalyzeClasses` with package patterns |
| JaCoCo | Maven lifecycle, all modules | `prepare-agent` → `report` → `report-aggregate` |
