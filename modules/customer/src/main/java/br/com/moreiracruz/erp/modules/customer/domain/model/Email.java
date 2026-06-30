package br.com.moreiracruz.erp.modules.customer.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;

/**
 * Value Object representing a validated email address.
 */
public record Email(String value) {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$";

    public Email {
        if (value == null || !value.matches(EMAIL_REGEX)) {
            throw new ValidationException("Email inválido");
        }
    }
}
