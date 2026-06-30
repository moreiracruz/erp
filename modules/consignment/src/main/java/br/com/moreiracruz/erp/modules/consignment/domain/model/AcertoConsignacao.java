package br.com.moreiracruz.erp.modules.consignment.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import br.com.moreiracruz.erp.shared.kernel.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AcertoConsignacao extends AggregateRoot {

    private UUID contratoUuid;
    private UUID responsibleUuid;
    private BigDecimal totalAmount;
    private String notes;
    private Instant createdAt;
    private List<AcertoItem> items;

    private AcertoConsignacao() {}

    public static AcertoConsignacao create(UUID contratoUuid, UUID responsibleUuid, String notes, List<AcertoItem> items) {
        if (contratoUuid == null || responsibleUuid == null) {
            throw new ValidationException("Contrato e responsável são obrigatórios");
        }
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Acerto deve conter pelo menos um item");
        }
        BigDecimal total = items.stream()
                .map(AcertoItem::manualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valor total do acerto deve ser positivo");
        }
        AcertoConsignacao acerto = new AcertoConsignacao();
        acerto.uuid = UUID.randomUUID();
        acerto.contratoUuid = contratoUuid;
        acerto.responsibleUuid = responsibleUuid;
        acerto.totalAmount = total;
        acerto.notes = notes == null || notes.isBlank() ? null : notes.trim();
        if (acerto.notes != null && acerto.notes.length() > 500) {
            throw new ValidationException("Observação excede 500 caracteres");
        }
        acerto.createdAt = Instant.now();
        acerto.items = List.copyOf(items);
        return acerto;
    }

    public static AcertoConsignacao restore(UUID uuid, UUID contratoUuid, UUID responsibleUuid,
                                            BigDecimal totalAmount, String notes, Instant createdAt,
                                            List<AcertoItem> items) {
        AcertoConsignacao acerto = new AcertoConsignacao();
        acerto.uuid = uuid;
        acerto.contratoUuid = contratoUuid;
        acerto.responsibleUuid = responsibleUuid;
        acerto.totalAmount = totalAmount;
        acerto.notes = notes;
        acerto.createdAt = createdAt;
        acerto.items = List.copyOf(items);
        return acerto;
    }

    public record AcertoItem(UUID itemUuid, int settledQuantity, BigDecimal manualAmount) {
        public AcertoItem {
            if (itemUuid == null || settledQuantity < 1 || manualAmount == null || manualAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Item de acerto inválido");
            }
        }
    }

    public UUID getContratoUuid() { return contratoUuid; }
    public UUID getResponsibleUuid() { return responsibleUuid; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public List<AcertoItem> getItems() { return items; }
}
