package br.com.moreiracruz.erp.shared.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Payload for the SaleCompleted domain event.
 * Published by the sales module when a sale is finalised.
 */
public record SaleCompletedPayload(
        UUID saleUuid,
        UUID operatorUuid,
        List<SaleItem> items,
        BigDecimal total,
        String paymentMethod
) {

    /**
     * A single line item within a completed sale.
     */
    public record SaleItem(
            String sku,
            int quantity
    ) {}
}
