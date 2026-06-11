package com.erp.modules.product.domain.model;

import com.erp.shared.exceptions.ValidationException;

/**
 * Value object representing a product barcode.
 *
 * <p>A valid barcode must consist of exactly 8 to 14 digits (no letters or special characters).
 */
public record Barcode(String value) {

    private static final java.util.regex.Pattern DIGITS_PATTERN =
            java.util.regex.Pattern.compile("^\\d{8,14}$");

    public Barcode {
        if (value == null || !DIGITS_PATTERN.matcher(value).matches()) {
            throw new ValidationException("Código de barras inválido");
        }
    }
}
