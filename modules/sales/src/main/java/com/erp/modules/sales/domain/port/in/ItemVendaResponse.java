package com.erp.modules.sales.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read model for a single sale line item.
 */
public record ItemVendaResponse(
        UUID varianteUuid,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {}
