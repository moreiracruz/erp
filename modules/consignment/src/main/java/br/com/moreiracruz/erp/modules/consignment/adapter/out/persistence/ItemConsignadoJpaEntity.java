package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "itens_consignados")
public class ItemConsignadoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(name = "contrato_uuid", nullable = false)
    private UUID contratoUuid;
    @Column(name = "variante_uuid", nullable = false)
    private UUID varianteUuid;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "remaining_quantity", nullable = false)
    private int remainingQuantity;
    @Column(name = "sold_quantity", nullable = false)
    private int soldQuantity;
    @Column(name = "settled_quantity", nullable = false)
    private int settledQuantity;
    @Column(name = "returned_quantity", nullable = false)
    private int returnedQuantity;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
    @Column(name = "sold_sale_uuid")
    private UUID soldSaleUuid;
    @Column(name = "returned_at")
    private Instant returnedAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (receivedAt == null) receivedAt = Instant.now();
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public UUID getContratoUuid() { return contratoUuid; }
    public void setContratoUuid(UUID contratoUuid) { this.contratoUuid = contratoUuid; }
    public UUID getVarianteUuid() { return varianteUuid; }
    public void setVarianteUuid(UUID varianteUuid) { this.varianteUuid = varianteUuid; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public int getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }
    public int getSettledQuantity() { return settledQuantity; }
    public void setSettledQuantity(int settledQuantity) { this.settledQuantity = settledQuantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(int returnedQuantity) { this.returnedQuantity = returnedQuantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public UUID getSoldSaleUuid() { return soldSaleUuid; }
    public void setSoldSaleUuid(UUID soldSaleUuid) { this.soldSaleUuid = soldSaleUuid; }
    public Instant getReturnedAt() { return returnedAt; }
    public void setReturnedAt(Instant returnedAt) { this.returnedAt = returnedAt; }
}
