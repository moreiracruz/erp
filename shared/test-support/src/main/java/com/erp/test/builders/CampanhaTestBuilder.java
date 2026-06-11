package com.erp.test.builders;

import com.erp.modules.pricing.domain.model.Campanha;
import com.erp.modules.pricing.domain.model.CampaignType;
import com.erp.modules.pricing.domain.model.TargetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Fluent builder for creating {@link Campanha} domain objects in tests.
 */
public class CampanhaTestBuilder {

    private String name = "Promoção de Verão";
    private CampaignType type = CampaignType.PERCENTAGE;
    private TargetType targetType = TargetType.ALL;
    private UUID targetUuid = null;
    private String targetCategory = null;
    private BigDecimal discountValue = new BigDecimal("15.00");
    private Integer minQuantity = null;
    private BigDecimal cashbackPct = null;
    private Instant startsAt = Instant.now().minus(1, ChronoUnit.DAYS);
    private Instant endsAt = Instant.now().plus(30, ChronoUnit.DAYS);

    private CampanhaTestBuilder() {}

    public static CampanhaTestBuilder aPercentageCampaign() {
        return new CampanhaTestBuilder()
                .withType(CampaignType.PERCENTAGE)
                .withDiscountValue(new BigDecimal("15.00"));
    }

    public static CampanhaTestBuilder aFixedCampaign() {
        return new CampanhaTestBuilder()
                .withType(CampaignType.FIXED)
                .withDiscountValue(new BigDecimal("25.00"));
    }

    public CampanhaTestBuilder withName(String name) { this.name = name; return this; }
    public CampanhaTestBuilder withType(CampaignType type) { this.type = type; return this; }
    public CampanhaTestBuilder withTargetType(TargetType targetType) { this.targetType = targetType; return this; }
    public CampanhaTestBuilder withTargetUuid(UUID targetUuid) { this.targetUuid = targetUuid; return this; }
    public CampanhaTestBuilder withTargetCategory(String targetCategory) { this.targetCategory = targetCategory; return this; }
    public CampanhaTestBuilder withDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; return this; }
    public CampanhaTestBuilder withMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; return this; }
    public CampanhaTestBuilder withCashbackPct(BigDecimal cashbackPct) { this.cashbackPct = cashbackPct; return this; }
    public CampanhaTestBuilder withStartsAt(Instant startsAt) { this.startsAt = startsAt; return this; }
    public CampanhaTestBuilder withEndsAt(Instant endsAt) { this.endsAt = endsAt; return this; }

    /** Build domain object using the create factory method. */
    public Campanha build() {
        return Campanha.create(name, type, targetType, targetUuid, targetCategory,
                discountValue, minQuantity, cashbackPct, startsAt, endsAt);
    }
}
