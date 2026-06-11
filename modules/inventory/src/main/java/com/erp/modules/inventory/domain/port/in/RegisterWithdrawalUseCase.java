package com.erp.modules.inventory.domain.port.in;

public interface RegisterWithdrawalUseCase {
    void registerWithdrawal(StockWithdrawalCommand cmd);
}
