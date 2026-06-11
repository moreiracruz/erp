package com.erp.modules.finance;

import net.jqwik.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property16_CashFlowBalanceTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 16: Cash flow net balance equals sum of receitas minus sum of despesas")
    void netBalanceIsExact(
            @ForAll("amounts") List<BigDecimal> receitas,
            @ForAll("amounts") List<BigDecimal> despesas) {

        BigDecimal totalReceita = receitas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDespesa = despesas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netBalance = totalReceita.subtract(totalDespesa);

        assertThat(netBalance).isEqualByComparingTo(totalReceita.subtract(totalDespesa));
    }

    @Provide
    Arbitrary<List<BigDecimal>> amounts() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("99999.99"))
                .ofScale(2)
                .list()
                .ofMinSize(0)
                .ofMaxSize(10);
    }
}
