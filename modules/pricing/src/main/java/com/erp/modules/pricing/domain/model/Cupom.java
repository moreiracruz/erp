package com.erp.modules.pricing.domain.model;

import com.erp.shared.exceptions.ValidationException;
import com.erp.shared.kernel.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root representing a discount coupon with usage limits and validity window.
 *
 * <p>Coupons are identified by a unique, case-insensitive code and can be used
 * a limited number of times within their active period.
 */
public class Cupom extends AggregateRoot {

    private String code;
    private CampaignType type;
    private BigDecimal discountValue;
    private Instant startsAt;
    private Instant endsAt;
    private int maxUsages;
    private int usageCount;
    private boolean active;
    private Long version;

    // Required by JPA
    protected Cupom() {}

    private Cupom(String code, CampaignType type, BigDecimal discountValue,
                  Instant startsAt, Instant endsAt, int maxUsages) {
        this.code = code;
        this.type = type;
        this.discountValue = discountValue;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.maxUsages = maxUsages;
        this.usageCount = 0;
        this.active = true;
        this.version = 0L;
    }

    /**
     * Restores a Cupom from persistence (bypasses validation).
     */
    public static Cupom restore(Long id, UUID uuid, String code, CampaignType type,
                                BigDecimal discountValue, Instant startsAt, Instant endsAt,
                                int maxUsages, int usageCount, boolean active, Long version) {
        Cupom c = new Cupom();
        c.id = id;
        c.uuid = uuid;
        c.code = code;
        c.type = type;
        c.discountValue = discountValue;
        c.startsAt = startsAt;
        c.endsAt = endsAt;
        c.maxUsages = maxUsages;
        c.usageCount = usageCount;
        c.active = active;
        c.version = version;
        return c;
    }

    /**
     * Factory method that creates a new coupon after validating all business rules.
     *
     * @throws ValidationException if any validation rule is violated
     */
    public static Cupom create(String code, CampaignType type, BigDecimal discountValue,
                               Instant startsAt, Instant endsAt, int maxUsages) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("code", "cannot be null or blank");
        }
        if (type == null) {
            throw new ValidationException("type", "cannot be null");
        }
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("discountValue", "must be greater than zero");
        }
        if (startsAt == null || endsAt == null) {
            throw new ValidationException("dates", "startsAt and endsAt cannot be null");
        }
        if (!startsAt.isBefore(endsAt)) {
            throw new ValidationException("startsAt", "must be before endsAt");
        }
        if (maxUsages < 1) {
            throw new ValidationException("maxUsages", "must be >= 1");
        }

        return new Cupom(code, type, discountValue, startsAt, endsAt, maxUsages);
    }

    // -------------------------------------------------------------------------
    // Domain behavior
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the coupon is valid at the given instant:
     * active, within the date range, and not exhausted.
     */
    public boolean isValidAt(Instant now) {
        return active
                && !now.isBefore(startsAt)
                && !now.isAfter(endsAt)
                && usageCount < maxUsages;
    }

    /**
     * Increments usage count by one.
     *
     * @throws ValidationException if the coupon has already reached its maximum usages
     */
    public void incrementUsage() {
        if (usageCount >= maxUsages) {
            throw new ValidationException("Cupom esgotado");
        }
        usageCount++;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getCode() {
        return code;
    }

    public CampaignType getType() {
        return type;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public int getMaxUsages() {
        return maxUsages;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public boolean isActive() {
        return active;
    }

    public Long getVersion() {
        return version;
    }
}
