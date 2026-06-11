package com.erp.modules.product.domain.model;

import com.erp.shared.exceptions.ValidationException;
import com.erp.shared.utils.MoneyUtils;

import java.math.BigDecimal;

/**
 * Value object representing a monetary amount.
 *
 * <p>The amount is rounded to 2 decimal places (HALF_UP) and must be
 * between 0.01 and 999999.99 inclusive.
 */
public record Money(BigDecimal amount) {

    private static final BigDecimal MIN = new BigDecimal("0.01");
    private static final BigDecimal MAX = new BigDecimal("999999.99");

    public Money {
        if (amount == null) {
            throw new ValidationException("Valor monetário inválido");
        }
        amount = MoneyUtils.round(amount);
        if (amount.compareTo(MIN) < 0 || amount.compareTo(MAX) > 0) {
            throw new ValidationException("Valor monetário inválido");
        }
    }
}
