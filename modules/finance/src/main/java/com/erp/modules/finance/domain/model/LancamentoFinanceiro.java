package com.erp.modules.finance.domain.model;

import com.erp.shared.exceptions.ValidationException;
import com.erp.shared.kernel.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root for financial entries (revenue/expense).
 */
public class LancamentoFinanceiro extends AggregateRoot {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");

    private EntryType type;
    private BigDecimal amount;
    private String paymentMethod;
    private String description;
    private String category;
    private LocalDate competenceDate;
    private UUID responsibleUuid;
    private UUID saleUuid;
    private Instant createdAt;

    protected LancamentoFinanceiro() {
        // JPA / restore
    }

    public static LancamentoFinanceiro create(EntryType type, BigDecimal amount, String paymentMethod,
                                               String description, String category,
                                               LocalDate competenceDate, UUID responsibleUuid,
                                               UUID saleUuid) {
        var l = new LancamentoFinanceiro();
        l.type = type;
        l.setAmount(amount);
        l.paymentMethod = paymentMethod;
        l.setDescription(description);
        l.category = category;
        l.competenceDate = competenceDate;
        l.responsibleUuid = responsibleUuid;
        l.saleUuid = saleUuid;
        l.createdAt = Instant.now();

        if (type == null) {
            throw new ValidationException("Tipo do lançamento é obrigatório");
        }
        if (competenceDate == null) {
            throw new ValidationException("Data de competência é obrigatória");
        }
        if (responsibleUuid == null) {
            throw new ValidationException("Responsável é obrigatório");
        }

        return l;
    }

    public static LancamentoFinanceiro restore(UUID uuid, EntryType type, BigDecimal amount,
                                                String paymentMethod, String description,
                                                String category, LocalDate competenceDate,
                                                UUID responsibleUuid, UUID saleUuid,
                                                Instant createdAt) {
        var l = new LancamentoFinanceiro();
        l.uuid = uuid;
        l.type = type;
        l.amount = amount;
        l.paymentMethod = paymentMethod;
        l.description = description;
        l.category = category;
        l.competenceDate = competenceDate;
        l.responsibleUuid = responsibleUuid;
        l.saleUuid = saleUuid;
        l.createdAt = createdAt;
        return l;
    }

    private void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(MIN_AMOUNT) < 0 || amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Valor deve estar entre 0.01 e 999999999.99");
        }
        this.amount = amount;
    }

    private void setDescription(String description) {
        if (description == null || description.isBlank() || description.length() > 255) {
            throw new ValidationException("Descrição deve ter entre 1 e 255 caracteres");
        }
        this.description = description.trim();
    }

    // Getters
    public EntryType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public LocalDate getCompetenceDate() { return competenceDate; }
    public UUID getResponsibleUuid() { return responsibleUuid; }
    public UUID getSaleUuid() { return saleUuid; }
    public Instant getCreatedAt() { return createdAt; }
}
