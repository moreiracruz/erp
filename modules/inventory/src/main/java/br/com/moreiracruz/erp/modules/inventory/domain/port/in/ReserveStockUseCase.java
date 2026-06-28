package br.com.moreiracruz.erp.modules.inventory.domain.port.in;

public interface ReserveStockUseCase {
    ReservaResponse reserve(StockReserveCommand cmd);
}
