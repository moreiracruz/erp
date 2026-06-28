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
@Table(name = "contratos_consignacao")
public class ContratoConsignacaoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(name = "consignante_uuid", nullable = false)
    private UUID consignanteUuid;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;
    @Column(name = "closed_at")
    private Instant closedAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (openedAt == null) openedAt = Instant.now();
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public UUID getConsignanteUuid() { return consignanteUuid; }
    public void setConsignanteUuid(UUID consignanteUuid) { this.consignanteUuid = consignanteUuid; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getOpenedAt() { return openedAt; }
    public void setOpenedAt(Instant openedAt) { this.openedAt = openedAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}
