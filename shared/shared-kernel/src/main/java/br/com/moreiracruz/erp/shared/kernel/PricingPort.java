package br.com.moreiracruz.erp.shared.kernel;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Cross-module port for pricing/discount operations used by the Sales module.
 *
 * <p>Implemented by the Pricing module's adapter layer. Defined here in the
 * shared kernel to avoid direct module-to-module coupling.
 */
public interface PricingPort {

    /**
     * Calculates discount without modifying coupon state.
     *
     * @param saleUuid   the sale being priced
     * @param items      line items in the sale
     * @param subtotal   the sale's subtotal
     * @param couponCode the coupon code (nullable)
     * @return discount amount (ZERO if no discount applies)
     */
    BigDecimal calculateDiscount(UUID saleUuid, List<ItemLine> items,
                                 BigDecimal subtotal, String couponCode);

    /**
     * Atomically confirms coupon usage after sale finalization.
     *
     * @param couponCode the coupon code to confirm
     */
    void confirmCouponUsage(String couponCode);

    /**
     * Represents a sale line item for discount calculation purposes.
     */
    record ItemLine(UUID varianteUuid, int quantity, BigDecimal unitPrice) {}
}
