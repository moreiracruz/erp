package br.com.moreiracruz.erp.modules.inventory.domain.port.in;

public interface RegisterWithdrawalUseCase {
    void registerWithdrawal(StockWithdrawalCommand cmd);
}
