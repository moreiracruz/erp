package br.com.moreiracruz.erp.modules.inventory.domain.port.in;

import java.util.UUID;

public record StockResponse(UUID varianteUuid, int physicalStock, int reservedStock, int availableStock) {}
