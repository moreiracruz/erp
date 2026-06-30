package br.com.moreiracruz.erp.infrastructure.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

/**
 * JPA entity mapping to the {@code estoque_items} table.
 *
 * <p>Pure persistence concern — domain logic lives in
 * {@link br.com.moreiracruz.erp.modules.inventory.domain.model.EstoqueItem}.
 */
@Entity
@Table(name = "estoque_items")
public class EstoqueItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variante_uuid", nullable = false, unique = true)
    private UUID varianteUuid;

    @Column(name = "physical_stock", nullable = false)
    private int physicalStock;

    @Column(name = "reserved_stock", nullable = false)
    private int reservedStock;

    @Version
    @Column(nullable = false)
    private Long version;

    /** Required by JPA. */
    protected EstoqueItemJpaEntity() {}

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getVarianteUuid() { return varianteUuid; }
    public void setVarianteUuid(UUID varianteUuid) { this.varianteUuid = varianteUuid; }

    public int getPhysicalStock() { return physicalStock; }
    public void setPhysicalStock(int physicalStock) { this.physicalStock = physicalStock; }

    public int getReservedStock() { return reservedStock; }
    public void setReservedStock(int reservedStock) { this.reservedStock = reservedStock; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
