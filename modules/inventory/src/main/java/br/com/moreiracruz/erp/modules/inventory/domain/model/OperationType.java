package br.com.moreiracruz.erp.modules.inventory.domain.model;

/**
 * Represents the type of stock movement operation.
 *
 * <p>Note: Java enum names use ASCII identifiers. The database stores the
 * Portuguese forms: 'ENTRADA', 'SAÍDA', 'RESERVA', 'LIBERAÇÃO_RESERVA'.
 * Persistence adapters must handle the mapping between these names.
 */
public enum OperationType {
    /** Stock entry — increases physicalStock. */
    ENTRADA,

    /** Stock withdrawal — decreases physicalStock. */
    SAIDA,

    /** Stock reservation — increases reservedStock. */
    RESERVA,

    /** Release of a reservation — decreases reservedStock. */
    LIBERACAO_RESERVA
}
