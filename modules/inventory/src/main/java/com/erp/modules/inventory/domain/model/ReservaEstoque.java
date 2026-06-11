package com.erp.modules.inventory.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Entity that represents a stock reservation tied to a pending sale.
 *
 * <p>A reservation transitions through: {@code ACTIVE → COMMITTED | RELEASED | EXPIRED}.
 * Use the {@link #create} factory to construct instances.
 */
public class ReservaEstoque {

    private Long id;
    private UUID uuid;
    private UUID varianteUuid;
    private UUID saleUuid;
    private int quantity;
    private ReservaStatus status;
    private Instant createdAt;
    private Instant expiresAt;

    // Required by JPA
    protected ReservaEstoque() {}

    private ReservaEstoque(UUID varianteUuid, UUID saleUuid, int quantity, Instant createdAt, Instant expiresAt) {
        this.uuid = UUID.randomUUID();
        this.varianteUuid = varianteUuid;
        this.saleUuid = saleUuid;
        this.quantity = quantity;
        this.status = ReservaStatus.ACTIVE;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Creates a new {@code ReservaEstoque} with {@code ACTIVE} status and a computed expiry time.
     *
     * @param varianteUuid the variant being reserved (required)
     * @param saleUuid     the associated sale UUID (required)
     * @param qty          quantity to reserve (must be > 0)
     * @param ttlMinutes   time-to-live in minutes; expiry = now + ttlMinutes
     */
    public static ReservaEstoque create(UUID varianteUuid, UUID saleUuid, int qty, long ttlMinutes) {
        Instant now = Instant.now();
        return new ReservaEstoque(varianteUuid, saleUuid, qty, now, now.plus(ttlMinutes, ChronoUnit.MINUTES));
    }

    /**
     * Reconstitutes a {@code ReservaEstoque} from a persistence store (DDD restore pattern).
     *
     * @param id           internal surrogate key from the database
     * @param uuid         public UUID of the reservation record
     * @param varianteUuid the variant being reserved
     * @param saleUuid     the associated sale UUID
     * @param qty          quantity reserved
     * @param status       status string (must match a {@link ReservaStatus} name)
     * @param createdAt    creation timestamp from the database
     * @param expiresAt    expiry timestamp from the database
     */
    public static ReservaEstoque restore(Long id, UUID uuid, UUID varianteUuid, UUID saleUuid,
                                         int qty, ReservaStatus status,
                                         Instant createdAt, Instant expiresAt) {
        ReservaEstoque r = new ReservaEstoque();
        r.id = id;
        r.uuid = uuid;
        r.varianteUuid = varianteUuid;
        r.saleUuid = saleUuid;
        r.quantity = qty;
        r.status = status;
        r.createdAt = createdAt;
        r.expiresAt = expiresAt;
        return r;
    }

    // -------------------------------------------------------------------------
    // State transitions
    // -------------------------------------------------------------------------

    /** Marks this reservation as committed (sale was finalized). */
    public void markCommitted() {
        this.status = ReservaStatus.COMMITTED;
    }

    /** Marks this reservation as released (stock returned to available pool). */
    public void markReleased() {
        this.status = ReservaStatus.RELEASED;
    }

    /** Marks this reservation as expired (TTL elapsed without commitment). */
    public void markExpired() {
        this.status = ReservaStatus.EXPIRED;
    }

    /** Returns {@code true} if this reservation is still active. */
    public boolean isActive() {
        return status == ReservaStatus.ACTIVE;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getVarianteUuid() {
        return varianteUuid;
    }

    public UUID getSaleUuid() {
        return saleUuid;
    }

    public int getQuantity() {
        return quantity;
    }

    public ReservaStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
