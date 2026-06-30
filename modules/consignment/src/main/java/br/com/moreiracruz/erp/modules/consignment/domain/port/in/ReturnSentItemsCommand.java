package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.util.List;
import java.util.UUID;

public record ReturnSentItemsCommand(UUID actorUuid, List<ItemLine> items) {
    public record ItemLine(UUID itemUuid, int quantity) {}
}
