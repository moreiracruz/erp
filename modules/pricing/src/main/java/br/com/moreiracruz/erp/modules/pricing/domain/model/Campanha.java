package br.com.moreiracruz.erp.modules.pricing.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root representing a pricing campaign (discount, cashback, progressive).
 *
 * <p>A campaign defines discount rules that can target a specific product,
 * a product category, or all products within a date range.
 */
public class Campanha extends AggregateRoot {

    private String name;
    private CampaignType type;
    private TargetType targetType;
    private UUID targetUuid;
    private String targetCategory;
    private BigDecimal discountValue;
    private Integer minQuantity;
    private BigDecimal cashbackPct;
    private Instant startsAt;
    private Instant endsAt;
    private boolean active;

    // Required by JPA
    protected Campanha() {}

    private Campanha(String name, CampaignType type, TargetType targetType,
                     UUID targetUuid, String targetCategory, BigDecimal discountValue,
                     Integer minQuantity, BigDecimal cashbackPct,
                     Instant startsAt, Instant endsAt) {
        this.name = name;
        this.type = type;
        this.targetType = targetType;
        this.targetUuid = targetUuid;
        this.targetCategory = targetCategory;
        this.discountValue = discountValue;
        this.minQuantity = minQuantity;
        this.cashbackPct = cashbackPct;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.active = true;
    }

    /**
     * Restores a Campanha from persistence (bypasses validation).
     */
    public static Campanha restore(Long id, UUID uuid, String name, CampaignType type,
                                   TargetType targetType, UUID targetUuid, String targetCategory,
                                   BigDecimal discountValue, Integer minQuantity,
                                   BigDecimal cashbackPct, Instant startsAt, Instant endsAt,
                                   boolean active) {
        Campanha c = new Campanha();
        c.id = id;
        c.uuid = uuid;
        c.name = name;
        c.type = type;
        c.targetType = targetType;
        c.targetUuid = targetUuid;
        c.targetCategory = targetCategory;
        c.discountValue = discountValue;
        c.minQuantity = minQuantity;
        c.cashbackPct = cashbackPct;
        c.startsAt = startsAt;
        c.endsAt = endsAt;
        c.active = active;
        return c;
    }

    /**
     * Factory method that creates a new campaign after validating all business rules.
     *
     * @throws ValidationException if any validation rule is violated
     */
    public static Campanha create(String name, CampaignType type, TargetType targetType,
                                  UUID targetUuid, String targetCategory,
                                  BigDecimal discountValue, Integer minQuantity,
                                  BigDecimal cashbackPct, Instant startsAt, Instant endsAt) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("name", "cannot be null or blank");
        }
        if (type == null) {
            throw new ValidationException("type", "cannot be null");
        }
        if (targetType == null) {
            throw new ValidationException("targetType", "cannot be null");
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
        if (type == CampaignType.PROGRESSIVE) {
            if (minQuantity == null || minQuantity < 2) {
                throw new ValidationException("minQuantity", "must be >= 2 for PROGRESSIVE campaigns");
            }
        }
        if (cashbackPct != null) {
            if (cashbackPct.compareTo(new BigDecimal("0.01")) < 0
                    || cashbackPct.compareTo(new BigDecimal("50.00")) > 0) {
                throw new ValidationException("cashbackPct", "must be between 0.01 and 50.00");
            }
        }

        return new Campanha(name, type, targetType, targetUuid, targetCategory,
                discountValue, minQuantity, cashbackPct, startsAt, endsAt);
    }

    // -------------------------------------------------------------------------
    // Domain behavior
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this campaign overlaps with another campaign of the same type
     * and target, with overlapping date ranges.
     */
    public boolean overlaps(Campanha other) {
        if (other == null) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.targetType != other.targetType) {
            return false;
        }
        if (!sameTarget(other)) {
            return false;
        }
        // Date ranges overlap: this.startsAt < other.endsAt && this.endsAt > other.startsAt
        return this.startsAt.isBefore(other.endsAt) && this.endsAt.isAfter(other.startsAt);
    }

    /**
     * Deactivates this campaign.
     */
    public void deactivate() {
        this.active = false;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private boolean sameTarget(Campanha other) {
        return switch (this.targetType) {
            case PRODUTO -> Objects.equals(this.targetUuid, other.targetUuid);
            case CATEGORY -> Objects.equals(this.targetCategory, other.targetCategory);
            case ALL -> true;
        };
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public CampaignType getType() {
        return type;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetCategory() {
        return targetCategory;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public BigDecimal getCashbackPct() {
        return cashbackPct;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public boolean isActive() {
        return active;
    }
}
