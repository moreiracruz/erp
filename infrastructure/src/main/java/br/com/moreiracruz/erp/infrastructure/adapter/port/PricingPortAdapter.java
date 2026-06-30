package br.com.moreiracruz.erp.infrastructure.adapter.port;

import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CalculateDiscountUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.ConfirmCouponUsageUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.DiscountQuery;
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
 */
@Component
public class PricingPortAdapter implements PricingPort {

    private final CalculateDiscountUseCase calculateDiscountUseCase;
    private final ConfirmCouponUsageUseCase confirmCouponUsageUseCase;

    public PricingPortAdapter(CalculateDiscountUseCase calculateDiscountUseCase,
                              ConfirmCouponUsageUseCase confirmCouponUsageUseCase) {
        this.calculateDiscountUseCase = calculateDiscountUseCase;
        this.confirmCouponUsageUseCase = confirmCouponUsageUseCase;
    }

    @Override
    public BigDecimal calculateDiscount(UUID saleUuid, List<ItemLine> items,
                                        BigDecimal subtotal, String couponCode) {
        List<DiscountQuery.ItemLine> queryItems = items.stream()
                .map(item -> new DiscountQuery.ItemLine(
                        item.varianteUuid(), item.quantity(), item.unitPrice()))
                .toList();

        return calculateDiscountUseCase.calculate(
                new DiscountQuery(saleUuid, queryItems, subtotal, couponCode))
                .discountAmount();
    }

    @Override
    public void confirmCouponUsage(String couponCode) {
        confirmCouponUsageUseCase.confirm(couponCode);
    }
}
