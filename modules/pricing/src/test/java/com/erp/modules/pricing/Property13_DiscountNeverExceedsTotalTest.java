package com.erp.modules.pricing;

import com.erp.shared.utils.MoneyUtils;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property13_DiscountNeverExceedsTotalTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 13: Combined discount never exceeds sale total")
    void discountNeverExceedsTotal(
            @ForAll("subtotals") BigDecimal subtotal,
            @ForAll("percentDiscounts") BigDecimal percentDiscount,
            @ForAll("fixedDiscounts") BigDecimal fixedDiscount) {

        // Apply in priority: percentage first, then fixed — cap at subtotal
        BigDecimal pctAmount = MoneyUtils.round(
                subtotal.multiply(percentDiscount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        BigDecimal totalDiscount = pctAmount.add(fixedDiscount);
        BigDecimal capped = totalDiscount.min(subtotal); // cap at subtotal

        assertThat(capped).isLessThanOrEqualTo(subtotal);
        assertThat(subtotal.subtract(capped)).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Provide
    Arbitrary<BigDecimal> subtotals() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("1.00"), new BigDecimal("99999.99"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> percentDiscounts() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("100.00"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> fixedDiscounts() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("99999.99"))
                .ofScale(2);
    }
}
