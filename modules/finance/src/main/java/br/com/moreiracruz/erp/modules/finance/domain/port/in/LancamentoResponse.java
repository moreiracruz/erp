package br.com.moreiracruz.erp.modules.finance.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for financial entry operations.
 */
public record LancamentoResponse(
        UUID uuid,
        String type,
        BigDecimal amount,
        String paymentMethod,
        String description,
        String category,
        LocalDate competenceDate
) {}
