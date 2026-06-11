package com.erp.test.arbitraries;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import java.math.BigDecimal;

/**
 * Jqwik Arbitrary providers for the Pricing module domain objects.
 */
public class PricingArbitraries {

    /** Valid discount percentage: 0.01 to 100.00. */
    public static Arbitrary<BigDecimal> validDiscountPercentage() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("100.00"))
                .ofScale(2);
    }

    /** Valid fixed discount amount: 0.01 to 9999.99. */
    public static Arbitrary<BigDecimal> validFixedDiscount() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999.99"))
                .ofScale(2);
    }

    /** Valid max usages for coupons: 1 to 10000. */
    public static Arbitrary<Integer> validMaxUsages() {
        return Arbitraries.integers().between(1, 10000);
    }

    /** Valid cashback percentage: 0.01 to 50.00. */
    public static Arbitrary<BigDecimal> validCashbackPercentage() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("50.00"))
                .ofScale(2);
    }
}
