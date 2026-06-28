package br.com.moreiracruz.erp.pricing;

import br.com.moreiracruz.erp.modules.pricing.domain.port.in.ConfirmCouponUsageUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCouponCommand;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCouponUseCase;
import br.com.moreiracruz.erp.test.AbstractIntegrationTest;
import br.com.moreiracruz.erp.test.ConcurrentTestResult;
import br.com.moreiracruz.erp.test.ConcurrentTestRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying coupon optimistic lock under concurrent access.
 * Property 9: Coupon usage never exceeds max.
 */
class CouponOptimisticLockIT extends AbstractIntegrationTest {

    @Autowired
    private CreateCouponUseCase createCouponUseCase;

    @Autowired
    private ConfirmCouponUsageUseCase confirmCouponUsageUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("15 concurrent threads confirming coupon with max_usages=5 — exactly 5 succeed")
    void concurrentCouponUsage_neverExceedsMaxUsages() {
        String couponCode = "CONCURRENT-TEST-" + System.currentTimeMillis();

        // Create coupon with max_usages = 5
        createCouponUseCase.create(new CreateCouponCommand(
                couponCode,
                "PERCENTAGE",
                new BigDecimal("10.00"),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(30, ChronoUnit.DAYS),
                5
        ));

        // Launch 15 threads each calling confirmCouponUsage
        ConcurrentTestResult result = ConcurrentTestRunner.run(15, () -> {
            try {
                confirmCouponUsageUseCase.confirm(couponCode);
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        // Assert: exactly 5 successful
        assertThat(result.successCount()).isEqualTo(5);

        // Assert DB: usage_count == 5
        Integer usageCount = jdbcTemplate.queryForObject(
                "SELECT usage_count FROM cupons WHERE code = ?",
                Integer.class, couponCode);
        assertThat(usageCount).isEqualTo(5);
    }

    @Test
    @DisplayName("Concurrent usage with max_usages=1 — exactly 1 succeeds")
    void concurrentCouponUsage_singleUse_exactlyOneSucceeds() {
        String couponCode = "SINGLE-USE-" + System.currentTimeMillis();

        createCouponUseCase.create(new CreateCouponCommand(
                couponCode,
                "PERCENTAGE",
                new BigDecimal("5.00"),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(30, ChronoUnit.DAYS),
                1
        ));

        ConcurrentTestResult result = ConcurrentTestRunner.run(10, () -> {
            try {
                confirmCouponUsageUseCase.confirm(couponCode);
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        assertThat(result.successCount()).isEqualTo(1);

        Integer usageCount = jdbcTemplate.queryForObject(
                "SELECT usage_count FROM cupons WHERE code = ?",
                Integer.class, couponCode);
        assertThat(usageCount).isEqualTo(1);
    }
}
