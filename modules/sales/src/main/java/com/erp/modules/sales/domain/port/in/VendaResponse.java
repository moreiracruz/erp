package com.erp.modules.sales.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Read model for a sale returned by use cases.
 */
public record VendaResponse(
        UUID uuid,
        UUID operatorUuid,
        String terminalId,
        UUID clienteUuid,
        String status,
        String paymentMethod,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal total,
        BigDecimal changeAmount,
        List<ItemVendaResponse> items,
        Instant createdAt
) {}
