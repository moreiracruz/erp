package com.erp;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test-only Spring Boot application class.
 * <p>
 * Placed in the root package {@code com.erp} so that all integration and E2E tests
 * (in sub-packages like {@code com.erp.auth}, {@code com.erp.e2e}) can find it
 * via Spring Boot's component scanning upward traversal.
 * <p>
 * Scans all {@code com.erp} packages — same behavior as the real
 * {@link com.erp.bootstrap.ErpApplication} but discoverable from test packages.
 */
@SpringBootApplication(scanBasePackages = "com.erp")
public class TestApplication {
}
