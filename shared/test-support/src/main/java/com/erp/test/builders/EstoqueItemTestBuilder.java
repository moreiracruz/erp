package com.erp.test.builders;

import com.erp.modules.inventory.domain.model.EstoqueItem;

import java.util.UUID;

/**
 * Fluent builder for creating {@link EstoqueItem} domain objects in tests.
 */
public class EstoqueItemTestBuilder {

    private UUID varianteUuid = UUID.randomUUID();
    private int physicalStock = 50;
    private int reservedStock = 0;

    private EstoqueItemTestBuilder() {}

    public static EstoqueItemTestBuilder anEstoqueItem() {
        return new EstoqueItemTestBuilder();
    }

    public EstoqueItemTestBuilder withVarianteUuid(UUID varianteUuid) { this.varianteUuid = varianteUuid; return this; }
    public EstoqueItemTestBuilder withPhysicalStock(int physicalStock) { this.physicalStock = physicalStock; return this; }
    public EstoqueItemTestBuilder withReservedStock(int reservedStock) { this.reservedStock = reservedStock; return this; }

    /** Build domain object using restore to set physical/reserved stock values. */
    public EstoqueItem build() {
        return EstoqueItem.restore(null, varianteUuid, physicalStock, reservedStock, 0L);
    }
}
