package br.com.moreiracruz.erp.test.builders;

import br.com.moreiracruz.erp.modules.pricing.domain.model.CampaignType;
import br.com.moreiracruz.erp.modules.pricing.domain.model.Cupom;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Fluent builder for creating {@link Cupom} domain objects in tests.
 */
public class CupomTestBuilder {

    private String code = "PROMO10";
    private CampaignType type = CampaignType.PERCENTAGE;
    private BigDecimal discountValue = new BigDecimal("10.00");
    private Instant startsAt = Instant.now().minus(1, ChronoUnit.DAYS);
    private Instant endsAt = Instant.now().plus(30, ChronoUnit.DAYS);
    private int maxUsages = 100;

    private CupomTestBuilder() {}

    public static CupomTestBuilder anActiveCoupon() {
        return new CupomTestBuilder();
    }

    public CupomTestBuilder withCode(String code) { this.code = code; return this; }
    public CupomTestBuilder withType(CampaignType type) { this.type = type; return this; }
    public CupomTestBuilder withDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; return this; }
    public CupomTestBuilder withStartsAt(Instant startsAt) { this.startsAt = startsAt; return this; }
    public CupomTestBuilder withEndsAt(Instant endsAt) { this.endsAt = endsAt; return this; }
    public CupomTestBuilder withMaxUsages(int maxUsages) { this.maxUsages = maxUsages; return this; }

    /** Build domain object using the create factory method. */
    public Cupom build() {
        return Cupom.create(code, type, discountValue, startsAt, endsAt, maxUsages);
    }
}
