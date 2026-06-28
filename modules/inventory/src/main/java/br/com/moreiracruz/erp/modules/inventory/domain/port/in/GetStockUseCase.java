package br.com.moreiracruz.erp.modules.inventory.domain.port.in;

import java.util.UUID;

public interface GetStockUseCase {
    StockResponse getStock(UUID varianteUuid);
}
