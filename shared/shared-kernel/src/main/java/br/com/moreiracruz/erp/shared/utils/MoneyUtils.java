package br.com.moreiracruz.erp.shared.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for monetary arithmetic.
 *
 * <p>All monetary values in the system use {@link BigDecimal} with a scale of 2
 * and {@link RoundingMode#HALF_UP} rounding, matching PostgreSQL's {@code NUMERIC(n,2)}
 * columns and standard accounting conventions.
 */
public final class MoneyUtils {

    private MoneyUtils() {
        // Utility class — not instantiable
    }

    /**
     * Rounds the given amount to 2 decimal places using HALF_UP rounding.
     *
     * @param amount the value to round; must not be {@code null}
     * @return a new {@link BigDecimal} scaled to 2 with HALF_UP rounding
     * @throws NullPointerException if {@code amount} is {@code null}
     */
    public static BigDecimal round(BigDecimal amount) {
        if (amount == null) {
            throw new NullPointerException("amount must not be null");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Returns {@code true} if the given amount is strictly greater than zero.
     *
     * @param amount the value to check; must not be {@code null}
     * @return {@code true} when {@code amount > 0}
     * @throws NullPointerException if {@code amount} is {@code null}
     */
    public static boolean isPositive(BigDecimal amount) {
        if (amount == null) {
            throw new NullPointerException("amount must not be null");
        }
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
