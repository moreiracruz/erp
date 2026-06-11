package com.erp.modules.sales.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read model returned after successful finalization of a sale.
 */
public record FinalizationResponse(
        UUID uuid,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal total,
        BigDecimal changeAmount,
        String paymentMethod
) {}
