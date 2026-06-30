package br.com.moreiracruz.erp.modules.inventory;

import br.com.moreiracruz.erp.modules.inventory.domain.model.EstoqueItem;
import net.jqwik.api.*;
import net.jqwik.api.Combinators;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 */
class Property6_StockCounterInvariantTest {

    enum OpType { ENTRY, WITHDRAWAL, RESERVE, RELEASE }

    record StockOp(OpType type, int qty) {}

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 6: Stock counter invariant after any operation")
    void stockCounterInvariantHoldsAfterAnySequence(@ForAll("validOperations") List<StockOp> ops) {
        EstoqueItem item = EstoqueItem.create(UUID.randomUUID());
        // Give it some initial stock
        item.incrementPhysical(500);

        for (StockOp op : ops) {
            try {
                switch (op.type()) {
                    case ENTRY -> item.incrementPhysical(op.qty());
                    case WITHDRAWAL -> item.decrementPhysical(op.qty());
                    case RESERVE -> item.incrementReserved(op.qty());
                    case RELEASE -> item.decrementReserved(op.qty());
                }
            } catch (Exception e) {
                // Invalid operations throw — that's fine, counters must remain valid
            }
            // INVARIANT: always holds regardless of exceptions
            assertThat(item.availableStock()).isEqualTo(item.getPhysicalStock() - item.getReservedStock());
            assertThat(item.getPhysicalStock()).isGreaterThanOrEqualTo(0);
            assertThat(item.getReservedStock()).isGreaterThanOrEqualTo(0);
            assertThat(item.availableStock()).isGreaterThanOrEqualTo(0);
        }
    }

    @Provide
    Arbitrary<List<StockOp>> validOperations() {
        var opType = Arbitraries.of(OpType.values());
        var qty = Arbitraries.integers().between(1, 100);
        return Combinators.combine(opType, qty).as(StockOp::new).list().ofMinSize(1).ofMaxSize(20);
    }
}
