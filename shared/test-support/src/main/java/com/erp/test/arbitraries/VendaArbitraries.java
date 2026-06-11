package com.erp.test.arbitraries;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import java.math.BigDecimal;

/**
 * Jqwik Arbitrary providers for the Sales module domain objects.
 */
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
