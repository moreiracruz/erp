package com.erp.modules.inventory.domain.port.in;

public interface RegisterEntryUseCase {
    void registerEntry(StockEntryCommand cmd);
}
