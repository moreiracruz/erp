package com.erp.modules.inventory;

import com.erp.modules.inventory.domain.model.EstoqueItem;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property7_ReserveThenReleaseRoundTripTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 7: Reserve-then-release restores original stock state")
    void reserveThenReleaseRestoresState(@ForAll @IntRange(min = 1, max = 500) int qty) {
        EstoqueItem item = EstoqueItem.create(UUID.randomUUID());
        item.incrementPhysical(1000);

        int physBefore = item.getPhysicalStock();
        int resBefore = item.getReservedStock();
        int availBefore = item.availableStock();

        item.incrementReserved(qty);
        item.decrementReserved(qty);

        assertThat(item.getPhysicalStock()).isEqualTo(physBefore);
        assertThat(item.getReservedStock()).isEqualTo(resBefore);
        assertThat(item.availableStock()).isEqualTo(availBefore);
    }
}
