package br.com.moreiracruz.erp.modules.pricing.domain.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DiscountQuery(
        UUID saleUuid,
        List<ItemLine> items,
        BigDecimal subtotal,
        String couponCode
) {
    public record ItemLine(UUID varianteUuid, int quantity, BigDecimal unitPrice) {}
}
