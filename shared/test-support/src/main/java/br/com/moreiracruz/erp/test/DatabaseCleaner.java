package br.com.moreiracruz.erp.test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Utility that truncates all application tables while preserving Flyway history.
 * Resets sequences to 1 for deterministic ID generation in tests.
 */
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
                ORDER BY table_name
                """,
                String.class
        ).stream()
         .filter(t -> !EXCLUDED_TABLES.contains(t))
         .toList();
    }
}
