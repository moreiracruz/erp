package br.com.moreiracruz.erp.modules.product.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;

/**
 * Value object representing a product variant SKU (Stock Keeping Unit).
 *
 * <p>A valid SKU must be between 1 and 50 non-blank characters.
 */
public record Sku(String value) {

    public Sku {
        if (value == null || value.isBlank() || value.length() > 50) {
            throw new ValidationException("SKU inválido");
        }
    }
}
