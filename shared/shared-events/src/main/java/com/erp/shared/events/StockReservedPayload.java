package com.erp.shared.events;

import java.util.UUID;

/**
 * Payload for the StockReserved domain event.
 * Published by the inventory module when stock is reserved for a sale.
 */
public record StockReservedPayload(
        UUID varianteUuid,
        int reservedQuantity,
        UUID saleUuid
) {}
