package br.com.moreiracruz.erp.modules.pricing.application.usecase;

import br.com.moreiracruz.erp.modules.pricing.domain.model.CampaignType;
import br.com.moreiracruz.erp.modules.pricing.domain.model.Campanha;
import br.com.moreiracruz.erp.modules.pricing.domain.model.Cupom;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.AppliedRule;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CalculateDiscountUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.DiscountQuery;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.DiscountResult;
import br.com.moreiracruz.erp.modules.pricing.domain.port.out.CampanhaRepository;
import br.com.moreiracruz.erp.modules.pricing.domain.port.out.CupomRepository;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalculateDiscountUseCaseImpl implements CalculateDiscountUseCase {

    private final CupomRepository cupomRepository;
    private final CampanhaRepository campanhaRepository;

    public CalculateDiscountUseCaseImpl(CupomRepository cupomRepository,
                                        CampanhaRepository campanhaRepository) {
        this.cupomRepository = cupomRepository;
        this.campanhaRepository = campanhaRepository;
    }

    @Override
    public DiscountResult calculate(DiscountQuery query) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCashback = BigDecimal.ZERO;
        List<AppliedRule> appliedRules = new ArrayList<>();
        Instant now = Instant.now();

        // 1. Apply coupon discount first (if present)
        if (query.couponCode() != null && !query.couponCode().isBlank()) {
            Cupom cupom = cupomRepository.findByCodeIgnoreCase(query.couponCode())
                    .orElseThrow(() -> new ValidationException("CUPOM_INATIVO"));

            if (!cupom.isActive()) {
                throw new ValidationException("CUPOM_INATIVO");
            }
            if (now.isAfter(cupom.getEndsAt()) || now.isBefore(cupom.getStartsAt())) {
                throw new ValidationException("CUPOM_EXPIRADO");
            }
            if (cupom.getUsageCount() >= cupom.getMaxUsages()) {
                throw new ValidationException("CUPOM_ESGOTADO");
            }

            BigDecimal couponDiscount = calculateCouponDiscount(cupom, query.subtotal());
            totalDiscount = totalDiscount.add(couponDiscount);
            appliedRules.add(new AppliedRule("COUPON", cupom.getCode(), couponDiscount));
        }

        // 2. Apply active campaigns (percentage first, then fixed)
        List<Campanha> activeCampaigns = campanhaRepository.findAllActive();

        // Percentage campaigns
        for (Campanha campaign : activeCampaigns) {
            if (campaign.getType() == CampaignType.PERCENTAGE && isWithinDates(campaign, now)) {
                BigDecimal discount = query.subtotal()
                        .multiply(campaign.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalDiscount = totalDiscount.add(discount);
                appliedRules.add(new AppliedRule("CAMPAIGN", campaign.getName(), discount));

                if (campaign.getCashbackPct() != null) {
                    BigDecimal cashback = query.subtotal()
                            .multiply(campaign.getCashbackPct())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    totalCashback = totalCashback.add(cashback);
                }
            }
        }

        // Fixed campaigns
        for (Campanha campaign : activeCampaigns) {
            if (campaign.getType() == CampaignType.FIXED && isWithinDates(campaign, now)) {
                BigDecimal discount = campaign.getDiscountValue();
                totalDiscount = totalDiscount.add(discount);
                appliedRules.add(new AppliedRule("CAMPAIGN", campaign.getName(), discount));

                if (campaign.getCashbackPct() != null) {
                    BigDecimal cashback = query.subtotal()
                            .multiply(campaign.getCashbackPct())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    totalCashback = totalCashback.add(cashback);
                }
            }
        }

        // Cap discount at subtotal (never go negative)
        if (totalDiscount.compareTo(query.subtotal()) > 0) {
            totalDiscount = query.subtotal();
        }

        return new DiscountResult(totalDiscount, totalCashback, appliedRules);
    }

    private BigDecimal calculateCouponDiscount(Cupom cupom, BigDecimal subtotal) {
        return switch (cupom.getType()) {
            case PERCENTAGE -> subtotal
                    .multiply(cupom.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED -> cupom.getDiscountValue().min(subtotal);
            case PROGRESSIVE -> cupom.getDiscountValue();
        };
    }

    private boolean isWithinDates(Campanha campaign, Instant now) {
        return !now.isBefore(campaign.getStartsAt()) && !now.isAfter(campaign.getEndsAt());
    }
}
