package com.erp.modules.inventory;

import com.erp.modules.inventory.domain.model.EstoqueItem;
import com.erp.shared.exceptions.InsufficientStockException;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates: Requirements 1.2
 */
class Property8_NegativeWithdrawalRejectedTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 8: Withdrawals that would create negative physicalStock are always rejected")
    void withdrawalBeyondPhysicalAlwaysRejected(
            @ForAll @IntRange(min = 0, max = 100) int physical,
            @ForAll @IntRange(min = 1, max = 200) int withdrawQty) {

        Assume.that(withdrawQty > physical);
        EstoqueItem item = EstoqueItem.create(UUID.randomUUID());
        if (physical > 0) item.incrementPhysical(physical);

        int physBefore = item.getPhysicalStock();
        assertThatThrownBy(() -> item.decrementPhysical(withdrawQty))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(item.getPhysicalStock()).isEqualTo(physBefore);
    }
}
