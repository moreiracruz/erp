package br.com.moreiracruz.erp.test;

import net.jqwik.api.lifecycle.AroundTryHook;
import net.jqwik.api.lifecycle.TryExecutionResult;
import net.jqwik.api.lifecycle.TryExecutor;
import net.jqwik.api.lifecycle.TryLifecycleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

/**
 * Base class for jqwik property tests that need a real PostgreSQL database.
 *
 * <p>Implements {@link AroundTryHook} to perform database cleanup before each
 * jqwik try (each generated input combination), ensuring deterministic isolation.
 */
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
                                         List<Object> parameters) {
        databaseCleaner.clean();
        return aTry.execute(parameters);
    }
}
