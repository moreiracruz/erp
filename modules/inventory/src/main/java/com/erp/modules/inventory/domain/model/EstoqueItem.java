package com.erp.modules.inventory.domain.model;

import com.erp.shared.exceptions.InsufficientStockException;
import com.erp.shared.exceptions.ValidationException;
import com.erp.shared.kernel.AggregateRoot;

import java.util.UUID;

/**
 * Aggregate root that tracks physical and reserved stock for a single product variant.
 *
 * <p>Invariant: {@code physicalStock - reservedStock >= 0} must hold after every mutation.
 * If a bug causes this to be violated, an {@link IllegalStateException} is thrown.
 */
public class EstoqueItem extends AggregateRoot {

    private static final int MAX_QTY = 100_000;

    private UUID varianteUuid;
    private int physicalStock;
    private int reservedStock;
    private Long version;

    // Required by JPA
    protected EstoqueItem() {}

    private EstoqueItem(UUID varianteUuid) {
        this.varianteUuid = varianteUuid;
        this.physicalStock = 0;
        this.reservedStock = 0;
        this.version = 0L;
    }

    /**
     * Creates a new {@code EstoqueItem} for the given variant with zeroed counters.
     */
    public static EstoqueItem create(UUID varianteUuid) {
        if (varianteUuid == null) {
            throw new ValidationException("varianteUuid", "cannot be null");
        }
        return new EstoqueItem(varianteUuid);
    }

    /**
     * Reconstitutes an {@code EstoqueItem} from a persistence store (DDD restore pattern).
     *
     * <p>This factory bypasses business-rule validation and sets all fields directly,
     * because the data coming from the database is already assumed to be valid.
     *
     * @param surrogateId   internal surrogate key from the database
     * @param varianteUuid  the variant UUID
     * @param physicalStock stored physical stock value
     * @param reservedStock stored reserved stock value
     * @param version       optimistic-lock version from the database
     */
    public static EstoqueItem restore(Long surrogateId, UUID varianteUuid,
                                      int physicalStock, int reservedStock, Long version) {
        EstoqueItem item = new EstoqueItem(varianteUuid);
        item.id = surrogateId;
        item.physicalStock = physicalStock;
        item.reservedStock = reservedStock;
        item.version = version;
        return item;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /** Available stock = physicalStock - reservedStock. */
    public int availableStock() {
        return physicalStock - reservedStock;
    }

    // -------------------------------------------------------------------------
    // Mutations
    // -------------------------------------------------------------------------

    /**
     * Increases physical stock by {@code qty}.
     *
     * @param qty amount to add; must be between 1 and 100 000 (inclusive)
     * @throws ValidationException if qty is out of range
     */
    public void incrementPhysical(int qty) {
        validateQtyRange(qty);
        physicalStock += qty;
        assertInvariant();
    }

    /**
     * Decreases physical stock by {@code qty}.
     *
     * @param qty amount to remove; must be between 1 and 100 000 (inclusive)
     * @throws ValidationException           if qty is out of range
     * @throws InsufficientStockException    if physicalStock < qty
     */
    public void decrementPhysical(int qty) {
        validateQtyRange(qty);
        if (physicalStock < qty) {
            throw new InsufficientStockException(physicalStock);
        }
        if ((physicalStock - qty) < reservedStock) {
            throw new InsufficientStockException(physicalStock - reservedStock);
        }
        physicalStock -= qty;
        assertInvariant();
    }

    /**
     * Increases reserved stock by {@code qty}.
     *
     * @param qty amount to reserve; must be >= 1
     * @throws ValidationException        if qty < 1
     * @throws InsufficientStockException if availableStock() < qty
     */
    public void incrementReserved(int qty) {
        if (qty < 1) {
            throw new ValidationException("qty", "must be >= 1");
        }
        if (availableStock() < qty) {
            throw new InsufficientStockException(availableStock());
        }
        reservedStock += qty;
        assertInvariant();
    }

    /**
     * Decreases reserved stock by {@code qty}.
     *
     * @param qty amount to release; must be >= 1
     * @throws ValidationException if qty < 1 or reservedStock < qty
     */
    public void decrementReserved(int qty) {
        if (qty < 1) {
            throw new ValidationException("qty", "must be >= 1");
        }
        if (reservedStock < qty) {
            throw new ValidationException("qty", "cannot release more than reserved stock (" + reservedStock + ")");
        }
        reservedStock -= qty;
        assertInvariant();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateQtyRange(int qty) {
        if (qty < 1 || qty > MAX_QTY) {
            throw new ValidationException("qty", "must be between 1 and " + MAX_QTY + ", got " + qty);
        }
    }

    /**
     * Asserts the core domain invariant. If violated by a bug, throws {@link IllegalStateException}
     * to surface programming errors immediately rather than silently corrupting data.
     */
    private void assertInvariant() {
        if (physicalStock - reservedStock < 0) {
            throw new IllegalStateException(
                    "Invariant violation: physicalStock (" + physicalStock +
                    ") - reservedStock (" + reservedStock + ") < 0");
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public UUID getVarianteUuid() {
        return varianteUuid;
    }

    public int getPhysicalStock() {
        return physicalStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public Long getVersion() {
        return version;
    }
}
