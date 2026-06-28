package br.com.moreiracruz.erp.modules.inventory.domain.port.in;

import java.util.UUID;

public record StockReserveCommand(UUID varianteUuid, UUID saleUuid, int quantity) {}
