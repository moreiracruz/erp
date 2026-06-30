package br.com.moreiracruz.erp.shared.kernel;

import java.util.UUID;

/**
 * Cross-module port for inventory operations used by the Sales module.
 *
 * <p>Implemented by the Inventory module's adapter layer. Defined here in the
 * shared kernel to avoid direct module-to-module coupling.
 */
public interface InventoryPort {

    /**
     * Reserves stock for a sale item.
     *
     * @param varianteUuid the product variant to reserve
     * @param saleUuid     the sale requesting the reservation
     * @param quantity     units to reserve
     * @return -1 if reservation succeeded, otherwise returns available stock
     */
    int reserve(UUID varianteUuid, UUID saleUuid, int quantity);

    /**
     * Releases all reservations for a sale (on cancellation).
     *
     * @param saleUuid the sale whose reservations should be released
     */
    void releaseAll(UUID saleUuid);

    /**
     * Commits all reservations for a finalized sale.
     *
     * @param saleUuid the finalized sale whose reservations are committed
     */
    void commitAll(UUID saleUuid);

    /**
     * Registers a physical stock entry with an external reference.
     *
     * @param varianteUuid  the product variant
     * @param quantity      units entering stock
     * @param actorUuid     operator responsible for the movement
     * @param referenceUuid business reference, such as a consignment contract UUID
     */
    void registerEntry(UUID varianteUuid, int quantity, UUID actorUuid, UUID referenceUuid);

    /**
     * Registers a physical stock withdrawal with an external reference.
     *
     * @param varianteUuid  the product variant
     * @param quantity      units leaving stock
     * @param actorUuid     operator responsible for the movement
     * @param referenceUuid business reference, such as a consignment contract UUID
     */
    void registerWithdrawal(UUID varianteUuid, int quantity, UUID actorUuid, UUID referenceUuid);
}
