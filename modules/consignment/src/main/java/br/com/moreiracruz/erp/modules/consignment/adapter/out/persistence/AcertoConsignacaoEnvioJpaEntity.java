package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "acertos_consignacao_envio")
public class AcertoConsignacaoEnvioJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(name = "contrato_uuid", nullable = false)
    private UUID contratoUuid;
    @Column(name = "responsible_uuid", nullable = false)
    private UUID responsibleUuid;
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    private String notes;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public UUID getContratoUuid() { return contratoUuid; }
    public void setContratoUuid(UUID contratoUuid) { this.contratoUuid = contratoUuid; }
    public UUID getResponsibleUuid() { return responsibleUuid; }
    public void setResponsibleUuid(UUID responsibleUuid) { this.responsibleUuid = responsibleUuid; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
