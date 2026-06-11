package com.erp.modules.customer.domain.model;

import com.erp.shared.exceptions.ValidationException;

/**
 * Value Object representing a Brazilian CPF (Cadastro de Pessoa Física).
 * Validates the 11-digit format and the two check digits using the official algorithm.
 */
public record Cpf(String value) {

    public Cpf {
        if (value == null || !value.matches("\\d{11}")) {
            throw new ValidationException("CPF inválido");
        }
        if (allDigitsEqual(value)) {
            throw new ValidationException("CPF inválido");
        }
        if (!isValid(value)) {
            throw new ValidationException("CPF inválido");
        }
    }

    private static boolean allDigitsEqual(String cpf) {
        char first = cpf.charAt(0);
        for (int i = 1; i < 11; i++) {
            if (cpf.charAt(i) != first) return false;
        }
        return true;
    }

    private static boolean isValid(String cpf) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) firstDigit = 0;
        if (firstDigit != (cpf.charAt(9) - '0')) return false;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) secondDigit = 0;
        return secondDigit == (cpf.charAt(10) - '0');
    }
}
