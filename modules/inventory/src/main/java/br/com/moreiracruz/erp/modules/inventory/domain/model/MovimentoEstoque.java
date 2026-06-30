package br.com.moreiracruz.erp.modules.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity that records a single stock movement event for audit and traceability.
 *
 * <p>Movements are immutable after creation. Use the {@link #of} factory to construct instances.
 */
public class MovimentoEstoque {

    private Long id;
    private UUID uuid;
    private UUID varianteUuid;
    private OperationType operationType;
    private int quantity;
    private Instant occurredAt;
    /** Nullable — null means the operation was performed by the SYSTEM. */
    private UUID actorUuid;
    /** Nullable — sale UUID when related to a reservation. */
    private UUID referenceUuid;

    // Required by JPA
    protected MovimentoEstoque() {}

    private MovimentoEstoque(UUID varianteUuid, OperationType operationType,
                              int quantity, UUID actorUuid, UUID referenceUuid) {
        this.uuid = UUID.randomUUID();
        this.varianteUuid = varianteUuid;
        this.operationType = operationType;
        this.quantity = quantity;
        this.occurredAt = Instant.now();
        this.actorUuid = actorUuid;
        this.referenceUuid = referenceUuid;
    }

    /**
     * Factory method to create a new {@code MovimentoEstoque}.
     *
     * @param varianteUuid  the variant this movement belongs to (required)
     * @param type          the operation type (required)
     * @param qty           the movement quantity (must be > 0)
     * @param actorUuid     the operator UUID, or {@code null} for system operations
     * @param referenceUuid the related sale UUID, or {@code null} if not applicable
     */
    public static MovimentoEstoque of(UUID varianteUuid, OperationType type, int qty,
                                       UUID actorUuid, UUID referenceUuid) {
        return new MovimentoEstoque(varianteUuid, type, qty, actorUuid, referenceUuid);
    }

    /**
     * Reconstitutes a {@code MovimentoEstoque} from a persistence store (DDD restore pattern).
     *
     * @param id            internal surrogate key from the database
     * @param uuid          public UUID of the movement record
     * @param varianteUuid  the variant this movement belongs to
     * @param type          the operation type
     * @param qty           the movement quantity
     * @param occurredAt    the timestamp from the database
     * @param actorUuid     the operator UUID, or {@code null}
     * @param referenceUuid the related sale UUID, or {@code null}
     */
    public static MovimentoEstoque restore(Long id, UUID uuid, UUID varianteUuid,
                                           OperationType type, int qty, Instant occurredAt,
                                           UUID actorUuid, UUID referenceUuid) {
        MovimentoEstoque m = new MovimentoEstoque();
        m.id = id;
        m.uuid = uuid;
        m.varianteUuid = varianteUuid;
        m.operationType = type;
        m.quantity = qty;
        m.occurredAt = occurredAt;
        m.actorUuid = actorUuid;
        m.referenceUuid = referenceUuid;
        return m;
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

    public OperationType getOperationType() {
        return operationType;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getActorUuid() {
        return actorUuid;
    }

    public UUID getReferenceUuid() {
        return referenceUuid;
    }
}
