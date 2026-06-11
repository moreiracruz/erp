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
 * JPA entity mapping to the {@code movimentos_estoque} table.
 *
 * <p>Pure persistence concern — domain logic lives in
 * {@link com.erp.modules.inventory.domain.model.MovimentoEstoque}.
 */
@Entity
@Table(name = "movimentos_estoque")
public class MovimentoEstoqueJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "variante_uuid", nullable = false)
    private UUID varianteUuid;

    /**
     * Stored as a VARCHAR(25) using Portuguese labels, e.g. 'ENTRADA', 'SAÍDA'.
     * Mapping is performed explicitly in the adapter.
     */
    @Column(name = "operation_type", nullable = false, length = 25)
    private String operationType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** Nullable — null means the operation was performed by the SYSTEM. */
    @Column(name = "actor_uuid")
    private UUID actorUuid;

    /** Nullable — sale UUID when related to a reservation. */
    @Column(name = "reference_uuid")
    private UUID referenceUuid;

    /** Required by JPA. */
    protected MovimentoEstoqueJpaEntity() {}

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public UUID getVarianteUuid() { return varianteUuid; }
    public void setVarianteUuid(UUID varianteUuid) { this.varianteUuid = varianteUuid; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public UUID getActorUuid() { return actorUuid; }
    public void setActorUuid(UUID actorUuid) { this.actorUuid = actorUuid; }

    public UUID getReferenceUuid() { return referenceUuid; }
    public void setReferenceUuid(UUID referenceUuid) { this.referenceUuid = referenceUuid; }
}
