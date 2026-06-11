package com.erp.modules.pricing;

import com.erp.modules.pricing.domain.model.CampaignType;
import com.erp.modules.pricing.domain.model.Cupom;
import com.erp.shared.exceptions.ValidationException;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property14_CouponUsageNeverExceedsMaxTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 14: Coupon usage count never exceeds its maximum limit")
    void couponUsageNeverExceedsMax(@ForAll @IntRange(min = 1, max = 50) int maxUsages) {
        Cupom cupom = Cupom.create("TEST", CampaignType.PERCENTAGE, new BigDecimal("10"),
                Instant.now().minusSeconds(3600), Instant.now().plusSeconds(3600), maxUsages);

        int successCount = 0;
        for (int i = 0; i < maxUsages + 5; i++) {
            try {
                cupom.incrementUsage();
                successCount++;
            } catch (ValidationException e) {
                // Expected after maxUsages
            }
        }

        assertThat(successCount).isEqualTo(maxUsages);
        assertThat(cupom.getUsageCount()).isEqualTo(maxUsages);
    }
}
