package br.com.moreiracruz.erp.infrastructure.adapter.port;

import br.com.moreiracruz.erp.shared.kernel.PricingPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Adapter that bridges the shared-kernel {@link PricingPort} to the Pricing
 * module's inbound use case ports. Lives in infrastructure to avoid lateral
 * module dependencies.
 *
 * <p>NOTE: The pricing module's CalculateDiscountUseCase and ConfirmCouponUsageUseCase
 * are not yet implemented. This adapter provides a no-op/pass-through implementation
 * until the pricing module is fully wired.
 */
@Component
public class PricingPortAdapter implements PricingPort {

    @Override
    public BigDecimal calculateDiscount(UUID saleUuid, List<ItemLine> items,
                                        BigDecimal subtotal, String couponCode) {
        // TODO: Delegate to pricing module's CalculateDiscountUseCase once available
        return BigDecimal.ZERO;
    }

    @Override
    public void confirmCouponUsage(String couponCode) {
        // TODO: Delegate to pricing module's ConfirmCouponUsageUseCase once available
    }
}
