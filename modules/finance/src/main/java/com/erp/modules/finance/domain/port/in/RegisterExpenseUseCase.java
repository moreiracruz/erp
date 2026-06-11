package com.erp.modules.finance.domain.port.in;

/**
 * Inbound port for registering an expense entry.
 */
public interface RegisterExpenseUseCase {
    LancamentoResponse register(RegisterExpenseCommand cmd);
}
