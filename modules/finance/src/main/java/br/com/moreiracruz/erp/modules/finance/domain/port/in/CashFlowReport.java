package br.com.moreiracruz.erp.modules.finance.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cash flow report aggregated by date range.
 */
public record CashFlowReport(
        LocalDate from,
        LocalDate to,
        BigDecimal totalReceita,
        BigDecimal totalDespesa,
        BigDecimal netBalance,
        List<DailyEntry> dailyEntries
) {}
