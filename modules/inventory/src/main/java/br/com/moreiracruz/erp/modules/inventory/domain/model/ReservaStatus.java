package br.com.moreiracruz.erp.modules.inventory.domain.model;

/**
 * Lifecycle status of a stock reservation.
 */
public enum ReservaStatus {
    /** Reservation is active and stock is held. */
    ACTIVE,

    /** Reservation was committed (sale finalized). */
    COMMITTED,

    /** Reservation was explicitly released before commitment. */
    RELEASED,

    /** Reservation expired before being committed or released. */
    EXPIRED
}
