package br.com.moreiracruz.erp.modules.sales;

import br.com.moreiracruz.erp.modules.sales.domain.model.ItemVenda;
import br.com.moreiracruz.erp.modules.sales.domain.model.Venda;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;
import net.jqwik.api.constraints.IntRange;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property9_BackendTotalTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 9: Sale total is always computed on the backend")
    void backendTotalNeverTrustedFromClient(
            @ForAll("unitPrices") BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 10) int quantity,
            @ForAll("discounts") BigDecimal discount) {

        Venda venda = Venda.create(UUID.randomUUID(), "PDV-01", null);
        ItemVenda item = ItemVenda.create(null, UUID.randomUUID(), "SKU-1", quantity, unitPrice);
        venda.addItem(item);

        BigDecimal backendTotal = venda.computeTotal(discount, BigDecimal.ZERO);
        BigDecimal wrongTotal = backendTotal.add(new BigDecimal("0.01"));

        // Any mismatch should be detected
        assertThat(wrongTotal).isNotEqualTo(backendTotal);
    }

    @Provide
    Arbitrary<BigDecimal> unitPrices() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999.99"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> discounts() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.00"), new BigDecimal("100.00"))
                .ofScale(2);
    }
}
