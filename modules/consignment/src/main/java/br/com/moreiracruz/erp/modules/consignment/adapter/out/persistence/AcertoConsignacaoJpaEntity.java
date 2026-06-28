package br.com.moreiracruz.erp.modules.consignment.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "acertos_consignacao")
public class AcertoConsignacaoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(name = "contrato_uuid", nullable = false)
    private UUID contratoUuid;
    @Column(name = "responsible_uuid", nullable = false)
    private UUID responsibleUuid;
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    @Column(length = 500)
    private String notes;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
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
