package br.com.moreiracruz.erp.modules.finance.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Single day within a cash flow report.
 */
public record DailyEntry(
        LocalDate date,
        List<LancamentoResponse> receitas,
        List<LancamentoResponse> despesas,
        BigDecimal dailyBalance
) {}
