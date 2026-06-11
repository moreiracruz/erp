package com.erp.infrastructure.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping to the {@code reservas_estoque} table.
 *
 * <p>Pure persistence concern — domain logic lives in
 * {@link com.erp.modules.inventory.domain.model.ReservaEstoque}.
 */
@Entity
@Table(name = "reservas_estoque")
public class ReservaEstoqueJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "variante_uuid", nullable = false)
    private UUID varianteUuid;

    @Column(name = "sale_uuid", nullable = false)
    private UUID saleUuid;

    @Column(nullable = false)
    private int quantity;

    /**
     * Stored as a VARCHAR(15) matching {@link com.erp.modules.inventory.domain.model.ReservaStatus}
     * names: 'ACTIVE', 'COMMITTED', 'RELEASED', 'EXPIRED'.
     */
    @Column(nullable = false, length = 15)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Required by JPA. */
    protected ReservaEstoqueJpaEntity() {}

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public UUID getVarianteUuid() { return varianteUuid; }
    public void setVarianteUuid(UUID varianteUuid) { this.varianteUuid = varianteUuid; }

    public UUID getSaleUuid() { return saleUuid; }
    public void setSaleUuid(UUID saleUuid) { this.saleUuid = saleUuid; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
