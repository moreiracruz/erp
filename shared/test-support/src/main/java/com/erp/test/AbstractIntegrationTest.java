package com.erp.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for all integration, component, and E2E tests.
 *
 * <p>Manages a singleton Testcontainers PostgreSQL 16 container shared across
 * all test classes in the same JVM. Flyway auto-migrates on first Spring context load.
 */
@SpringBootTest
@ExtendWith(DatabaseCleanerExtension.class)
@Testcontainers
@org.springframework.test.context.ActiveProfiles("test")
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
