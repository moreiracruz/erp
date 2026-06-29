package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.util.List;

public record ReportSentSalesCommand(List<ItemLine> items) {
    public record ItemLine(java.util.UUID itemUuid, int quantity) {}
}
