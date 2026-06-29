package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "itens_consignacao_envio")
public class ItemConsignacaoEnvioJpaEntity {
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
    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;
    @Column(name = "sold_quantity", nullable = false)
    private int soldQuantity;
    @Column(name = "settled_quantity", nullable = false)
    private int settledQuantity;
    @Column(name = "returned_quantity", nullable = false)
    private int returnedQuantity;
    @Column(nullable = false)
    private String status;
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
    @Column(name = "returned_at")
    private Instant returnedAt;

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public UUID getContratoUuid() { return contratoUuid; }
    public void setContratoUuid(UUID contratoUuid) { this.contratoUuid = contratoUuid; }
    public UUID getVarianteUuid() { return varianteUuid; }
    public void setVarianteUuid(UUID varianteUuid) { this.varianteUuid = varianteUuid; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public int getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }
    public int getSettledQuantity() { return settledQuantity; }
    public void setSettledQuantity(int settledQuantity) { this.settledQuantity = settledQuantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(int returnedQuantity) { this.returnedQuantity = returnedQuantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public Instant getReturnedAt() { return returnedAt; }
    public void setReturnedAt(Instant returnedAt) { this.returnedAt = returnedAt; }
}
