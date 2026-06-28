package br.com.moreiracruz.erp.modules.inventory;

import br.com.moreiracruz.erp.modules.inventory.domain.model.EstoqueItem;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import net.jqwik.api.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates: Requirements 1.2
 */
class Property19_QuantityOutOfRangeRejectedTest {

    @Property(tries = 1000)
    @Label("Feature: erp-loja-roupas, Property 19: Quantity out of [1, 100000] is always rejected")
    void quantityOutOfRangeAlwaysRejected(@ForAll("invalidQuantity") int qty) {
        EstoqueItem item = EstoqueItem.create(UUID.randomUUID());
        item.incrementPhysical(50000); // ensure stock available

        assertThatThrownBy(() -> item.incrementPhysical(qty))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> item.decrementPhysical(qty))
                .isInstanceOf(Exception.class); // ValidationException or InsufficientStock
    }

    @Provide
    Arbitrary<Integer> invalidQuantity() {
        return Arbitraries.oneOf(
                Arbitraries.integers().lessOrEqual(0),
                Arbitraries.integers().greaterOrEqual(100001)
        );
    }
}
