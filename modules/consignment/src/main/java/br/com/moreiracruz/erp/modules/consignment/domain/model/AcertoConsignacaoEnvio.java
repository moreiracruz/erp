package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AcertoConsignacaoEnvio extends AggregateRoot {

    private UUID contratoUuid;
    private UUID responsibleUuid;
    private BigDecimal totalAmount;
    private String notes;
    private Instant createdAt;
    private List<AcertoItem> items;

    private AcertoConsignacaoEnvio() {}

    public static AcertoConsignacaoEnvio create(UUID contratoUuid, UUID responsibleUuid, String notes, List<AcertoItem> items) {
        if (contratoUuid == null || responsibleUuid == null) {
            throw new ValidationException("Contrato e responsável são obrigatórios");
        }
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Informe pelo menos um item para acerto");
        }
        AcertoConsignacaoEnvio acerto = new AcertoConsignacaoEnvio();
        acerto.uuid = UUID.randomUUID();
        acerto.contratoUuid = contratoUuid;
        acerto.responsibleUuid = responsibleUuid;
        acerto.notes = notes == null || notes.isBlank() ? null : notes.trim();
        acerto.items = List.copyOf(items);
        acerto.totalAmount = items.stream()
                .map(AcertoItem::manualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (acerto.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valor total do acerto deve ser positivo");
        }
        acerto.createdAt = Instant.now();
        return acerto;
    }

    public static AcertoConsignacaoEnvio restore(UUID uuid, UUID contratoUuid, UUID responsibleUuid,
                                                 BigDecimal totalAmount, String notes, Instant createdAt,
                                                 List<AcertoItem> items) {
        AcertoConsignacaoEnvio acerto = new AcertoConsignacaoEnvio();
        acerto.uuid = uuid;
        acerto.contratoUuid = contratoUuid;
        acerto.responsibleUuid = responsibleUuid;
        acerto.totalAmount = totalAmount;
        acerto.notes = notes;
        acerto.createdAt = createdAt;
        acerto.items = List.copyOf(items);
        return acerto;
    }

    public UUID getContratoUuid() { return contratoUuid; }
    public UUID getResponsibleUuid() { return responsibleUuid; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public List<AcertoItem> getItems() { return items; }

    public record AcertoItem(UUID itemUuid, int settledQuantity, BigDecimal manualAmount) {
        public AcertoItem {
            if (itemUuid == null || settledQuantity < 1 || manualAmount == null || manualAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Item, quantidade e valor positivo são obrigatórios no acerto");
            }
        }
    }
}
