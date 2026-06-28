package br.com.moreiracruz.erp.modules.finance.domain.port.in;

import java.time.LocalDate;

/**
 * Inbound port for generating cash flow reports.
 */
public interface GetCashFlowReportUseCase {
    CashFlowReport getCashFlow(LocalDate from, LocalDate to);
}
