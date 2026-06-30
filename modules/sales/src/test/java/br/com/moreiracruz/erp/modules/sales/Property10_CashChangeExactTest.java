package br.com.moreiracruz.erp.modules.sales;

import br.com.moreiracruz.erp.modules.sales.domain.model.ItemVenda;
import br.com.moreiracruz.erp.modules.sales.domain.model.Venda;
import br.com.moreiracruz.erp.shared.utils.MoneyUtils;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property10_CashChangeExactTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 10: Cash payment change calculation is exact")
    void cashChangeIsExact(
            @ForAll("totals") BigDecimal total,
            @ForAll("extras") BigDecimal extra) {

        Venda venda = Venda.create(UUID.randomUUID(), "PDV-01", null);
        ItemVenda item = ItemVenda.create(null, UUID.randomUUID(), "SKU-1", 1, total);
        venda.addItem(item);
        venda.computeTotal(BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal amountPaid = total.add(extra);
        BigDecimal change = venda.computeChange(amountPaid);

        assertThat(change).isEqualByComparingTo(MoneyUtils.round(amountPaid.subtract(total)));
    }

    @Provide
    Arbitrary<BigDecimal> totals() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("1.00"), new BigDecimal("9999.99"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> extras() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.00"), new BigDecimal("5000.00"))
                .ofScale(2);
    }
}
