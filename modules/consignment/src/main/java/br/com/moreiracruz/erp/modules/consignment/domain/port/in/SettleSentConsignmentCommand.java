package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SettleSentConsignmentCommand(UUID responsibleUuid, String notes, List<ItemLine> items) {
    public record ItemLine(UUID itemUuid, int quantity, BigDecimal manualAmount) {}
}
