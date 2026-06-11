package com.erp.modules.finance.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command for registering a new expense.
 */
public record RegisterExpenseCommand(
        BigDecimal amount,
        String description,
        String category,
        LocalDate competenceDate,
        UUID responsibleUuid
) {}
