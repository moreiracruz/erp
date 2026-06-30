package br.com.moreiracruz.erp;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test-only Spring Boot application class.
 * <p>
 * Placed in the root package {@code br.com.moreiracruz.erp} so that all integration and E2E tests
 * (in sub-packages like {@code br.com.moreiracruz.erp.auth}, {@code br.com.moreiracruz.erp.e2e}) can find it
 * via Spring Boot's component scanning upward traversal.
 * <p>
 * Scans all {@code br.com.moreiracruz.erp} packages — same behavior as the real
 * {@link br.com.moreiracruz.erp.bootstrap.ErpApplication} but discoverable from test packages.
 */
@SpringBootApplication(scanBasePackages = "br.com.moreiracruz.erp")
public class TestApplication {
}
