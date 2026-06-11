package com.erp.modules.finance.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lancamentos_financeiros")
public class LancamentoFinanceiroJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 10)
    private String paymentMethod;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 20)
    private String category;

    @Column(name = "competence_date", nullable = false)
    private LocalDate competenceDate;

    @Column(name = "responsible_uuid", nullable = false)
    private UUID responsibleUuid;

    @Column(name = "sale_uuid", unique = true)
    private UUID saleUuid;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getCompetenceDate() { return competenceDate; }
    public void setCompetenceDate(LocalDate competenceDate) { this.competenceDate = competenceDate; }

    public UUID getResponsibleUuid() { return responsibleUuid; }
    public void setResponsibleUuid(UUID responsibleUuid) { this.responsibleUuid = responsibleUuid; }

    public UUID getSaleUuid() { return saleUuid; }
    public void setSaleUuid(UUID saleUuid) { this.saleUuid = saleUuid; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
